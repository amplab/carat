//
//  Sampler.m
//  Carat
//
//  Handles the sampling. Does sampling (foreground & background) and stores
//  them in SQLite database. 
//
//  Created by Anand Padmanabha Iyer on 11/5/11.
//  Copyright (c) 2011 UC Berkeley. All rights reserved.
//

#import "Sampler.h"

@implementation Sampler

- (void) sampleForeground 
{
    NSArray *processes = [[UIDevice currentDevice] runningProcesses];
    for (NSDictionary *dict in processes) 
    {
        NSLog(@"%@ - %@", [dict objectForKey:@"ProcessID"], [dict objectForKey:@"ProcessName"]);
    }
    
    NSLog(@"%@", [UIDevice currentDevice].name);
    NSLog(@"%@", [UIDevice currentDevice].model);
    NSLog(@"%@", [UIDevice currentDevice].systemName);
    NSLog(@"%@", [UIDevice currentDevice].systemVersion);
    
    if ([UIDevice currentDevice].batteryMonitoringEnabled) 
    {
        NSLog(@"%f", [UIDevice currentDevice].batteryLevel);
        switch ([UIDevice currentDevice].batteryState) 
        {
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

- (void) sampleBackground 
{
    // REMEMBER. We are running in the background if this is being executed.
    // We can't assume normal network access.
    // bgTask is defined as an instance variable of type UIBackgroundTaskIdentifier
    
    // Note that the expiration handler block simply ends the task. It is important that we always
    // end tasks that we have started.
    
    UIBackgroundTaskIdentifier bgTask = [[UIApplication sharedApplication]
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


- (void) sampleNow 
{
    if ([UIApplication sharedApplication].applicationState == UIApplicationStateBackground)
    {
        [self sampleBackground];
    }
    else
    {
        [self sampleForeground];
    }
}

@end
