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
#import "FlurryAnalytics.h"
#import "UIDeviceHardware.h"

@implementation Sampler

@synthesize managedObjectContext = __managedObjectContext;
@synthesize managedObjectModel = __managedObjectModel;
@synthesize persistentStoreCoordinator = __persistentStoreCoordinator;
@synthesize fetchedResultsController = __fetchResultsController;

static id instance = nil;

+ (void) initialize {
    if (self == [Sampler class]) {
        instance = [[self alloc] init];
    }
}

+ (id) instance {
    return instance;
}

- (id) initWithCommManager:(id)cManager 
{
    self = [super init];
//    commManager = [cManager retain];
    return self;
}

- (void) dealloc
{
    [__managedObjectContext release];
    [__managedObjectModel release];
    [__persistentStoreCoordinator release];
    [__fetchResultsController release];
  //  [commManager release];
    [super dealloc];
}

//
//  Get information about the memory.
//
- (void) printMemoryInfo
{
    int pagesize = [[UIDevice currentDevice] pageSize];
    NSLog(@"Memory Info");
    NSLog(@"-----------");
    NSLog(@"Page size = %d bytes", pagesize);
    
    mach_msg_type_number_t count = HOST_VM_INFO_COUNT;
    
    vm_statistics_data_t vmstat;
    if (host_statistics(mach_host_self(), HOST_VM_INFO, (host_info_t)&vmstat, &count) != KERN_SUCCESS)
    {
        NSLog(@"Failed to get VM statistics.");
    }
    
    double total = vmstat.wire_count + vmstat.active_count + vmstat.inactive_count + vmstat.free_count;
    double wired = vmstat.wire_count / total;
    double active = vmstat.active_count / total;
    double inactive = vmstat.inactive_count / total;
    double free = vmstat.free_count / total;
    
    NSLog(@"Total =    %8d pages", vmstat.wire_count + vmstat.active_count + vmstat.inactive_count + vmstat.free_count);
    
    NSLog(@"Wired =    %8d bytes", vmstat.wire_count * pagesize);
    NSLog(@"Active =   %8d bytes", vmstat.active_count * pagesize);
    NSLog(@"Inactive = %8d bytes", vmstat.inactive_count * pagesize);
    NSLog(@"Free =     %8d bytes", vmstat.free_count * pagesize);
    
    NSLog(@"Total =    %8d bytes", (vmstat.wire_count + vmstat.active_count + vmstat.inactive_count + vmstat.free_count) * pagesize);
    
    NSLog(@"Wired =    %0.2f %%", wired * 100.0);
    NSLog(@"Active =   %0.2f %%", active * 100.0);
    NSLog(@"Inactive = %0.2f %%", inactive * 100.0);
    NSLog(@"Free =     %0.2f %%", free * 100.0);
    
    NSLog(@"Physical memory = %8d bytes", [[UIDevice currentDevice] totalMemory]);
    NSLog(@"User memory =     %8d bytes", [[UIDevice currentDevice] userMemory]);
}

- (void) printProcessorInfo
{
    NSLog(@"Processor Info");
    NSLog(@"--------------");
    NSLog(@"CPU Frequency = %d hz", [[UIDevice currentDevice] cpuFrequency]);
    NSLog(@"Bus Frequency = %d hz", [[UIDevice currentDevice] busFrequency]);
}

//
// Generate and save registration information in core data.
//
- (void) generateSaveRegistration 
{
    NSError *error = nil;
    NSManagedObjectContext *managedObjectContext = self.managedObjectContext;
    
    //
    // Store registration information.
    //
    CoreDataRegistration *cdataRegistration = (CoreDataRegistration *) [NSEntityDescription 
                                                                        insertNewObjectForEntityForName:@"CoreDataRegistration" 
                                                                        inManagedObjectContext:managedObjectContext];
    [cdataRegistration setTimestamp:[NSNumber numberWithDouble:[[Globals instance] utcSecondsSinceEpoch]]];
    
    UIDeviceHardware *h =[[UIDeviceHardware alloc] init];
    [cdataRegistration setPlatformId:[h platformString]];
    NSLog([h platformString]);
    [h release];
    
    [cdataRegistration setSystemVersion:[UIDevice currentDevice].systemVersion];
    
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
            //NSLog(@"%@ - %@", [dict objectForKey:@"ProcessID"], [dict objectForKey:@"ProcessName"]);
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
- (void) sampleForeground : (NSString *) triggeredBy
{
    NSError *error = nil;
    NSManagedObjectContext *managedObjectContext = self.managedObjectContext;
    
    CoreDataSample *cdataSample = (CoreDataSample *) [NSEntityDescription insertNewObjectForEntityForName:@"CoreDataSample" 
                                                                                   inManagedObjectContext:managedObjectContext];
    [cdataSample setTriggeredBy:triggeredBy];
    [cdataSample setTimestamp:[NSNumber numberWithDouble:[[Globals instance] utcSecondsSinceEpoch]]];
    
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
        
        NSString* batteryStateString = @"None";
        switch ([UIDevice currentDevice].batteryState) 
        {
            case UIDeviceBatteryStateUnknown:
                NSLog(@"%@", @"Unknown");
                batteryStateString = @"Unknown";
                break;
            case UIDeviceBatteryStateUnplugged:
                NSLog(@"%@", @"Unplugged");
                batteryStateString = @"Unplugged";
                break;
            case UIDeviceBatteryStateCharging:
                NSLog(@"%@", @"Charging");
                batteryStateString = @"Charging";
                break;
            case UIDeviceBatteryStateFull:
                NSLog(@"%@", @"Full");
                batteryStateString = @"Full";
                break;
            default:
                break;
        }
        [cdataSample setBatteryState:batteryStateString];
    }
    
    //
    //  Memory info.
    //
    mach_msg_type_number_t count = HOST_VM_INFO_COUNT;
    vm_statistics_data_t vmstat;
    if (host_statistics(mach_host_self(), HOST_VM_INFO, (host_info_t)&vmstat, &count) != KERN_SUCCESS)
    {
        NSLog(@"Failed to get VM statistics.");
    }
    else 
    {
        int pagesize = [[UIDevice currentDevice] pageSize];
        int wired = vmstat.wire_count * pagesize;
        int active = vmstat.active_count * pagesize;
        int inactive = vmstat.inactive_count * pagesize;
        int free = vmstat.free_count * pagesize;
        [cdataSample setMemoryWired:[NSNumber numberWithInt:wired]];
        [cdataSample setMemoryActive:[NSNumber numberWithInt:active]];
        [cdataSample setMemoryInactive:[NSNumber numberWithInt:inactive]];
        [cdataSample setMemoryFree:[NSNumber numberWithInt:free]];
        [cdataSample setMemoryUser:[NSNumber numberWithUnsignedInteger:[[UIDevice currentDevice] userMemory]]];
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

- (void) sampleBackground : (NSString *) triggeredBy
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


- (void) sampleNow : (NSString *) triggeredBy
{
    if ([UIApplication sharedApplication].applicationState == UIApplicationStateBackground)
    {
        [FlurryAnalytics logEvent:@"sampleNowBackground"
                   withParameters:[NSDictionary dictionaryWithObjectsAndKeys:triggeredBy, @"BG Sample Triggered", nil]
                            timed:YES];
        [self sampleBackground:triggeredBy];
        [FlurryAnalytics endTimedEvent:@"sampleNowBackground" withParameters:nil];
    }
    else
    {
        [FlurryAnalytics logEvent:@"sampleNowForeround"
                   withParameters:[NSDictionary dictionaryWithObjectsAndKeys:triggeredBy, @"FG Sample Triggered", nil]
                            timed:YES];
        [self sampleForeground:triggeredBy];
        [FlurryAnalytics endTimedEvent:@"sampleNowForeround" withParameters:nil];
    }
}

//
// Retrieve and send registration messages to the server.
//
- (void) fetchAndSendRegistrations : (NSUInteger) limitMessagesTo
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
        
        NSEntityDescription *entity = [NSEntityDescription entityForName:@"CoreDataRegistration" 
                                                  inManagedObjectContext:managedObjectContext];
        [fetchRequest setEntity:entity];
        [fetchRequest setFetchLimit:limitMessagesTo];  
        
        NSArray *fetchedObjects = [managedObjectContext executeFetchRequest:fetchRequest error:&error];
        if (fetchedObjects == nil) {
            NSLog(@"Could not fetch registrations, error %@, %@", error, [error userInfo]);
            goto cleanup;
        } 
        
        NSLog(@"Number of registrations fetched: %u", [fetchedObjects count]);
        
        for (CoreDataRegistration *registration in fetchedObjects)
        {
            if (registration == nil) 
                break;
            
            Registration* registrationToSend = [[Registration alloc] init];
            registrationToSend.uuId = [[Globals instance] getUUID];
            registrationToSend.timestamp = [[registration valueForKey:@"timestamp"] doubleValue]; 
            registrationToSend.platformId = (NSString*) [registration valueForKey:@"platformId"];
            registrationToSend.systemVersion = (NSString*) [registration valueForKey:@"systemVersion"]; 
            
            //
            //  Try to send. If successful, delete. 
            //
            BOOL ret = [[CommunicationManager instance] sendRegistrationMessage:registrationToSend];
            if (ret == YES) 
            {
                [managedObjectContext deleteObject:registration];
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
            if (sample == nil) 
                break;
            
            Sample* sampleToSend = [[Sample alloc] init];
            sampleToSend.uuId = [[Globals instance] getUUID ];
            sampleToSend.timestamp = [[sample valueForKey:@"timestamp"] doubleValue];
            sampleToSend.batteryState = (NSString *) [sample valueForKey:@"batteryState"];
            sampleToSend.batteryLevel = (double) [[sample valueForKey:@"batteryLevel"] doubleValue];
            sampleToSend.memoryWired = (int) [sample valueForKey:@"memoryWired"];
            sampleToSend.memoryActive = (int) [sample valueForKey:@"memoryActive"];
            sampleToSend.memoryInactive = (int) [sample valueForKey:@"memoryInactive"];
            sampleToSend.memoryFree = (int) [sample valueForKey:@"memoryFree"];
            sampleToSend.memoryUser = (int) [sample valueForKey:@"memoryUser"];
            sampleToSend.triggeredBy = (NSString *) [sample valueForKey:@"triggeredBy"];
            
            NSMutableArray *pInfoList = [[NSMutableArray alloc] init];
            sampleToSend.piList = pInfoList;
            
            NSLog(@"timestamp: %f", sampleToSend.timestamp);
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
                //NSLog(@"Id: %@", [processInfo valueForKey:@"id"]);
                //NSLog(@"Name: %@", [processInfo valueForKey:@"name"]);
            }
            
            //
            //  Try to send. If successful, delete. Note that the process info
            //  is cascaded with sample deletion.
            //
            BOOL ret = [[CommunicationManager instance] sendSample:sampleToSend];
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

//
// Send stored data in coredatastore to the server. First, we 
// send all the registration messages. Then we send samples.
//
- (void) sendStoredDataToServer : (NSUInteger) limitEntriesTo
{
    [self fetchAndSendRegistrations: limitEntriesTo];
    [self fetchAndSendSamples:limitEntriesTo];
}

- (NSDate *) getLastReportUpdateTimestamp
{
    return [NSDate date];
}

- (double) secondsSinceLastUpdate
{
    return 0.0;
}

- (HogBugReport *) getHogs 
{
    HogBugReport *dummy = [[HogBugReport alloc] init];
    return dummy;
}

- (HogBugReport *) getBugs 
{
    HogBugReport *dummy = [[HogBugReport alloc] init];
    return dummy;
}

- (double) getJScore
{
    return 0.0;
}

- (DetailScreenReport *) getOSInfo : (BOOL) with
{
    DetailScreenReport *dummy = [[DetailScreenReport alloc] init ];
    return dummy;
}

- (DetailScreenReport *) getModelInfo : (BOOL) with
{
    DetailScreenReport *dummy = [[DetailScreenReport alloc] init ];
    return dummy;
}

- (DetailScreenReport *) getSimilarAppsInfo : (BOOL) with
{
    DetailScreenReport *dummy = [[DetailScreenReport alloc] init ];
    return dummy;
}

- (NSArray *) getChangeSinceLastWeek
{
    NSArray *dummy = [[NSArray alloc] initWithObjects:@"-6",@"-11", nil];
    return dummy;
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
