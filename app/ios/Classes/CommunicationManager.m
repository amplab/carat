//
//  CommunicationManager.m
//  Carat
//
//  Handles communication with CARAT server. 
//
//  Created by Anand Padmanabha Iyer on 11/5/11.
//  Copyright (c) 2011 UC Berkeley. All rights reserved.
//

#import "CommunicationManager.h"
#import "Reachability.h"
#import "Utilities.h"
#import "UIDeviceHardware.h"

@interface CommunicationManager() 
@property (retain) TSocketClient *transport;
@property (retain) TBinaryProtocol *protocol;
@property (retain) CaratServiceClient *service;
@end

@implementation CommunicationManager 

@synthesize transport;
@synthesize protocol;
@synthesize service;

static id instance = nil;
static NSString * caratServerAddress = nil;
static int caratServerPort = 8080;
static BOOL isInternetActive;
static NSString * networkStatusString;

+ (void) initialize {
    if (self == [CommunicationManager class]) {
        instance = [[self alloc] init];
    }
    
    //caratServerAddress = @"server.caratproject.com";
    caratServerAddress = @"caratserver.kurolabs.co";
    
    [instance setupReachabilityNotifications];
}

+ (id) instance {
    return instance;
}

- (void) setupReachabilityNotifications
{
    isInternetActive = NO;
    networkStatusString = @"NotReachable";
    
    [[NSNotificationCenter defaultCenter] addObserver:self 
                                             selector:@selector(checkNetworkStatus:) 
                                                 name:kReachabilityChangedNotification 
                                               object:nil];
    internetReachable = [Reachability reachabilityWithHostName:caratServerAddress];
    if ([internetReachable startNotifier]) { DLog(@"%s Success!", __PRETTY_FUNCTION__); }
}

//
// Checks if the service is already setup.
//
- (bool) isCaratServiceSetup
{
    if (service != Nil) 
    {
        return YES;
    }
    return NO;
}

- (void) shutdownCaratService
{
    @try {
        [[self service] release];
        [[self protocol] release];
        [[self transport] release];
        DLog(@"%s Success!", __PRETTY_FUNCTION__);
    }
    @catch (NSException *exception) {
        DLog(@"%s Caught %@: %@", __PRETTY_FUNCTION__, [exception name], [exception reason]);
    }
}

//
//  Setup the carat thrift service. We check if we already setup the service. 
//  
//
- (bool) setupCaratService
{
    //if ([self isCaratServiceSetup]) 
    //    return YES;
    
    //
    // Try setting it up.
    //
    @try 
    {
        [self setTransport:[[TSocketClient alloc] initWithHostname:caratServerAddress port:caratServerPort]];
        [self setProtocol:[[TBinaryProtocol alloc] initWithTransport:transport strictRead:YES strictWrite:YES]];
        [self setService:[[CaratServiceClient alloc] initWithProtocol:protocol]];
        DLog(@"%s Success!", __PRETTY_FUNCTION__);
        return YES;
    }
    @catch (NSException *exception) 
    {
        DLog(@"%s Caught %@: %@", __PRETTY_FUNCTION__, [exception name], [exception reason]);
        [self shutdownCaratService];
    }
    return NO;
}

//
//  Send a registration message.
//
- (BOOL) sendRegistrationMessage:(Registration *) registrationMessage
{
    BOOL ret = NO;
    
    if ([self setupCaratService] == YES) {
        @try {
            [service registerMe:registrationMessage];
            ret = YES;
            DLog(@"%s Success!", __PRETTY_FUNCTION__);
        }
        @catch (NSException *exception) {
            DLog(@"%s Caught %@: %@", __PRETTY_FUNCTION__, [exception name], [exception reason]);
        }
        [self shutdownCaratService];
    }
    return ret;
}

//
//
//
- (HogBugReport *) getHogsImmediatelyAndMaybeRegister:(Registration *) registrationMessage  : (NSArray *) processList {
    //TODO
    return nil;
}


//
//  Send sample to the server.
//
- (BOOL) sendSample:(Sample *)sample
{
    BOOL ret = NO;
    
    if ([self setupCaratService] == YES) 
    {
        @try {
            [service uploadSample:sample];
            ret = YES;
            DLog(@"%s Success!", __PRETTY_FUNCTION__);
        }
        @catch (NSException *exception) {
            DLog(@"%s Caught %@: %@", __PRETTY_FUNCTION__, [exception name], [exception reason]);
        }
        [self shutdownCaratService];
    }
    
    return ret;
}

- (Reports *) getReports
{
    if ([self setupCaratService] == YES) 
    {
        @try {
            Feature *feature1 = [[[Feature alloc] init] autorelease];
            Feature *feature2 = [[[Feature alloc] init] autorelease];
            [feature1 setKey:@"OS"];
            [feature1 setValue:[UIDevice currentDevice].systemVersion];
            [feature2 setKey:@"Model"];
            UIDeviceHardware *h =[[UIDeviceHardware alloc] init];
            [feature2 setValue:[h platformString]];
            [h release];
            FeatureList featureList = [[NSArray alloc] initWithObjects:feature1, feature2, nil];
            return [service getReports:[[Globals instance] getUUID]
                                      :featureList];
        }
        @catch (NSException *exception) {
            DLog(@"%s Caught %@: %@", __PRETTY_FUNCTION__, [exception name], [exception reason]);
        }
        [self shutdownCaratService];
    }
    return nil;
}

- (HogBugReport *) getHogOrBugReport:(FeatureList) featureList
{
    if ([self setupCaratService] == YES) 
    {
        @try {
            return [service getHogOrBugReport:[[Globals instance] getUUID ]
                                             :featureList];
        }
        @catch (NSException *exception) {
            DLog(@"%s Caught %@: %@", __PRETTY_FUNCTION__, [exception name], [exception reason]);
        }
        [self shutdownCaratService];
    }
    return nil;
}

- (BOOL) isInternetReachable
{
    DLog(@"%s %d", __PRETTY_FUNCTION__, isInternetActive);
    return isInternetActive;
}

- (NSString *) networkStatusString
{
    DLog(@"%s %@", __PRETTY_FUNCTION__, networkStatusString);
    return networkStatusString;
}

- (void) checkNetworkStatus:(NSNotification *) notice
{
    DLog(@"%s", __PRETTY_FUNCTION__);
    NetworkStatus internetStatus = [internetReachable currentReachabilityStatus];
    switch (internetStatus)
    {
        case NotReachable:
        {
            DLog(@"%s NetworkStatus changed to NotReachable", __PRETTY_FUNCTION__);
            isInternetActive = NO;
            networkStatusString = @"NotReachable";
            break;
        }
        case ReachableViaWiFi:
        {
            DLog(@"%s NetworkStatus changed to ReachableViaWiFi", __PRETTY_FUNCTION__);
            isInternetActive = YES;
            networkStatusString = @"ReachableViaWiFi";
            break;
        }
        case ReachableViaWWAN:
        {
            DLog(@"%s NetworkStatus changed to ReachableViaWWAN", __PRETTY_FUNCTION__);
            isInternetActive = YES;
            networkStatusString = @"ReachableViaWWAN";
            break;
        }
    }
}

//
// Cleanup stuff.
//
- (void) dealloc
{
    [service release];
    [protocol release];
    [transport release];
    [super dealloc];
}

@end
