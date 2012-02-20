//
//  CommunicationManager.h
//  Carat
//
//  Created by Anand Padmanabha Iyer on 11/5/11.
//  Copyright (c) 2011 UC Berkeley. All rights reserved.
//

#import <Foundation/Foundation.h>

#import "Globals.h"
#import "CaratProtocol.h"
#import "Thrift/transport/TSocketClient.h"
#import "Thrift/protocol/TBinaryProtocol.h"

@class Reachability;

@interface CommunicationManager : NSObject
{
    Reachability* internetReachable;
}

+ (id) instance;
- (void) setupReachabilityNotifications;
- (BOOL) sendRegistrationMessage:(Registration *) registrationMessage;
- (BOOL) sendSample:(Sample *) sample;
- (Reports *) getReports;
- (HogBugReport *) getHogOrBugReport:(FeatureList) featureList;
- (BOOL) isInternetReachable; 
- (void) checkNetworkStatus:(NSNotification *)notice;
@end
