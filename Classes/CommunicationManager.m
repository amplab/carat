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
static NSString* caratServerIP = nil;
static int caratServerPort = 4444;
static BOOL isInternetActive;

+ (void) initialize {
    if (self == [CommunicationManager class]) {
        instance = [[self alloc] init];
    }
    //caratServerIP = @"localhost";
    caratServerIP = @"50.18.127.4";
    
    [instance setupReachabilityNotifications];
}

+ (id) instance {
    return instance;
}

- (void) setupReachabilityNotifications
{
    isInternetActive = NO;
    
    [[NSNotificationCenter defaultCenter] addObserver:self 
                                             selector:@selector(checkNetworkStatus:) 
                                                 name:kReachabilityChangedNotification 
                                               object:nil];
    //internetReachable = [[Reachability reachabilityForInternetConnection] retain];
    internetReachable = [Reachability reachabilityWithHostName: @"www.apple.com"];
    [internetReachable startNotifier];
    NSLog(@"%s Success!", __PRETTY_FUNCTION__);
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
        NSLog(@"%s Success!", __PRETTY_FUNCTION__);
    }
    @catch (NSException *exception) {
        NSLog(@"%s Caught %@: %@", __PRETTY_FUNCTION__, [exception name], [exception reason]);
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
        [self setTransport:[[TSocketClient alloc] initWithHostname:caratServerIP port:caratServerPort]];
        [self setProtocol:[[TBinaryProtocol alloc] initWithTransport:transport strictRead:YES strictWrite:YES]];
        [self setService:[[CaratServiceClient alloc] initWithProtocol:protocol]];
        NSLog(@"%s Success!", __PRETTY_FUNCTION__);
        return YES;
    }
    @catch (NSException *exception) 
    {
        NSLog(@"%s Caught %@: %@", __PRETTY_FUNCTION__, [exception name], [exception reason]);
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
            NSLog(@"%s Success!", __PRETTY_FUNCTION__);
        }
        @catch (NSException *exception) {
            NSLog(@"%s Caught %@: %@", __PRETTY_FUNCTION__, [exception name], [exception reason]);
        }
        [self shutdownCaratService];
    }
    return ret;
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
            NSLog(@"%s Success!", __PRETTY_FUNCTION__);
        }
        @catch (NSException *exception) {
            NSLog(@"%s Caught %@: %@", __PRETTY_FUNCTION__, [exception name], [exception reason]);
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
            return [service getReports:[[Globals instance] getUUID]];
            NSLog(@"%s Success!", __PRETTY_FUNCTION__);
        }
        @catch (NSException *exception) {
            NSLog(@"%s Caught %@: %@", __PRETTY_FUNCTION__, [exception name], [exception reason]);
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
            NSLog(@"%s Success!", __PRETTY_FUNCTION__);
        }
        @catch (NSException *exception) {
            NSLog(@"%s Caught %@: %@", __PRETTY_FUNCTION__, [exception name], [exception reason]);
        }
        [self shutdownCaratService];
    }
    return nil;
}

- (BOOL) isInternetReachable
{
    NSLog(@"%s %d", __PRETTY_FUNCTION__, isInternetActive);
    return isInternetActive;
}

- (void) checkNetworkStatus:(NSNotification *) notice
{
    NetworkStatus internetStatus = [internetReachable currentReachabilityStatus];
    switch (internetStatus)
    {
        case NotReachable:
        {
            NSLog(@"%s NetworkStatus changed to NotReachable", __PRETTY_FUNCTION__);
            isInternetActive = NO;
            break;
        }
        case ReachableViaWiFi:
        {
            NSLog(@"%s NetworkStatus changed to ReachableViaWiFi", __PRETTY_FUNCTION__);
            isInternetActive = YES;
            break;
        }
        case ReachableViaWWAN:
        {
            NSLog(@"%s NetworkStatus changed to ReachableViaWWAN", __PRETTY_FUNCTION__);
            isInternetActive = YES;
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
