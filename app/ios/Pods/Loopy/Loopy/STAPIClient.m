//
//  STAPIClient.m
//  Loopy
//
//  Created by David Jedeikin on 9/10/13.
//  Copyright (c) 2013 ShareThis. All rights reserved.
//

#import "STAPIClient.h"
#import "STJSONUtils.h"
#import "STReachability.h"
#import "STIdentifier.h"
#import "STIdentifierFactory.h"
#import "STClient.h"
#import "STGeo.h"
#import "STItem.h"
#import "STIdentifier.h"

@implementation STAPIClient

NSString *const INSTALL = @"/install";
NSString *const OPEN = @"/open";
NSString *const SHORTLINK = @"/shortlink";
NSString *const REPORT_SHARE = @"/share";
NSString *const SHARELINK = @"/sharelink";
NSString *const LOG = @"/log";

NSString *const API_KEY = @"X-LoopyAppID";
NSString *const LOOPY_KEY = @"X-LoopyKey";
NSString *const STDID_KEY = @"stdid";
NSString *const MD5ID_KEY = @"md5id";
NSString *const LAST_OPEN_TIME_KEY = @"lastOpenTime";
NSString *const SESSION_DATA_FILENAME = @"STSessionData.plist";

@synthesize callTimeout = _callTimeout;
@synthesize openTimeout = _openTimeout;
@synthesize urlPrefix;
@synthesize httpsURLPrefix;
@synthesize apiKey;
@synthesize loopyKey;
@synthesize stdid;
@synthesize shortlinks;

#pragma  mark - Constructors

//init API with specified keys and with location services enabled
//does NOT call any endpoints
- (id)initWithAPIKey:(NSString *)key
            loopyKey:(NSString *)lkey {
    return [self initWithAPIKey:key
                       loopyKey:lkey
              locationsDisabled:NO];
}

//init API with specified keys and default identifier types
//does NOT call any endpoints
- (id)initWithAPIKey:(NSString *)key
            loopyKey:(NSString *)lkey
   locationsDisabled:(BOOL)locationServicesDisabled {
    return [self initWithAPIKey:key
                       loopyKey:lkey
              locationsDisabled:locationServicesDisabled
                 identifierType:STIdentifierTypeStandard];
}

//init API with specified keys
//does NOT call any endpoints
- (id)initWithAPIKey:(NSString *)key
            loopyKey:(NSString *)lkey
   locationsDisabled:(BOOL)locationServicesDisabled
      identifierType:(STIdentifierType)identifierType {
    self = [super init];
    if(self) {
        //device info
        self.deviceSettings = [[STDeviceSettings alloc] initWithLocationsDisabled:locationServicesDisabled
                                                                   identifierType:identifierType];
        
        //init shortlink cache
        self.shortlinks = [NSMutableDictionary dictionary];
        
        //set keys
        self.apiKey = key;
        self.loopyKey = lkey;
        
        //set URLs
        NSBundle *bundle =  [NSBundle bundleForClass:[self class]];
        NSString *configPath = [bundle pathForResource:@"LoopyApiInfo" ofType:@"plist"];
        NSDictionary *configurationDict = [[NSDictionary alloc]initWithContentsOfFile:configPath];
        NSDictionary *apiInfoDict = [configurationDict objectForKey:@"Loopy API info"];
        self.urlPrefix = [apiInfoDict objectForKey:@"urlPrefix"];
        self.httpsURLPrefix = [apiInfoDict objectForKey:@"urlHttpsPrefix"];
        
        //set timeouts
        NSNumber *callTimeoutMillis = [apiInfoDict objectForKey:@"callTimeoutInMillis"];
        NSNumber *openTimeoutMillis = [apiInfoDict objectForKey:@"openTimeoutInMillis"];
        _callTimeout = [callTimeoutMillis floatValue] / 1000.0f;
        _openTimeout = [openTimeoutMillis floatValue] / 1000.0f;
    }
    return self;
}

#pragma mark - Identities Handling

//creates/loads session file from disk, and calls appropriate recording endpoint (/open or /install) as required
- (void)getSessionWithReferrer:(NSString *)referrer
                   postSuccess:(void(^)(AFHTTPRequestOperation *, id))postSuccessCallback
                       failure:(void(^)(AFHTTPRequestOperation *, NSError *))failureCallback {
    NSString *rootPath = [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) objectAtIndex:0];
    NSString *filePath = [rootPath stringByAppendingPathComponent:SESSION_DATA_FILENAME];
    NSMutableDictionary *plistDict = [[NSMutableDictionary alloc] initWithContentsOfFile:filePath];
    NSDate *now = [NSDate date];
    NSNumber *nowNum = [NSNumber numberWithDouble:[now timeIntervalSince1970]];
    NSString *error = nil;
    NSData *plistData = nil;
    
    //no file -- call /install and store device-generated stdid in new file
    if(!plistDict) {
        NSUUID *stdidObj = (NSUUID *)[NSUUID UUID];
        self.stdid = (NSString *)[stdidObj UUIDString];
        NSMutableDictionary *newPlistDict = [NSMutableDictionary dictionaryWithObjectsAndKeys:
                                             self.stdid,STDID_KEY,
                                             nowNum,LAST_OPEN_TIME_KEY,
                                             nil];
        plistData = [NSPropertyListSerialization dataFromPropertyList:(id)newPlistDict
                                                               format:NSPropertyListXMLFormat_v1_0
                                                     errorDescription:&error];
        [plistData writeToFile:filePath atomically:YES];
        
        [self install:[self installWithReferrer:referrer]
              success:^(AFHTTPRequestOperation *operation, id responseObject) {
                  if(postSuccessCallback != nil) {
                      postSuccessCallback(operation, responseObject);
                  }
              }
              failure:failureCallback];
    }
    //file exists -- call /open with stdid from file if timeout has been hit
    //store updated timestamp in file if new open needs to be called
    else {
        self.stdid = (NSString *)[plistDict valueForKey:STDID_KEY];
        NSNumber *lastOpenNum = (NSNumber *)[plistDict valueForKey:LAST_OPEN_TIME_KEY];
        double diff = [nowNum doubleValue] - [lastOpenNum doubleValue];
        
        if(diff > _openTimeout) {
            plistData = [NSPropertyListSerialization dataFromPropertyList:(id)plistDict
                                                                   format:NSPropertyListXMLFormat_v1_0
                                                         errorDescription:&error];
            [plistData writeToFile:filePath atomically:YES];
            
            [self open:[self openWithReferrer:referrer]//[self openDictionaryWithReferrer:referrer]
               success:^(AFHTTPRequestOperation *operation, id responseObject) {
                   if(postSuccessCallback != nil) {
                       postSuccessCallback(operation, responseObject);
                   }
               }
               failure:failureCallback];
        }
        //bogus call to success to indicating no open needed
        else {
            if(postSuccessCallback != nil) {
                postSuccessCallback(nil, nil);
            }
        }
    }
}

#pragma mark - URL Requests

//factory method for URLRequest for specified JSON data and endpoint
- (NSMutableURLRequest *)newHTTPSURLRequest:(NSData *)jsonData
                                     length:(NSNumber *)length
                                   endpoint:(NSString *)endpoint {
    NSString *urlStr = [NSString stringWithFormat:@"%@%@", httpsURLPrefix, endpoint];
    return [self jsonURLRequestForURL:urlStr data:jsonData length:length];
}

//factory method for URLRequest for specified JSON data and endpoint
- (NSMutableURLRequest *)newURLRequest:(NSData *)jsonData
                                length:(NSNumber *)length
                              endpoint:(NSString *)endpoint {
    NSString *urlStr = [NSString stringWithFormat:@"%@%@", urlPrefix, endpoint];
    return [self jsonURLRequestForURL:urlStr data:jsonData length:length];
}

//convenience method
-(NSMutableURLRequest *)jsonURLRequestForURL:(NSString *)urlStr
                                        data:(NSData *)jsonData
                                      length:(NSNumber *)length {
    NSURL *url = [NSURL URLWithString:urlStr];
    NSMutableURLRequest *request = [NSMutableURLRequest requestWithURL:url
                                                           cachePolicy:NSURLRequestUseProtocolCachePolicy
                                                       timeoutInterval:_callTimeout];
    [request setHTTPMethod:@"POST"];
    [request setValue:@"application/json" forHTTPHeaderField:@"Accept"];
    [request setValue:@"application/json" forHTTPHeaderField:@"Content-Type"];
    [request setValue:self.apiKey forHTTPHeaderField:API_KEY];
    [request setValue:self.loopyKey forHTTPHeaderField:LOOPY_KEY];
    [request setValue:[length stringValue] forHTTPHeaderField:@"Content-Length"];
    [request setHTTPBody:jsonData];
    
    return request;
}

//factory method to init operations with specified requests and callbacks
- (AFHTTPRequestOperation *)newURLRequestOperation:(NSURLRequest *)request
                                           isHTTPS:(BOOL)https
                                           success:(void(^)(AFHTTPRequestOperation *, id))successCallback
                                           failure:(void(^)(AFHTTPRequestOperation *, NSError *))failureCallback {
    AFHTTPRequestOperation *operation = [[AFHTTPRequestOperation alloc] initWithRequest:request];
    operation.responseSerializer = [AFJSONResponseSerializer serializer];
    [operation setCompletionBlockWithSuccess:successCallback
                                     failure:failureCallback];
    
    //allow self-signed certs for HTTPS
    if(https) {
        [operation setWillSendRequestForAuthenticationChallengeBlock:^(NSURLConnection *connection, NSURLAuthenticationChallenge *challenge) {
            SecTrustRef trust = challenge.protectionSpace.serverTrust;
            NSURLCredential *cred = [NSURLCredential credentialForTrust:trust];
            [challenge.sender useCredential:cred forAuthenticationChallenge:challenge];
            [challenge.sender continueWithoutCredentialForAuthenticationChallenge:challenge];
        }];
    }
    return operation;
}

//Returns error code
//if code is nil or no error value contained, returns nil
- (NSNumber *)loopyErrorCode:(NSDictionary *)errorDict {
    NSNumber *errorCode = nil;
    id codeObj = [errorDict valueForKey:@"code"];
    if([codeObj isKindOfClass:[NSNumber class]]) {
        errorCode = (NSNumber *)codeObj;
    }
    return errorCode;
}

//Returns array of error values taken from the userInfo portion of error returned from request
//if error is nil or no error value contained, returns nil
- (NSArray *)loopyErrorArray:(NSDictionary *)errorDict {
    NSArray *errorArray = nil;
    id errorObj = [errorDict valueForKey:@"error"];
    
    if([errorObj isKindOfClass:[NSArray class]]) {
        errorArray = (NSArray *)errorObj;
    }
    return errorArray;
}

#pragma mark - Objects For Endpoints

//returns JSON-ready object for /install endpoint for specified referrer
- (STInstall *)installWithReferrer:(NSString *)referrer {
    STInstall *installObj = [STInstall installWithReferrer:referrer];
    
    int timestamp = [[NSDate date] timeIntervalSince1970];
    installObj.timestamp = [NSNumber numberWithInt:timestamp];
    installObj.stdid = self.stdid;
    installObj.md5id = self.deviceSettings.md5id;
    installObj.device = self.deviceSettings.device;
    installObj.app = self.deviceSettings.app;
    installObj.client = [STClient client];
    
    return installObj;
}

//returns JSON-ready object for /open endpoint for specified referrer
- (STOpen *)openWithReferrer:(NSString *)referrer {
    STOpen *openObj = [STOpen openWithReferrer:referrer];
    
    int timestamp = [[NSDate date] timeIntervalSince1970];
    openObj.timestamp = [NSNumber numberWithInt:timestamp];
    openObj.stdid = self.stdid;
    openObj.md5id = self.deviceSettings.md5id;
    openObj.device = self.deviceSettings.device;
    openObj.app = self.deviceSettings.app;
    openObj.client = [STClient client];
    
    return openObj;
}

//returns JSON-ready dictionary for /share endpoint, based on shortlink and channel
- (STShare *)reportShareWithShortlink:(NSString *)shortlink channel:(NSString *)socialChannel {
    STShare *reportShareObj = [[STShare alloc] init];

    int timestamp = [[NSDate date] timeIntervalSince1970];
    reportShareObj.timestamp = [NSNumber numberWithInt:timestamp];
    reportShareObj.stdid = self.stdid;
    reportShareObj.md5id = self.deviceSettings.md5id;
    reportShareObj.shortlink = shortlink;
    reportShareObj.channel = socialChannel;
    reportShareObj.device = self.deviceSettings.device;
    reportShareObj.app = self.deviceSettings.app;
    reportShareObj.client = [STClient client];
    
    return reportShareObj;
}

//returns JSON-ready dictionary for /shortlink endpoint, based on link, title, meta, and tags
//Either link OR title may be nil, but not both
//Meta may be nil, or may contain various OG keys
- (STShortlink *)shortlinkWithURL:(NSString *)link
                     title:(NSString *)title
                      meta:(NSDictionary *)meta
                      tags:(NSArray *)tags {
    STShortlink *shortlinkObj = [[STShortlink alloc] init];
    shortlinkObj.stdid = self.stdid;
    shortlinkObj.md5id = self.deviceSettings.md5id;
    
    int timestamp = [[NSDate date] timeIntervalSince1970];
    shortlinkObj.timestamp = [NSNumber numberWithInt:timestamp];
    
    STItem *item = [[STItem alloc] init];
    if(link != nil) {
        item.url = link;
    }
    if(title != nil) {
        item.title = title;
    }
    if(meta != nil) {
        item.meta = meta;
    }
    shortlinkObj.item = item;

    if(tags != nil) {
        shortlinkObj.tags = tags;
    }
    
    return shortlinkObj;
}

//returns JSON-ready object for /sharelink endpoint
- (STSharelink *)sharelinkWithURL:(NSString *)link
                          channel:(NSString *)socialChannel
                            title:(NSString *)title
                             meta:(NSDictionary *)meta
                             tags:(NSArray *)tags {
    int timestamp = [[NSDate date] timeIntervalSince1970];
    STSharelink *sharelinkObj = [[STSharelink alloc] init];

    sharelinkObj.stdid = self.stdid;
    sharelinkObj.md5id = self.deviceSettings.md5id;
    sharelinkObj.timestamp = [NSNumber numberWithInt:timestamp];
    sharelinkObj.channel = socialChannel;
    sharelinkObj.device = self.deviceSettings.device;
    sharelinkObj.app = self.deviceSettings.app;
    sharelinkObj.client = [STClient client];

    STItem *item = [[STItem alloc] init];
    if(link != nil) {
        item.url = link;
    }
    if(title != nil) {
        item.title = title;
    }
    if(meta != nil) {
        item.meta = meta;
    }
    sharelinkObj.item = item;
    
    
    if(tags != nil) {
        sharelinkObj.tags = tags;
    }

    return sharelinkObj;
}

//returns JSON-ready object for /log endpoint, based on type and meta
- (STLog *)logWithType:(NSString *)type meta:(NSDictionary *)meta {
    int timestamp = [[NSDate date] timeIntervalSince1970];
    STEvent *eventObj = [[STEvent alloc] init];
    eventObj.type = type;
    eventObj.meta = meta;

    STLog *logObj = [[STLog alloc] init];
    logObj.stdid = self.stdid;
    logObj.md5id = self.deviceSettings.md5id;
    logObj.timestamp = [NSNumber numberWithInt:timestamp];
    logObj.device = self.deviceSettings.device;
    logObj.app = self.deviceSettings.app;
    logObj.event = eventObj;
    logObj.client = [STClient client];
   
    return logObj;
}

#pragma mark - Calling Endpoints

- (void)install:(STInstall *)installObj
        success:(void(^)(AFHTTPRequestOperation *, id))successCallback
        failure:(void(^)(AFHTTPRequestOperation *, NSError *))failureCallback {
    [self callHTTPSEndpoint:INSTALL json:installObj success:successCallback failure:failureCallback];
}

- (void)open:(STOpen *)openObj
     success:(void(^)(AFHTTPRequestOperation *, id))successCallback
     failure:(void(^)(AFHTTPRequestOperation *, NSError *))failureCallback {
    [self callEndpoint:OPEN json:openObj success:successCallback failure:failureCallback];
}

- (void)reportShare:(STShare *)reportShareObj
            success:(void(^)(AFHTTPRequestOperation *, id))successCallback
            failure:(void(^)(AFHTTPRequestOperation *, NSError *))failureCallback {
    [self callEndpoint:REPORT_SHARE
                  json:reportShareObj
               success:^(AFHTTPRequestOperation *operation, id responseObject) {
                   //remove current shortlink from cache
                   //although shortlinks are the values (not keys) of the shortlinks dictionary, they should be unique
                   //thus keys should contain only one element
                   NSString *shortlink = reportShareObj.shortlink;
                   NSArray *keys = [self.shortlinks allKeysForObject:shortlink];
                   for(id key in keys) {
                       [self.shortlinks removeObjectForKey:key];
                   }
                   if(successCallback != nil) {
                       successCallback(operation, responseObject);
                   }
               }
               failure:failureCallback];
}

- (void)shortlink:(STShortlink *)jsonObj
          success:(void(^)(AFHTTPRequestOperation *, id))successCallback
          failure:(void(^)(AFHTTPRequestOperation *, NSError *))failureCallback {
    //check the cache to see if shortlink already exists, and if so, simply call successCallback
    STItem *item = jsonObj.item;
    NSString *url = item.url;
    if([self.shortlinks valueForKey:url]) {
        NSDictionary *shortlinkDict = [NSDictionary dictionaryWithObjectsAndKeys:[self.shortlinks valueForKey:url], @"shortlink", nil];
        successCallback(nil, shortlinkDict);
    }
    else {
        [self callEndpoint:SHORTLINK
                      json:jsonObj
                   success:^(AFHTTPRequestOperation *operation, id responseObject) {
                       //cache the shortlink for future reuse
                       NSDictionary *responseDict = (NSDictionary *)responseObject;
                       [self.shortlinks setValue:[responseDict valueForKey:@"shortlink"] forKey:url];
                       if(successCallback != nil) {
                           successCallback(operation, responseObject);
                       }
                   }
                   failure:failureCallback];
    }
}

- (void)sharelink:(STSharelink *)jsonObj
          success:(void(^)(AFHTTPRequestOperation *, id))successCallback
          failure:(void(^)(AFHTTPRequestOperation *, NSError *))failureCallback {
    [self callEndpoint:SHARELINK
                  json:jsonObj
               success:^(AFHTTPRequestOperation *operation, id responseObject) {
                   //remove current shortlink from cache
                   //although shortlinks are the values (not keys) of the shortlinks dictionary, they should be unique
                   //thus keys should contain only one element
                   NSDictionary *responseDict = (NSDictionary *)responseObject;
                   NSString *shortlink = (NSString *)[responseDict objectForKey:@"shortlink"];
                   NSArray *keys = [self.shortlinks allKeysForObject:shortlink];
                   for(id key in keys) {
                       [self.shortlinks removeObjectForKey:key];
                   }
                   if(successCallback != nil) {
                       successCallback(operation, responseObject);
                   }
               }
               failure:failureCallback];
}

- (void)log:(STLog *)jsonObj
    success:(void(^)(AFHTTPRequestOperation *, id))successCallback
    failure:(void(^)(AFHTTPRequestOperation *, NSError *))failureCallback {
    [self callEndpoint:LOG json:jsonObj success:successCallback failure:failureCallback];
}

//convenience method
- (void)callHTTPSEndpoint:(NSString *)endpoint
                     json:(id)jsonObj
                  success:(void(^)(AFHTTPRequestOperation *, id))successCallback
                  failure:(void(^)(AFHTTPRequestOperation *, NSError *))failureCallback {
    NSData *jsonData = nil;
    NSString *jsonStr = nil;
    //allow dictionaries at this level, though all higher-level calls now expect objects
    if([jsonObj isKindOfClass:[NSDictionary class]]) {
        jsonData = [STJSONUtils toJSONData:jsonObj];
    }
    else if([jsonObj isKindOfClass:[STObject class]]) {
        STObject *obj = (STObject *)jsonObj;
        jsonData = [STJSONUtils toJSONDataFromObject:obj];
    }
    jsonStr = [STJSONUtils toJSONString:jsonData];
    
    NSNumber *jsonLength = [NSNumber numberWithLong:[jsonStr length]];
    NSURLRequest *request = [self newHTTPSURLRequest:jsonData
                                              length:jsonLength
                                            endpoint:endpoint];
    AFHTTPRequestOperation *operation = [self newURLRequestOperation:request
                                                             isHTTPS:YES
                                                             success:successCallback
                                                             failure:failureCallback];
    [operation start];
}

//convenience method
- (void)callEndpoint:(NSString *)endpoint
                json:(id)jsonObj
             success:(void(^)(AFHTTPRequestOperation *, id))successCallback
             failure:(void(^)(AFHTTPRequestOperation *, NSError *))failureCallback {
    NSData *jsonData = nil;
    NSString *jsonStr = nil;
    //allow dictionaries at this level, though all higher-level calls now expect objects
    if([jsonObj isKindOfClass:[NSDictionary class]]) {
        jsonData = [STJSONUtils toJSONData:jsonObj];
    }
    else if([jsonObj isKindOfClass:[STObject class]]) {
        STObject *obj = (STObject *)jsonObj;
        jsonData = [STJSONUtils toJSONDataFromObject:obj];
    }
    jsonStr = [STJSONUtils toJSONString:jsonData];

    NSNumber *jsonLength = [NSNumber numberWithLong:[jsonStr length]];
    NSURLRequest *request = [self newURLRequest:jsonData
                                         length:jsonLength
                                       endpoint:endpoint];
    AFHTTPRequestOperation *operation = [self newURLRequestOperation:request
                                                             isHTTPS:NO
                                                             success:successCallback
                                                             failure:failureCallback];
    [operation start];
}

@end
