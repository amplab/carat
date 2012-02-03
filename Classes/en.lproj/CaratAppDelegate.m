//
//  CaratAppDelegate.m
//  Carat
//
//  Created by Adam Oliner on 10/6/11.
//  Copyright 2011 UC Berkeley. All rights reserved.
//

#import "CaratAppDelegate.h"
#import "UIDeviceProc.h"
#import <CoreData/CoreData.h>
#import "FlurryAnalytics.h"
#import "SHK.h"
#import "Utilities.h"

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
    [FlurryAnalytics logError:@"Uncaught" message:@"Crash!" exception:exception];
    NSLog(@"uncaught exception: %@", exception.description);
}

#pragma mark -
#pragma mark Application lifecycle

- (id) init {
    if (self = [super init]) {
        // custom init code
    }
    return self;
}

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {    
   
    // UI
    self.window = [[[UIWindow alloc] initWithFrame:[[UIScreen mainScreen] bounds]] autorelease];
    UIViewController *viewController1, *viewController2, *viewController3, *viewController4;
    UINavigationController *navController1, *navController2, *navController3;
    viewController1 = [[CurrentViewController alloc] initWithNibName:@"CurrentView" bundle:nil];
    navController1 = [[UINavigationController alloc] initWithRootViewController:viewController1];
    navController1.navigationBarHidden = YES;
    viewController2 = [[HogReportViewController alloc] initWithNibName:@"ReportView" bundle:nil];
    navController2 = [[UINavigationController alloc] initWithRootViewController:viewController2];
    navController2.navigationBarHidden = YES;
    viewController3 = [[BugReportViewController alloc] initWithNibName:@"ReportView" bundle:nil];
    navController3 = [[UINavigationController alloc] initWithRootViewController:viewController3];
    navController3.navigationBarHidden = YES;
    viewController4 = [[AboutViewController alloc] initWithNibName:@"AboutView" bundle:nil];
    self.tabBarController = [[[UITabBarController alloc] init] autorelease];
    self.tabBarController.viewControllers = [NSArray arrayWithObjects:navController1, navController2, navController3, viewController4, nil];
    self.window.rootViewController = self.tabBarController;
    [self.window makeKeyAndVisible];
    
    // views have been added to hierarchy, so they can be released
    [viewController1 release];
    [viewController2 release];
    [viewController3 release];
    [viewController4 release];
    [navController1 release];
    [navController2 release];
    [navController3 release];

    // Override point for customization after application launch.
    if (locationManager == nil && [CLLocationManager significantLocationChangeMonitoringAvailable]) {
        locationManager = [[CLLocationManager alloc] init];
        locationManager.delegate = self;
    }
    
    // idempotent setup of notifications; also called in willResignActive
    [self setupNotificationSubscriptions];
    
    // we do this to prompt the dialog asking for permission to share location info
    [locationManager startMonitoringSignificantLocationChanges];
    [locationManager stopMonitoringSignificantLocationChanges];
    
    // Everytime the CARAT app is launched, we will send a registration message. 
    // Right at this point, we are unsure if there is network connectivity, so 
    // save the message in core data. 
    [[Sampler instance] generateSaveRegistration];
    
    // Analytics
    [FlurryAnalytics startSession:@"4XITISYNWHTBTL4E533E"];
    [FlurryAnalytics logAllPageViews:self.tabBarController];
    [FlurryAnalytics logAllPageViews:navController1];
    [FlurryAnalytics logAllPageViews:navController2];
    [FlurryAnalytics logAllPageViews:navController3];
    [FlurryAnalytics setUserID:[[Globals instance] getUUID]];
    
    // flush any sharing actions that were performed offline and cached
    [SHK flushOfflineQueue];
    
    // to help track down where exceptions are being raised
    NSSetUncaughtExceptionHandler(&onUncaughtException);
        
    return YES;
}


- (void)applicationWillResignActive:(UIApplication *)application {
    [self setupNotificationSubscriptions]; // we aren't seeing any battery events, and I don't know why
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
    [[Sampler instance] sampleNow:@"applicationDidBecomeActive"];
}


- (void)applicationWillTerminate:(UIApplication *)application {
    /*
     Called when the application is about to terminate.
     See also applicationDidEnterBackground:.
     */
    [[NSNotificationCenter defaultCenter] removeObserver:self];
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
    [[NSNotificationCenter defaultCenter] addObserver:self 
                                             selector:@selector(batteryLevelChanged:) 
                                                 name:UIDeviceBatteryLevelDidChangeNotification 
                                               object:nil];
	[[NSNotificationCenter defaultCenter] addObserver:self 
                                             selector:@selector(batteryStateChanged:) 
                                                 name:UIDeviceBatteryStateDidChangeNotification 
                                               object:nil];
    //[[NSNotificationCenter defaultCenter] addObserver:self 
    //                                         selector:@selector(checkNetworkStatus:) 
    //                                             name:kReachabilityChangedNotification 
    //                                           object:nil];
    //hostReachable = [Reachability reachabilityWithHostName: @"www.apple.com"];
    //[hostReachable startNotifier];
}

- (void)batteryLevelChanged:(NSNotification *)notification {
    [[Sampler instance] sampleNow:@"batteryLevelChanged"];
}

- (void)batteryStateChanged:(NSNotification *)notification {
    [[Sampler instance] sampleNow:@"batteryStateChanged"];
}

- (void)application:(UIApplication *)application 
didReceiveLocalNotification:(UILocalNotification *)notification {
    [[Sampler instance] sampleNow:@"didReceiveLocalNotification"];
}

#pragma mark -
#pragma mark location awareness

- (void)locationManager:(CLLocationManager *)manager didUpdateToLocation:(CLLocation *)newLocation fromLocation:(CLLocation *)oldLocation
{
    [FlurryAnalytics setLatitude:newLocation.coordinate.latitude longitude:newLocation.coordinate.longitude horizontalAccuracy:newLocation.horizontalAccuracy            verticalAccuracy:newLocation.verticalAccuracy]; 
    // Do any prep work before sampling. Note that we may be in the background, so nothing heavy.
    
    [[Sampler instance] sampleNow:@"didUpdateToLocation"];
}

- (void)locationManager:(CLLocationManager *)manager didFailWithError:(NSError *)error {
    NSString *errorData = [NSString stringWithFormat:@"%@",[error localizedDescription]];
    DLog(@"%@", errorData);
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
}

@end

