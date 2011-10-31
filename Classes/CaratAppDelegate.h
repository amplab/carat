//
//  CaratAppDelegate.h
//  Carat
//
//  Created by Adam Oliner on 10/6/11.
//  Copyright 2011 Stanford University. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <CoreLocation/CoreLocation.h>

@class Reachability;

@interface CaratAppDelegate : NSObject <UIApplicationDelegate, UITabBarControllerDelegate, CLLocationManagerDelegate> {
    UIWindow *window;
    UITabBarController *tabBarController;
    CLLocationManager *locationManager;
    Reachability *hostReachable;
    UIBackgroundTaskIdentifier bgTask;
}

@property (nonatomic, strong) IBOutlet UIWindow *window;
@property (nonatomic, strong) IBOutlet UITabBarController *tabBarController;
@property CFUUIDRef *uuid;

- (void) checkNetworkStatus:(NSNotification *)notice;
- (void) setupNotificationSubscriptions;
- (void) doSample;
- (void) doSampleBackground;
- (void) doSampleForeground;
- (void) doSyncIfNeeded;

@end
