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

@interface CommunicationManager() 
@property (retain) TSocketClient *transport;
@property (retain) TBinaryProtocol *protocol;
@property (retain) CaratServiceClient *service;
@end

@implementation CommunicationManager 

@synthesize transport;
@synthesize protocol;
@synthesize service;

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
    @try {
        [self setTransport:[[TSocketClient alloc] initWithHostname:@"localhost" port:4444]];
        [self setProtocol:[[TBinaryProtocol alloc] initWithTransport:transport strictRead:YES strictWrite:YES]];
        [self setService:[[CaratServiceClient alloc] initWithProtocol:protocol]];
        NSLog(@"setupCaratService: CARAT service setup successful.");
        return YES;
    }
    @catch (NSException *exception) {
        NSLog(@"setupCaratService: Caught %@: %@", [exception name], [exception reason]);
    }
    return NO;
}

//
//  Send a registration message.
//
- (void) sendRegistrationMessage:(Registration *) registrationMessage
{
    if ([self setupCaratService] == YES) {
        @try {
            [service registerMe:registrationMessage];
            NSLog(@"sendRegistrationMessage: Success!");
        }
        @catch (NSException *exception) {
            NSLog(@"sendRegistrationMessage: Caught %@: %@", [exception name], [exception reason]);
        }
    }
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
            NSLog(@"sendSample: Success!");
        }
        @catch (NSException *exception) {
            NSLog(@"sendSample: Caught %@: %@", [exception name], [exception reason]);
        }
    }
    
    return ret;
}

- (HogBugReport *) getHogOrBugReport:(FeatureList) featureList
{
    if ([self setupCaratService] == YES) 
    {
        @try {
            return [service getHogOrBugReport:[[Globals instance] getUUID ]
                                             :featureList];
            NSLog(@"getHogOrBugReport: Success!");
        }
        @catch (NSException *exception) {
            NSLog(@"getHogOrBugReport: Caught %@: %@", [exception name], [exception reason]);
        }
    }
    return NULL;
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
