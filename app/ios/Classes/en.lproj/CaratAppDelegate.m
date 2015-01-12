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
#import "Flurry.h"
#import "Utilities.h"
#import "CoreDataManager.h"

#import "ActionViewController.h"
#import "CurrentViewController.h"
#import "HogReportViewController.h"
#import "BugReportViewController.h"
#import "ConsentViewController.h"
#import "AboutViewController.h"
#import <Socialize/Socialize.h>

@implementation CaratAppDelegate

@synthesize window = _window;
@synthesize tabBarController = _tabBarController;

#pragma mark -
#pragma mark utility

void onUncaughtException(NSException *exception)
{
    [Flurry logError:@"Uncaught" message:[[exception callStackSymbols] componentsJoinedByString:@"\n"] exception:exception];
    NSLog(@"uncaught exception: %@", exception.description);
}

#pragma mark - notifications

- (void)scheduleNotificationAfterInterval:(int)interval {
    [[UIApplication sharedApplication] cancelAllLocalNotifications];
    if (interval <= 0) { interval = 432000; } // 5 days
    //if (interval <= 0) { interval = 10; }
    
    UILocalNotification *localNotif = [[UILocalNotification alloc] init];
    if (localNotif == nil)
        return;
    localNotif.fireDate = [[NSDate date] dateByAddingTimeInterval:interval];
    localNotif.timeZone = [NSTimeZone defaultTimeZone];
    
    localNotif.alertBody = @"Carat may have discovered new battery-saving actions for you. Why don't you take a look?";
    localNotif.alertAction = NSLocalizedString(@"Launch Carat", nil);
    localNotif.repeatInterval = 0;
    
    [[UIApplication sharedApplication] scheduleLocalNotification:localNotif];
    [localNotif release];
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
    if ([[[UIDevice currentDevice] systemVersion] floatValue] >= 7) {
        [application setStatusBarStyle:UIStatusBarStyleLightContent];
        self.window.clipsToBounds =YES;
        self.window.frame =  CGRectMake(0,20,self.window.frame.size.width,self.window.frame.size.height-20);
    }
    // test for consent
    if ([[Globals instance] hasUserConsented]) return [self proceedWithConsent];
    else return [self acquireConsentWithCallbackTarget:self
                                          withSelector:@selector(proceedWithConsent)];
}

- (BOOL)acquireConsentWithCallbackTarget:(CaratAppDelegate *)delegate withSelector:(SEL)selector {
    // UI
    self.window = [[[UIWindow alloc] initWithFrame:[[UIScreen mainScreen] bounds]] autorelease];
    UIViewController *viewController = [[ConsentViewController alloc] initWithNibName:@"ConsentView" bundle:nil callbackTo:delegate withSelector:selector];
    self.window.rootViewController = viewController;
    [self.window makeKeyAndVisible];
    [viewController release];
    
    // to help track down where exceptions are being raised
    NSSetUncaughtExceptionHandler(&onUncaughtException);
    
    return YES;
}


// called when the user has accepted the EULA
- (BOOL)proceedWithConsent {
    DLog(@"Proceeding with consent");
    if (self.window == nil) self.window = [[[UIWindow alloc] initWithFrame:[[UIScreen mainScreen] bounds]] autorelease];
    UIViewController *viewController0, *viewController1, *viewController2, *viewController3, *viewController4;

    UINavigationController *navController0, *navController1, *navController2, *navController3;
    viewController0 = [[ActionViewController alloc] initWithNibName:@"ActionView" bundle:nil];
    navController0 = [[UINavigationController alloc] initWithRootViewController:viewController0];
    navController0.navigationBar.translucent = NO;
    navController0.navigationBarHidden = YES;
    viewController1 = [[CurrentViewController alloc] initWithNibName:@"CurrentView" bundle:nil];
    navController1 = [[UINavigationController alloc] initWithRootViewController:viewController1];
    navController1.navigationBar.translucent = NO;
    navController1.navigationBarHidden = YES;
    viewController2 = [[HogReportViewController alloc] initWithNibName:@"ReportView" bundle:nil];
    navController2 = [[UINavigationController alloc] initWithRootViewController:viewController2];
     navController2.navigationBar.translucent = NO;
    navController2.navigationBarHidden = YES;
    viewController3 = [[BugReportViewController alloc] initWithNibName:@"ReportView" bundle:nil];
    navController3 = [[UINavigationController alloc] initWithRootViewController:viewController3];
    navController3.navigationBar.translucent = NO;
    navController3.navigationBarHidden = YES;
    viewController4 = [[AboutViewController alloc] initWithNibName:@"AboutView" bundle:nil];
    self.tabBarController = [[[UITabBarController alloc] init] autorelease];
   self.tabBarController.tabBar.translucent = NO;
    self.tabBarController.viewControllers = [NSArray arrayWithObjects:navController0, navController1, navController2, navController3, viewController4, nil];



    self.window.rootViewController = self.tabBarController;
    [self.window makeKeyAndVisible];
    DLog(@"Set root view controller; is nil? %@",
         self.tabBarController==nil ? @"yes" : @"no");
    // Fixme: What is this? Not used.
	//UIView *view = [[UIView alloc] initWithFrame:CGRectMake(0, 0 , 20 ,20)];

	 // views have been added to hierarchy, so they can be released
    [viewController0 release];
    [viewController1 release];
    [viewController2 release];
    [viewController3 release];
    [viewController4 release];
    [navController0 release];
    [navController1 release];
    [navController2 release];
    [navController3 release];
    
    // Override point for customization after application launch.
    if (locationManager == nil && [CLLocationManager significantLocationChangeMonitoringAvailable]) {
        locationManager = [[CLLocationManager alloc] init];
        locationManager.delegate = self;
    }
    
    // we do this to prompt the dialog asking for permission to share location info
    [locationManager startMonitoringSignificantLocationChanges];
    [locationManager stopMonitoringSignificantLocationChanges];
        
    // idempotent setup of notifications; also called in willResignActive
    [self setupNotificationSubscriptions];
    [[UIApplication sharedApplication] cancelAllLocalNotifications]; // so nothing fires while we're active
    
    // Everytime the CARAT app is launched, we will send a registration message. 
    // Right at this point, we are unsure if there is network connectivity, so 
    // save the message in core data. 
    [[CoreDataManager instance] generateSaveRegistration];
    
    // Analytics
    [Flurry startSession:@"4XITISYNWHTBTL4E533E"];
    [Flurry logAllPageViewsForTarget:self.tabBarController];
    [Flurry logAllPageViewsForTarget:navController0];
    [Flurry logAllPageViewsForTarget:navController1];
    [Flurry logAllPageViewsForTarget:navController2];
    [Flurry logAllPageViewsForTarget:navController3];
    [Flurry setUserID:[[Globals instance] getUUID]];
    
    // set the socialize api key and secret, app registered here: http://www.getsocialize.com/apps/
    [Socialize storeConsumerKey:@"8d0ddf53-fac1-48b1-ab25-b8c819455124"];
    [Socialize storeConsumerSecret:@"a043bea4-b4c0-432d-a007-5b74207d184e"];
    [SZFacebookUtils setAppId:@"258193747569113"];
    [SZTwitterUtils setConsumerKey:@"JkGB6jsTAAfitYT6xASvxA" consumerSecret:@"crj455prhsA22L48VmxH2aEimR8Rmi0yYJgoclQpwPw"];
    
    [[CoreDataManager instance] sampleNow:@"applicationDidBecomeActive"];
    
    // to help track down where exceptions are being raised
    NSSetUncaughtExceptionHandler(&onUncaughtException);
    
    return YES;
}

- (void)applicationWillResignActive:(UIApplication *)application {
    if ([[Globals instance] hasUserConsented]) {
        [self setupNotificationSubscriptions];
        [self scheduleNotificationAfterInterval:-1]; // uses default of 5 days
    }
}


- (void)applicationDidEnterBackground:(UIApplication *)application {
    /*
     Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later. 
     If your application supports background execution, called instead of applicationWillTerminate: when the user quits.
     */
    if ([[Globals instance] hasUserConsented]) [locationManager startMonitoringSignificantLocationChanges];
}


- (void)applicationWillEnterForeground:(UIApplication *)application {
    /*
     Called as part of  transition from the background to the inactive state: here you can undo many of the changes made on entering the background.
     */
    if ([[Globals instance] hasUserConsented]) [locationManager stopMonitoringSignificantLocationChanges];
}


- (void)applicationDidBecomeActive:(UIApplication *)application {
    /*
     Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
     */
}


- (void)applicationWillTerminate:(UIApplication *)application {
    /*
     Called when the application is about to terminate.
     See also applicationDidEnterBackground:.
     */
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

#pragma mark -
#pragma mark Facebook Connect methods

- (BOOL)application:(UIApplication *)application 
            openURL:(NSURL *)url 
  sourceApplication:(NSString *)sourceApplication 
         annotation:(id)annotation 
{
    return [Socialize handleOpenURL:url];
}

- (BOOL)application:(UIApplication *)application 
      handleOpenURL:(NSURL *)url 
{
    return [Socialize handleOpenURL:url];
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
}

- (void)batteryLevelChanged:(NSNotification *)notification {
    [[CoreDataManager instance] sampleNow:@"batteryLevelChanged"];
}

- (void)batteryStateChanged:(NSNotification *)notification {
    [[CoreDataManager instance] sampleNow:@"batteryStateChanged"];
}

- (void)application:(UIApplication *)application 
didReceiveLocalNotification:(UILocalNotification *)notification {
    [[CoreDataManager instance] sampleNow:@"didReceiveLocalNotification"];
}

#pragma mark -
#pragma mark location awareness

- (void)locationManager:(CLLocationManager *)manager 
    didUpdateToLocation:(CLLocation *)newLocation 
           fromLocation:(CLLocation *)oldLocation
{
    [Flurry setLatitude:newLocation.coordinate.latitude 
                       longitude:newLocation.coordinate.longitude 
              horizontalAccuracy:newLocation.horizontalAccuracy            
                verticalAccuracy:newLocation.verticalAccuracy]; 
    
    // Do any prep work before sampling. Note that we may be in the background, so nothing heavy.
    [(Globals *)[Globals instance] setDistanceTraveled:[newLocation distanceFromLocation:oldLocation]];
    [[CoreDataManager instance] sampleNow:@"didUpdateToLocation"];
}

- (void)locationManager:(CLLocationManager *)manager didFailWithError:(NSError *)error {
    DLog(@"%@", [NSString stringWithFormat:@"%@",[error localizedDescription]]);
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
    [super dealloc];
}

@end

