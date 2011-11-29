//
//  CaratAppDelegate.m
//  Carat
//
//  Created by Adam Oliner on 10/6/11.
//  Copyright 2011 UC Berkeley. All rights reserved.
//

#import "CaratAppDelegate.h"
#import "Reachability.h"
#import "UIDeviceProc.h"
#import <CoreData/CoreData.h>

#import "CurrentViewController.h"
#import "HogReportViewController.h"
#import "BugReportViewController.h"
#import "AboutViewController.h"

@implementation CaratAppDelegate

@synthesize window = _window;
@synthesize tabBarController = _tabBarController;

#pragma mark -
#pragma mark utility

void onUncaughtException(NSException *exception)
{
    NSLog(@"uncaught exception: %@", exception.description);
}

#pragma mark -
#pragma mark Application lifecycle

- (id) init {
    [super init];
    if (self != Nil) {
        communicationMgr = [[CommunicationManager alloc] init];
        sampler = [[Sampler alloc] initWithCommManager:communicationMgr];
    }
    return self;
}

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {    

    // UI
    self.window = [[UIWindow alloc] initWithFrame:[[UIScreen mainScreen] bounds]];
    UIViewController *viewController1, *viewController2, *viewController3, *viewController4;
    viewController1 = [[CurrentViewController alloc] initWithNibName:@"CurrentView" bundle:nil];
    viewController2 = [[HogReportViewController alloc] initWithNibName:@"HogReportView" bundle:nil];
    viewController3 = [[BugReportViewController alloc] initWithNibName:@"BugReportView" bundle:nil];
    viewController4 = [[AboutViewController alloc] initWithNibName:@"AboutView" bundle:nil];
    self.tabBarController = [[UITabBarController alloc] init];
    self.tabBarController.viewControllers = [NSArray arrayWithObjects:viewController1, viewController2, viewController3, viewController4, nil];
    self.window.rootViewController = self.tabBarController;
    [self.window makeKeyAndVisible];
    
    // Override point for customization after application launch.
    if (locationManager == nil && [CLLocationManager significantLocationChangeMonitoringAvailable]) {
        locationManager = [[CLLocationManager alloc] init];
        locationManager.delegate = self;
    }
    [self setupNotificationSubscriptions];
    
    // we do this to prompt the dialog asking for permission to share location info
    [locationManager startMonitoringSignificantLocationChanges];
    [locationManager stopMonitoringSignificantLocationChanges];
    
    // Everytime the CARAT app is launched, send a registration message.
    // 
    //Registration *dummy = [[Registration alloc] initWithUuId:[[Globals instance] getUUID] platformId:[UIDevice currentDevice].model systemVersion:[UIDevice currentDevice].systemVersion]; 
    Registration *registerMe = [[Registration alloc] init];
    registerMe.uuId = [[Globals instance] getUUID ];
    registerMe.platformId = [UIDevice currentDevice].model;
    registerMe.systemVersion = [UIDevice currentDevice].systemVersion;
    [communicationMgr sendRegistrationMessage:registerMe];
    
    // to help track down where exceptions are being raised
    NSSetUncaughtExceptionHandler(&onUncaughtException);
        
    return YES;
}


- (void)applicationWillResignActive:(UIApplication *)application {
    /*
     Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
     Use this method to pause ongoing tasks, disable timers, and throttle down OpenGL ES frame rates. Games should use this method to pause the game.
     */
}


- (void)applicationDidEnterBackground:(UIApplication *)application {
    /*
     Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later. 
     If your application supports background execution, called instead of applicationWillTerminate: when the user quits.
     */
    [locationManager startMonitoringSignificantLocationChanges];
}


- (void)applicationWillEnterForeground:(UIApplication *)application {
    /*
     Called as part of  transition from the background to the inactive state: here you can undo many of the changes made on entering the background.
     */
    [locationManager stopMonitoringSignificantLocationChanges];
}


- (void)applicationDidBecomeActive:(UIApplication *)application {
    /*
     Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
     */
    //[self doSample];
    //[sampler sampleNow];
    [sampler fetchAndSendSamples:10];
}


- (void)applicationWillTerminate:(UIApplication *)application {
    /*
     Called when the application is about to terminate.
     See also applicationDidEnterBackground:.
     */
}


#pragma mark -
#pragma mark UITabBarControllerDelegate methods

/*
// Optional UITabBarControllerDelegate method.
- (void)tabBarController:(UITabBarController *)tabBarController didSelectViewController:(UIViewController *)viewController {
}
*/

/*
// Optional UITabBarControllerDelegate method.
- (void)tabBarController:(UITabBarController *)tabBarController didEndCustomizingViewControllers:(NSArray *)viewControllers changed:(BOOL)changed {
}
*/

#pragma mark -
#pragma mark application logic methods

- (void)setupNotificationSubscriptions {
    // check settings to determine if location-change service is allowed
    // setup notifications for battery, location, etc.
    [[UIDevice currentDevice] setBatteryMonitoringEnabled:YES];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(batteryLevelChanged:) name:UIDeviceBatteryLevelDidChangeNotification object:nil];
	[[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(batteryStateChanged:) name:UIDeviceBatteryStateDidChangeNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(checkNetworkStatus:) name:kReachabilityChangedNotification object:nil];
    hostReachable = [Reachability reachabilityWithHostName: @"www.apple.com"];
    [hostReachable startNotifier];
}

- (void)batteryLevelChanged:(NSNotification *)notification {

}

- (void)batteryStateChanged:(NSNotification *)notification {

}


#pragma mark -
#pragma mark location awareness

- (void)locationManager:(CLLocationManager *)manager didUpdateToLocation:(CLLocation *)newLocation fromLocation:(CLLocation *)oldLocation
{
    // Do any prep work before sampling. Note that we may be in the background, so nothing heavy.
    
    //[self doSample];
}

- (void)locationManager:(CLLocationManager *)manager didFailWithError:(NSError *)error {
    NSString *errorData = [NSString stringWithFormat:@"%@",[error localizedDescription]];
    NSLog(@"%@", errorData);
}

       
#pragma mark -
#pragma mark networking methods
       
- (void) checkNetworkStatus:(NSNotification *)notice
{
    // called after network status changes
   
    NetworkStatus internetStatus = [hostReachable currentReachabilityStatus];
    if (internetStatus == ReachableViaWiFi || internetStatus == ReachableViaWWAN) {
        [self doSyncIfNeeded];
    }
}

- (void) doSyncIfNeeded {
    // we've already checked that the host we need to talk to is reachable
    // so see if there's enough data stored up to justify a sync
    // if so, do it!
}

#pragma mark -
#pragma mark Memory management

- (void)applicationDidReceiveMemoryWarning:(UIApplication *)application {
    /*
     Free up as much memory as possible by purging cached data objects that can be recreated (or reloaded from disk) later.
     */
}


- (void) dealloc {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
    [sampler release];
    [communicationMgr release];
}

@end

