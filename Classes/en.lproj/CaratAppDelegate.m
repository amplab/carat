//
//  CaratAppDelegate.m
//  Carat
//
//  Created by Adam Oliner on 10/6/11.
//  Copyright 2011 Stanford University. All rights reserved.
//

#import "CaratAppDelegate.h"
#import "Reachability.h"
#import "UIDeviceProc.h"
#import "AsyncSocket.h"
#import "CaratProtocol.pb.h"

@implementation CaratAppDelegate

@synthesize window;
@synthesize tabBarController;


#pragma mark -
#pragma mark Application lifecycle

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {    
    
    // Override point for customization after application launch.
    if (locationManager == nil && [CLLocationManager significantLocationChangeMonitoringAvailable]) {
        locationManager = [[CLLocationManager alloc] init];
        locationManager.delegate = self;
    }
    [self setupNotificationSubscriptions];

	// Set the tab bar controller as the window's root view controller and display.
    self.window.rootViewController = self.tabBarController;
    [self.window makeKeyAndVisible];

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
    [self doSample];
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
#pragma mark sampling methods

- (void)doSample {
    if ([UIApplication sharedApplication].applicationState == UIApplicationStateBackground)
    {
        [self doSampleBackground];
    }
    else
    {
        [self doSampleForeground];
    }
    
    AsyncSocket *asyncSocket = [[AsyncSocket alloc] initWithDelegate:self];
    NSError *nsError = nil;
    if (![asyncSocket connectToHost:@"127.0.0.1" onPort:8080 error:&nsError]) {
        NSLog(@"%@ %@", [nsError code], [nsError localizedDescription]);
    } else {
        NSLog(@"Connected!");
    }
    
    NSLog(@"Sending samples");
    
    Sample_Builder *sampleBuilder1 = [[Sample_Builder alloc] init];
    [sampleBuilder1 setProcessId: 1];
    [sampleBuilder1 setProcessName:@"Process1"];
    Sample *sample1 = [sampleBuilder1 build];
    
    Sample_Builder *sampleBuilder2 = [[Sample_Builder alloc] init];
    [sampleBuilder2 setProcessId: 2];
    [sampleBuilder2 setProcessName:@"Process2"];
    Sample *sample2 = [sampleBuilder2 build];
    
    Samples_Builder *samplesBuilder = [[Samples_Builder alloc] init];
    [samplesBuilder addSample:sample1];
    [samplesBuilder addSample:sample2];
    Samples *samples = [samplesBuilder build];
    NSData* nsData = [samples data];
    NSLog(@"Raw string is '%s' (length %d)\n", [nsData bytes], [nsData length]);
    [asyncSocket writeData:nsData withTimeout:-1 tag:10];
}

- (void)doSampleForeground {
    NSArray *processes = [[UIDevice currentDevice] runningProcesses];
    for (NSDictionary *dict in processes){
        NSLog(@"%@ - %@", [dict objectForKey:@"ProcessID"], [dict objectForKey:@"ProcessName"]);
    }
    
    NSLog(@"%@", [UIDevice currentDevice].name);
    NSLog(@"%@", [UIDevice currentDevice].model);
    NSLog(@"%@", [UIDevice currentDevice].systemName);
    NSLog(@"%@", [UIDevice currentDevice].systemVersion);
    
    if ([UIDevice currentDevice].batteryMonitoringEnabled) {
        NSLog(@"%f", [UIDevice currentDevice].batteryLevel);
        switch ([UIDevice currentDevice].batteryState) {
            case UIDeviceBatteryStateUnknown:
                NSLog(@"%@", @"Unknown");
                break;
            case UIDeviceBatteryStateUnplugged:
                NSLog(@"%@", @"Unplugged");
                break;
            case UIDeviceBatteryStateCharging:
                NSLog(@"%@", @"Charging");
                break;
            case UIDeviceBatteryStateFull:
                NSLog(@"%@", @"Full");
                break;
            default:
                break;
        }
    }

    
}

- (void)doSampleBackground {
    // REMEMBER. We are running in the background if this is being executed.
    // We can't assume normal network access.
    // bgTask is defined as an instance variable of type UIBackgroundTaskIdentifier
    
    // Note that the expiration handler block simply ends the task. It is important that we always
    // end tasks that we have started.
    
    bgTask = [[UIApplication sharedApplication]
              beginBackgroundTaskWithExpirationHandler:^{
                  [[UIApplication sharedApplication] endBackgroundTask:bgTask];
              }];
                  
    // ANY CODE WE PUT HERE IS OUR BACKGROUND TASK
    // For example, I can do a series of SYNCHRONOUS network methods
    // (we're in the background, there is
    // no UI to block so synchronous is the correct approach here).

    // ...

    // AFTER ALL THE UPDATES, close the task

    if (bgTask != UIBackgroundTaskInvalid)
    {
      [[UIApplication sharedApplication] endBackgroundTask:bgTask];
       bgTask = UIBackgroundTaskInvalid;
    }
}


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
    
    [self doSample];
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


- (void)dealloc {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

@end

