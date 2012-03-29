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
#import "CoreDataManager.h"
#import "Globals.h"
#import "CommunicationManager.h"
#import "Thrift/transport/TSocketClient.h"
#import "Thrift/protocol/TBinaryProtocol.h"

@interface CaratAppDelegate : UIResponder <UIApplicationDelegate, UITabBarControllerDelegate, CLLocationManagerDelegate> {
    UIWindow *window;
    UITabBarController *tabBarController;
    CLLocationManager *locationManager;
    UIBackgroundTaskIdentifier bgTask;
}

@property (strong, nonatomic) UIWindow *window;
@property (strong, nonatomic) UITabBarController *tabBarController;

- (BOOL)proceedWithConsent;
- (BOOL)acquireConsentWithCallbackTarget:(CaratAppDelegate *)delegate withSelector:(SEL)selector;
- (void) setupNotificationSubscriptions;

@end
