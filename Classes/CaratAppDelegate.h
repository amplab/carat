//
//  CaratAppDelegate.h
//  Carat
//
//  Created by Adam Oliner on 10/6/11.
//  Copyright 2011 Stanford University. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <CoreData/CoreData.h>
#import <CoreLocation/CoreLocation.h>
#import "CaratProtocol.h"
#import "Sampler.h"
#import "CommunicationManager.h"
#import "Thrift/transport/TSocketClient.h"
#import "Thrift/protocol/TBinaryProtocol.h"

@class Reachability;

@interface CaratAppDelegate : NSObject <UIApplicationDelegate, UITabBarControllerDelegate, CLLocationManagerDelegate> {
    UIWindow *window;
    UITabBarController *tabBarController;
    CLLocationManager *locationManager;
    Reachability *hostReachable;
    UIBackgroundTaskIdentifier bgTask;
    Sampler *sampler;
    CommunicationManager *communicationMgr;
}

@property (nonatomic, strong) IBOutlet UIWindow *window;
@property (nonatomic, strong) IBOutlet UITabBarController *tabBarController;

- (void) checkNetworkStatus:(NSNotification *)notice;
- (void) setupNotificationSubscriptions;
- (void) doSyncIfNeeded;

@end
