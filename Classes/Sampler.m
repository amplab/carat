//
//  Sampler.m
//  Carat
//
//  Handles the sampling. Does sampling (foreground & background) and stores
//  them in core data. This is also the controller for core data.
//
//  Created by Anand Padmanabha Iyer on 11/5/11.
//  Copyright (c) 2011 UC Berkeley. All rights reserved.
//

#import "Sampler.h"

@implementation Sampler

@synthesize managedObjectContext = __managedObjectContext;
@synthesize managedObjectModel = __managedObjectModel;
@synthesize persistentStoreCoordinator = __persistentStoreCoordinator;
@synthesize fetchedResultsController = __fetchResultsController;

- (id) initWithCommManager:(id)cManager 
{
    self = [super init];
    commManager = [cManager retain];
    return self;
}

- (void)dealloc
{
    [__managedObjectContext release];
    [__managedObjectModel release];
    [__persistentStoreCoordinator release];
    [__fetchResultsController release];
    [commManager release];
    [super dealloc];
}

//
//  Get the list of running processes and put it in core data. Note that we 
//  don't call save on the managed object here, as we will do a final call to 
//  save the entire sample.
//
- (void) sampleProcessInfo : (CoreDataSample *) currentCDSample
{
    NSManagedObjectContext *managedObjectContext = self.managedObjectContext;
    
    if (managedObjectContext != nil)
    {
        NSArray *processes = [[UIDevice currentDevice] runningProcesses];
        
        for (NSDictionary *dict in processes)
        {
            NSLog(@"%@ - %@", [dict objectForKey:@"ProcessID"], [dict objectForKey:@"ProcessName"]);
            CoreDataProcessInfo *cdataProcessInfo = (CoreDataProcessInfo *) [NSEntityDescription insertNewObjectForEntityForName:@"CoreDataProcessInfo" inManagedObjectContext:managedObjectContext];
            [cdataProcessInfo setId: [NSNumber numberWithInt:[[dict objectForKey:@"ProcessID"] intValue]]];
            [cdataProcessInfo setName:[dict objectForKey:@"ProcessName"]];
            [cdataProcessInfo setCoredatasample:currentCDSample];
            [currentCDSample addProcessInfosObject:cdataProcessInfo];
        }
    }
}

//
//  Do a sampling while the app is in the foreground. There shouldn't be any
//  restrictions on CPU usage as we are active.
//
- (void) sampleForeground 
{
    NSError *error = nil;
    NSManagedObjectContext *managedObjectContext = self.managedObjectContext;
    
    CoreDataSample *cdataSample = (CoreDataSample *) [NSEntityDescription insertNewObjectForEntityForName:@"CoreDataSample" 
                                                                                   inManagedObjectContext:managedObjectContext];
    [cdataSample setTimestamp:[NSDate date]];
    
    //
    //  Running processes.
    //
    [self sampleProcessInfo:cdataSample];
    
    //
    //  Battery state and level.
    //
    if ([UIDevice currentDevice].batteryMonitoringEnabled) 
    {
        NSLog(@"%f", [UIDevice currentDevice].batteryLevel);
        [cdataSample setBatteryLevel:[NSNumber numberWithFloat:[UIDevice currentDevice].batteryLevel]];
        
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
        [cdataSample setBatteryState:[NSNumber numberWithInt:[UIDevice currentDevice].batteryState]];
    }
    
    //
    //  Now save the sample.
    //
    if ([managedObjectContext hasChanges] && ![managedObjectContext save:&error])
    {
        /*
         Replace this implementation with code to handle the error appropriately.
     
         abort() causes the application to generate a crash log and terminate. You should not use this function in a shipping application, although it may be useful during development. 
         */
        NSLog(@"Unresolved error %@, %@", error, [error userInfo]);
        abort();
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

//
//  Retrieve and send some samples to the server.
//
- (void) fetchAndSendSamples : (NSUInteger) limitSamplesTo
{
    NSError *error = nil;
    NSManagedObjectContext *managedObjectContext = self.managedObjectContext;
    
    if (managedObjectContext != nil) 
    {
        NSFetchRequest *fetchRequest = [[[NSFetchRequest alloc] init] autorelease];
        NSSortDescriptor *sortDescriptor = [[NSSortDescriptor alloc] initWithKey:@"timestamp" 
                                                                       ascending:YES];
        NSArray *sortDescriptors = [[NSArray alloc] initWithObjects:sortDescriptor, nil];
        [fetchRequest setSortDescriptors:sortDescriptors];
        
        NSEntityDescription *entity = [NSEntityDescription entityForName:@"CoreDataSample" 
                                                  inManagedObjectContext:managedObjectContext];
        [fetchRequest setEntity:entity];
        [fetchRequest setFetchLimit:limitSamplesTo];  
        
        NSArray *fetchedObjects = [managedObjectContext executeFetchRequest:fetchRequest error:&error];
        if (fetchedObjects == nil) {
            NSLog(@"Could not fetch samples, error %@, %@", error, [error userInfo]);
            goto cleanup;
        } 
        
        NSLog(@"Number of samples fetched: %u", [fetchedObjects count]);
        
        for (CoreDataSample *sample in fetchedObjects)
        {
            //CoreDataSample *sample = (CoreDataSample *)  obj;
            if (sample == nil) 
                break;
            
            Sample* sampleToSend = [[Sample alloc] init];
            sampleToSend.uuId = [[Globals instance] getUUID ];
            NSMutableArray *pInfoList = [[NSMutableArray alloc] init];
            sampleToSend.piList = pInfoList;
            
            NSLog(@"timestamp: %@", [sample valueForKey:@"timestamp"]);
            NSLog(@"batteryLevel: %@", [sample valueForKey:@"batteryLevel"]);
            NSLog(@"batteryState: %@", [sample valueForKey:@"batteryState"]);
            
            //
            //  Get all the process info objects for this sample.
            //
            NSSet *processInfosSet = sample.processInfos;
            NSArray *processInfoArray = [processInfosSet allObjects];
            
            for (CoreDataProcessInfo *processInfo in processInfoArray)
            {
                ProcessInfo *pInfo = [[ProcessInfo alloc] init];
                pInfo.pId = (int)[processInfo valueForKey:@"id"];
                pInfo.pName = (NSString *)[processInfo valueForKey:@"name"];
                [pInfoList addObject:pInfo];
                NSLog(@"Id: %@", [processInfo valueForKey:@"id"]);
                NSLog(@"Name: %@", [processInfo valueForKey:@"name"]);
            }
            
            //
            //  Try to send. If successful, delete. Note that the process info
            //  is cascaded with sample deletion.
            //
            BOOL ret = [commManager sendSample:sampleToSend];
            if (ret == YES) 
            {
                [managedObjectContext deleteObject:sample];
            }
        }
        
    cleanup:
        [sortDescriptors release];
        [sortDescriptor release];
        if ([managedObjectContext hasChanges] && ![managedObjectContext save:&error])
        {
            /*
             Replace this implementation with code to handle the error appropriately.
             
             abort() causes the application to generate a crash log and terminate. You should not use this function in a shipping application, although it may be useful during development. 
             */
            NSLog(@"Unresolved error %@, %@", error, [error userInfo]);
            abort();
        } 
    }
}

#pragma mark - Core Data stack

/**
 Returns the managed object context for the application.
 If the context doesn't already exist, it is created and bound to the persistent store coordinator for the application.
 */
- (NSManagedObjectContext *)managedObjectContext
{
    if (__managedObjectContext != nil)
    {
        return __managedObjectContext;
    }
    
    NSPersistentStoreCoordinator *coordinator = [self persistentStoreCoordinator];
    if (coordinator != nil)
    {
        __managedObjectContext = [[NSManagedObjectContext alloc] init];
        [__managedObjectContext setPersistentStoreCoordinator:coordinator];
    }
    return __managedObjectContext;
}

/**
 Returns the managed object model for the application.
 If the model doesn't already exist, it is created from the application's model.
 */
- (NSManagedObjectModel *)managedObjectModel
{
    if (__managedObjectModel != nil)
    {
        return __managedObjectModel;
    }
    NSURL *modelURL = [[NSBundle mainBundle] URLForResource:@"Carat" withExtension:@"momd"];
    __managedObjectModel = [[NSManagedObjectModel alloc] initWithContentsOfURL:modelURL];
    return __managedObjectModel;
}

/**
 Returns the persistent store coordinator for the application.
 If the coordinator doesn't already exist, it is created and the application's store added to it.
 */
- (NSPersistentStoreCoordinator *)persistentStoreCoordinator
{
    if (__persistentStoreCoordinator != nil)
    {
        return __persistentStoreCoordinator;
    }
    
    NSURL *storeURL = [[self applicationDocumentsDirectory] URLByAppendingPathComponent:@"Carat.sqlite"];
    
    NSError *error = nil;
    __persistentStoreCoordinator = [[NSPersistentStoreCoordinator alloc] initWithManagedObjectModel:[self managedObjectModel]];
    if (![__persistentStoreCoordinator addPersistentStoreWithType:NSSQLiteStoreType configuration:nil URL:storeURL options:nil error:&error])
    {
        /*
         Replace this implementation with code to handle the error appropriately.
         
         abort() causes the application to generate a crash log and terminate. You should not use this function in a shipping application, although it may be useful during development. 
         
         Typical reasons for an error here include:
         * The persistent store is not accessible;
         * The schema for the persistent store is incompatible with current managed object model.
         Check the error message to determine what the actual problem was.
         
         
         If the persistent store is not accessible, there is typically something wrong with the file path. Often, a file URL is pointing into the application's resources directory instead of a writeable directory.
         
         If you encounter schema incompatibility errors during development, you can reduce their frequency by:
         * Simply deleting the existing store:
         [[NSFileManager defaultManager] removeItemAtURL:storeURL error:nil]
         
         * Performing automatic lightweight migration by passing the following dictionary as the options parameter: 
         [NSDictionary dictionaryWithObjectsAndKeys:[NSNumber numberWithBool:YES], NSMigratePersistentStoresAutomaticallyOption, [NSNumber numberWithBool:YES], NSInferMappingModelAutomaticallyOption, nil];
         
         Lightweight migration will only work for a limited set of schema changes; consult "Core Data Model Versioning and Data Migration Programming Guide" for details.
         
         */
        NSLog(@"Unresolved error %@, %@", error, [error userInfo]);
        abort();
    }    
    
    return __persistentStoreCoordinator;
}

#pragma mark - Application's Documents directory

/**
 Returns the URL to the application's Documents directory.
 */
- (NSURL *)applicationDocumentsDirectory
{
    return [[[NSFileManager defaultManager] URLsForDirectory:NSDocumentDirectory inDomains:NSUserDomainMask] lastObject];
}

@end
