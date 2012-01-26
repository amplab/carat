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
@synthesize LastUpdatedDate;
@synthesize OSInfo;
@synthesize OSInfoWithout;
@synthesize ModelInfo;
@synthesize ModelInfoWithout;
@synthesize SimilarAppsInfo;
@synthesize SimilarAppsInfoWithout;
@synthesize ChangesSinceLastWeek;

static id instance = nil;
static double JScore;
static NSArray * SubReports = nil;

+ (void) initialize {
    if (self == [Sampler class]) {
        instance = [[self alloc] init];
    }
    SubReports = [[NSArray alloc] initWithObjects:@"OSInfo",@"ModelInfo",@"SimilarAppsInfo", nil];
    [instance loadLocalReportsToMemory];
    [[CommunicationManager instance] isInternetReachable]; // This is here just to make sure CommunicationManager subscribes 
                                                           // to reachability updates.
}

+ (id) instance {
    return instance;
}

- (void) initLocalReportStore
{
    NSError *error = nil;
    NSManagedObjectContext *managedObjectContext = self.managedObjectContext;
    
    if (managedObjectContext != nil) 
    {
        CoreDataMainReport *cdataMainReport = (CoreDataMainReport *) [NSEntityDescription 
                                                                      insertNewObjectForEntityForName:@"CoreDataMainReport" 
                                                                      inManagedObjectContext:managedObjectContext];
        cdataMainReport.jScore = [NSNumber numberWithDouble:0.0];
        
        NSDateFormatter *dateFormatter = [[NSDateFormatter alloc]init];
        [dateFormatter setDateFormat:@"yyyy-MM-dd"];
        cdataMainReport.lastUpdated = [[dateFormatter dateFromString:@"1970-01-01"] retain];
        [dateFormatter release];
        
        cdataMainReport.changesSinceLastWeek = [[NSArray alloc] initWithObjects:@"0.0",@"0.0", nil];
        
        for (NSString * subReportName in SubReports)
        {
            CoreDataSubReport *cdataSubReport = (CoreDataSubReport *) [NSEntityDescription 
                                                                       insertNewObjectForEntityForName:@"CoreDataSubReport" 
                                                                       inManagedObjectContext:managedObjectContext];
            cdataSubReport.name = subReportName;
            cdataSubReport.score = [NSNumber numberWithDouble:0.0];
            cdataSubReport.distributionXWith = [[[NSArray alloc] init] autorelease];
            cdataSubReport.distributionXWithout = [[[NSArray alloc] init] autorelease];
            cdataSubReport.distributionYWith = [[[NSArray alloc] init] autorelease];
            cdataSubReport.distributionYWithout = [[[NSArray alloc] init] autorelease];
            [cdataSubReport setMainreport:cdataMainReport];
            [cdataMainReport addSubreportsObject:cdataSubReport];
        }
    }

    if ([managedObjectContext hasChanges] && ![managedObjectContext save:&error])
    {
        NSLog(@"%s Could not save coredata, error: %@, %@.", __PRETTY_FUNCTION__, error, [error userInfo]);
        return;
    }
}

//
// Load the report data (except bug and hog report) to memory so that 
// we don't have to keep going to the core data store.
//
- (void) loadLocalReportsToMemory 
{    
    NSError *error = nil;
    NSManagedObjectContext *managedObjectContext = self.managedObjectContext;
    
    if (managedObjectContext != nil) 
    {
        NSFetchRequest *fetchRequest = [[[NSFetchRequest alloc] init] autorelease];
        NSEntityDescription *entity = [NSEntityDescription entityForName:@"CoreDataMainReport" 
                                                  inManagedObjectContext:managedObjectContext];
        [fetchRequest setEntity:entity];
        
        NSArray *fetchedObjects = [managedObjectContext executeFetchRequest:fetchRequest error:&error];
        if (fetchedObjects == nil) {
            NSLog(@"Could not fetch main report data, error %@, %@", error, [error userInfo]);
            goto cleanup;
        } 
        
        NSLog(@"%s Number of main reports fetched: %u", __PRETTY_FUNCTION__, [fetchedObjects count]);
        
        //
        // If the store is empty, let us create a dummy placeholder until we
        // get back real stuff from the server.
        //
        if ([fetchedObjects count] == 0)
        {
            NSLog(@"%s Reports core data store not initialized. Initing...", __PRETTY_FUNCTION__);
            [self initLocalReportStore];
            fetchedObjects = [managedObjectContext executeFetchRequest:fetchRequest error:&error];
        }
        else if ([fetchedObjects count] > 1)    // This should not happen!!!!
        {
            NSLog(@"%s Found more than 1 item in main reports core data store!", __PRETTY_FUNCTION__);
            for (CoreDataMainReport *mainReport in fetchedObjects)
            {
                [managedObjectContext deleteObject:mainReport];
                [managedObjectContext save:nil];
            }
            [self initLocalReportStore];
            fetchedObjects = [managedObjectContext executeFetchRequest:fetchRequest error:&error];
        }
                
        for (CoreDataMainReport *mainReport in fetchedObjects)
        {
            if (mainReport == nil)
                break;
            
            JScore = [[mainReport valueForKey:@"jScore"] doubleValue];
            self.LastUpdatedDate = (NSDate *) [mainReport valueForKey:@"lastUpdated"];
            self.ChangesSinceLastWeek = (NSArray *) [mainReport valueForKey:@"changesSinceLastWeek"];
            
            NSSet *subReportsSet = mainReport.subreports;
            NSArray *subReportsArray = [subReportsSet allObjects];
            NSLog(@"%s Number of sub reports fetched: %u", __PRETTY_FUNCTION__, [subReportsArray count]);
            
            for (CoreDataSubReport *subReport in subReportsArray)
            {
                NSString *subReportName = (NSString *) [subReport valueForKey:@"name"];
                if ([subReportName isEqualToString:@"OSInfo"]) 
                {
                    if (OSInfo == nil)
                        OSInfo = [[DetailScreenReport alloc] init];
                    OSInfo.score = [[subReport valueForKey:@"score"] doubleValue];
                    OSInfo.xVals = (NSArray *) [subReport valueForKey:@"distributionXWith"];  
                    OSInfo.yVals = (NSArray *) [subReport valueForKey:@"distributionYWith"];
                    if (OSInfoWithout == nil)
                        OSInfoWithout = [[DetailScreenReport alloc] init];
                    OSInfoWithout.score = [[subReport valueForKey:@"score"] doubleValue]; 
                    OSInfoWithout.xVals = (NSArray *) [subReport valueForKey:@"distributionXWithout"];  
                    OSInfoWithout.yVals = (NSArray *) [subReport valueForKey:@"distributionYWithout"];
                } 
                else if ([subReportName isEqualToString:@"ModelInfo"]) 
                {
                    if (ModelInfo == nil)
                        ModelInfo = [[DetailScreenReport alloc] init];
                    ModelInfo.score = [[subReport valueForKey:@"score"] doubleValue];
                    ModelInfo.xVals = (NSArray *) [subReport valueForKey:@"distributionXWith"];  
                    ModelInfo.yVals = (NSArray *) [subReport valueForKey:@"distributionYWith"];
                    
                    if (ModelInfoWithout == nil)
                        ModelInfoWithout = [[DetailScreenReport alloc] init];
                    ModelInfoWithout.score = [[subReport valueForKey:@"score"] doubleValue]; 
                    ModelInfoWithout.xVals = (NSArray *) [subReport valueForKey:@"distributionXWithout"];  
                    ModelInfoWithout.yVals = (NSArray *) [subReport valueForKey:@"distributionYWithout"];
                }
                else if ([subReportName isEqualToString:@"SimilarAppsInfo"])
                {
                    if (SimilarAppsInfo == nil)
                        SimilarAppsInfo = [[DetailScreenReport alloc] init];
                    SimilarAppsInfo.score = [[subReport valueForKey:@"score"] doubleValue];
                    SimilarAppsInfo.xVals = (NSArray *) [subReport valueForKey:@"distributionXWith"];  
                    SimilarAppsInfo.yVals = (NSArray *) [subReport valueForKey:@"distributionYWith"];
                    
                    if (SimilarAppsInfoWithout == nil)
                        SimilarAppsInfoWithout = [[DetailScreenReport alloc] init];
                    SimilarAppsInfoWithout.score = [[subReport valueForKey:@"score"] doubleValue]; 
                    SimilarAppsInfoWithout.xVals = (NSArray *) [subReport valueForKey:@"distributionXWithout"];  
                    SimilarAppsInfoWithout.yVals = (NSArray *) [subReport valueForKey:@"distributionYWithout"];
                }
            }
        }
                
    cleanup:
        return;
    }
}

- (BOOL) clearLocalAppReports
{
    NSError *error = nil;
    NSManagedObjectContext *managedObjectContext = self.managedObjectContext;
    if (managedObjectContext != nil) 
    {
        NSFetchRequest *fetchRequest = [[[NSFetchRequest alloc] init] autorelease];
        NSEntityDescription *entity = [NSEntityDescription entityForName:@"CoreDataAppReport" 
                                                  inManagedObjectContext:managedObjectContext];
        [fetchRequest setEntity:entity];
        
        NSArray *fetchedObjects = [managedObjectContext executeFetchRequest:fetchRequest error:&error];
        if (fetchedObjects == nil) {
            NSLog(@"%s Could not fetch app report data, error %@, %@", __PRETTY_FUNCTION__,error, [error userInfo]);
            return NO;
        } 
        
        // NOTE: We don't want to save this unless putting new data is successful. 
        // That will happen in the calling function.
        for (CoreDataAppReport *appReport in fetchedObjects)
        {
            [managedObjectContext deleteObject:appReport];
        }
        
        return YES;
    }
    return NO;
}

- (void) updateLocalSubReport: (CoreDataSubReport *) cdataSubReport 
          withThisDetailReport: (DetailScreenReport *) detailScreenReportWith 
          andThatDetailReport: (DetailScreenReport *) detailScreenReportWithout
{
    cdataSubReport.score = [NSNumber numberWithDouble:detailScreenReportWith.score];
    cdataSubReport.distributionXWith = detailScreenReportWith.xVals;
    cdataSubReport.distributionYWith = detailScreenReportWith.yVals;
    cdataSubReport.distributionXWithout = detailScreenReportWithout.xVals;
    cdataSubReport.distributionYWithout = detailScreenReportWithout.yVals;
}

//
// Refresh all the local reports from server. 
//
- (void) updateLocalReportsFromServer
{
    NSError *error = nil;
    NSManagedObjectContext *managedObjectContext = self.managedObjectContext;
    
    if (managedObjectContext != nil) 
    {
        NSFetchRequest *fetchRequest = [[[NSFetchRequest alloc] init] autorelease];
        NSEntityDescription *entity = [NSEntityDescription entityForName:@"CoreDataMainReport" 
                                                  inManagedObjectContext:managedObjectContext];
        [fetchRequest setEntity:entity];
        
        NSArray *fetchedObjects = [managedObjectContext executeFetchRequest:fetchRequest error:&error];
        if (fetchedObjects == nil) {
            NSLog(@"%s Could not fetch CoreDataMainReport, error %@, %@", __PRETTY_FUNCTION__, error, [error userInfo]);
            return;
        } 
        
        NSLog(@"%s Number of main reports fetched: %u", __PRETTY_FUNCTION__, [fetchedObjects count]);
        
        // Check for sanity.
        if ([fetchedObjects count] == 0)
        {
            NSLog(@"%s Reports core data store not initialized. Initing...", __PRETTY_FUNCTION__);
            [self initLocalReportStore];
            fetchedObjects = [managedObjectContext executeFetchRequest:fetchRequest error:&error];
        }
        else if ([fetchedObjects count] > 1)    // This should not happen!!!!
        {
            NSLog(@"%s Found more than 1 item in main reports core data store!", __PRETTY_FUNCTION__);
            for (CoreDataMainReport *mainReport in fetchedObjects)
            {
                [managedObjectContext deleteObject:mainReport];
                [managedObjectContext save:nil];
            }
            [self initLocalReportStore];
            fetchedObjects = [managedObjectContext executeFetchRequest:fetchRequest error:&error];
        }

        // Now let's get the report data from the server. First main reports.
        Reports *reports = [[CommunicationManager instance] getReports];
        
        if (reports != nil)
        {
            CoreDataMainReport *cdataMainReport = [fetchedObjects objectAtIndex:0];
            if (cdataMainReport == nil) return;
            
            [cdataMainReport setLastUpdated:[NSDate date]];
            double lastJScore = [[cdataMainReport valueForKey:@"jScore"] doubleValue];
            double change = reports.jScore - lastJScore;
            double changePercentage = ((reports.jScore - lastJScore)*100.0) / lastJScore;
            
            [cdataMainReport setJScore:[NSNumber numberWithDouble:reports.jScore]];
            NSArray *existing = (NSArray *) [cdataMainReport valueForKey:@"changesSinceLastWeek"];
            [existing release];
            NSArray *new = [[NSArray alloc] initWithObjects:
                            [NSNumber numberWithDouble:change],
                            [NSNumber numberWithDouble:changePercentage], 
                            nil];
            cdataMainReport.changesSinceLastWeek = new;
            
            // Subreports.
            NSSet *subReportsSet = cdataMainReport.subreports;
            NSArray *subReportsArray = [subReportsSet allObjects];
            NSLog(@"%s Number of sub reports fetched: %u", __PRETTY_FUNCTION__, [subReportsArray count]);
            
            for (CoreDataSubReport *cdataSubReport in subReportsArray)
            {
                NSString *subReportName = (NSString *) [cdataSubReport valueForKey:@"name"];
                if ([subReportName isEqualToString:@"OSInfo"]) 
                {
                    [self updateLocalSubReport:cdataSubReport 
                           withThisDetailReport:reports.os 
                           andThatDetailReport:reports.osWithout];
                } 
                else if ([subReportName isEqualToString:@"ModelInfo"]) 
                {
                    [self updateLocalSubReport:cdataSubReport 
                           withThisDetailReport:reports.model 
                           andThatDetailReport:reports.modelWithout];
                }
                else if ([subReportName isEqualToString:@"SimilarAppsInfo"])
                {
                    [self updateLocalSubReport:cdataSubReport 
                          withThisDetailReport:reports.similarApps 
                           andThatDetailReport:reports.similarAppsWithout];
                }
            }
        }
        
        // Clear local app reports.
        if ([self clearLocalAppReports] == NO)
            return;
        
        // Hog report
        Feature *feature = [[[Feature alloc] init] autorelease];
        [feature setKey:@"ReportType"];
        [feature setValue:@"Hog"];
        FeatureList list = [[NSArray alloc] initWithObjects:feature, nil];
        HogBugReport *hogReport = [[CommunicationManager instance] getHogOrBugReport:list];
        if (hogReport != nil)
        {
            HogsBugsList hogList = hogReport.hbList;
            for(HogsBugs * hog in hogList)
            {
                CoreDataAppReport *cdataAppReport = (CoreDataAppReport *) [NSEntityDescription 
                                                                           insertNewObjectForEntityForName:@"CoreDataAppReport" 
                                                                           inManagedObjectContext:managedObjectContext];
                [cdataAppReport setAppName:hog.appName];
                [cdataAppReport setAppScore:[NSNumber numberWithDouble:hog.wDistance]];
                [cdataAppReport setReportType:@"Hog"];
                [cdataAppReport setLastUpdated:[NSDate date]];
                CoreDataDetail *cdataDetail = (CoreDataDetail *) [NSEntityDescription 
                                                                  insertNewObjectForEntityForName:@"CoreDataDetail" 
                                                                  inManagedObjectContext:managedObjectContext];
                [cdataDetail setDistance:[NSNumber numberWithDouble:hog.wDistance]];
                cdataDetail.distributionXWith = hog.xVals;
                cdataDetail.distributionXWithout = hog.xValsWithout;
                cdataDetail.distributionYWith = hog.yVals;
                cdataDetail.distributionYWithout = hog.yValsWithout;
                [cdataDetail setAppReport:cdataAppReport];
                [cdataAppReport setAppDetails:cdataDetail];
            }
        }
        [list release];
        
        // Bug report
        [feature setValue:@"Bug"];
        list = [[NSArray alloc] initWithObjects:feature, nil];
        HogBugReport *bugReport = [[CommunicationManager instance] getHogOrBugReport:list];
        if (bugReport != nil)
        {
            HogsBugsList bugList = bugReport.hbList;
            for(HogsBugs * bug in bugList)
            {
                CoreDataAppReport *cdataAppReport = (CoreDataAppReport *) [NSEntityDescription 
                                                                           insertNewObjectForEntityForName:@"CoreDataAppReport" 
                                                                           inManagedObjectContext:managedObjectContext];
                [cdataAppReport setAppName:bug.appName];
                [cdataAppReport setAppScore:[NSNumber numberWithDouble:bug.wDistance]];
                [cdataAppReport setReportType:@"Bug"];
                [cdataAppReport setLastUpdated:[NSDate date]];
                CoreDataDetail *cdataDetail = (CoreDataDetail *) [NSEntityDescription 
                                                                           insertNewObjectForEntityForName:@"CoreDataDetail" 
                                                                           inManagedObjectContext:managedObjectContext];
                [cdataDetail setDistance:[NSNumber numberWithDouble:bug.wDistance]];
                cdataDetail.distributionXWith = bug.xVals;
                cdataDetail.distributionXWithout = bug.xValsWithout;
                cdataDetail.distributionYWith = bug.yVals;
                cdataDetail.distributionYWithout = bug.yValsWithout;
                [cdataDetail setAppReport:cdataAppReport];
                [cdataAppReport setAppDetails:cdataDetail];
            }
        }
        [list release];
    
        // Save the entire stuff.
        if ([managedObjectContext hasChanges] && ![managedObjectContext save:&error])
        {
            NSLog(@"%s Could not save coredata, error: %@, %@.", __PRETTY_FUNCTION__, error, [error userInfo]);
            return;
        }
        
        // Reload data in memory.
        [self loadLocalReportsToMemory];
    }    
}

- (id) initWithCommManager:(id)cManager 
{
    self = [super init];
    return self;
}

- (void) dealloc
{
    [__managedObjectContext release];
    [__managedObjectModel release];
    [__persistentStoreCoordinator release];
    [__fetchResultsController release];
    [LastUpdatedDate release];
    [OSInfo release];
    [OSInfoWithout release];
    [ModelInfo release];
    [ModelInfoWithout release];
    [SimilarAppsInfo release];
    [SimilarAppsInfoWithout release];
    [ChangesSinceLastWeek release];
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
    [h release];
    
    [cdataRegistration setSystemVersion:[UIDevice currentDevice].systemVersion];
    
    //
    //  Now save it.
    //
    if ([managedObjectContext hasChanges] && ![managedObjectContext save:&error])
    {
        /*
         Error is logged, but we soldier on without saving our registration. :-(
         */
        [FlurryAnalytics logEvent:@"generateSaveRegistration Error"
                   withParameters:[NSDictionary dictionaryWithObjectsAndKeys:[NSString stringWithFormat:@"Unresolved error %@, %@", error, [error userInfo]], @"Error Info", nil]];
        NSLog(@"%s Unresolved error %@, %@", __PRETTY_FUNCTION__,error, [error userInfo]);
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
         Error is logged, but we soldier on without saving our sample. :-(
         */
        [FlurryAnalytics logEvent:@"sampleForeground Error"
                   withParameters:[NSDictionary dictionaryWithObjectsAndKeys:[NSString stringWithFormat:@"Unresolved error %@, %@", error, [error userInfo]], @"Error Info", nil]];
        NSLog(@"%s Unresolved error %@, %@", __PRETTY_FUNCTION__,error, [error userInfo]);
        
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
    
    [self sampleForeground:triggeredBy];
    
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
            NSLog(@"%s Could not fetch registrations, error %@, %@", __PRETTY_FUNCTION__, error, [error userInfo]);
            goto cleanup;
        } 
        
        NSLog(@"%s # registrations fetched: %u", __PRETTY_FUNCTION__, [fetchedObjects count]);
        
        for (CoreDataRegistration *registration in fetchedObjects)
        {
            if (registration == nil) 
                break;
            
            Registration* registrationToSend = [[[Registration alloc] init] autorelease];
            registrationToSend.uuId = [[Globals instance] getUUID ];
            registrationToSend.timestamp = [[registration valueForKey:@"timestamp"] doubleValue]; 
            registrationToSend.platformId = (NSString*) [registration valueForKey:@"platformId"];
            registrationToSend.systemVersion = (NSString*) [registration valueForKey:@"systemVersion"]; 
            
            NSLog(@"%s\ttimestamp: %f", __PRETTY_FUNCTION__, registrationToSend.timestamp);
            NSLog(@"%s\tplatformId: %@", __PRETTY_FUNCTION__,registrationToSend.platformId);
            NSLog(@"%s\tsystemVersion: %@", __PRETTY_FUNCTION__,registrationToSend.systemVersion);
            
            //
            //  Try to send. If successful, delete. 
            //
            BOOL ret = [[CommunicationManager instance] sendRegistrationMessage:registrationToSend];
            if (ret == YES) 
            {
                [managedObjectContext deleteObject:registration];
                if (![managedObjectContext save:&error])
                {
                    NSLog(@"%s Could not delete registration from coredata, error %@, %@", __PRETTY_FUNCTION__,error, [error userInfo]);
                }
            }
        }
        
    cleanup:
        [sortDescriptors release];
        [sortDescriptor release];
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
            NSLog(@"%s Could not fetch samples, error %@, %@", __PRETTY_FUNCTION__,error, [error userInfo]);
            goto cleanup;
        } 
        
        NSLog(@"%s Number of samples fetched: %u",__PRETTY_FUNCTION__,[fetchedObjects count]);
        
        for (CoreDataSample *sample in fetchedObjects)
        {
            if (sample == nil) 
                break;
            
            Sample* sampleToSend = [[[Sample alloc] init] autorelease];
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
            
            NSMutableArray *pInfoList = [[[NSMutableArray alloc] init] autorelease];
            sampleToSend.piList = pInfoList;
            
            NSLog(@"%s\ttimestamp: %f",__PRETTY_FUNCTION__, sampleToSend.timestamp);
            NSLog(@"%s\tbatteryLevel: %@",__PRETTY_FUNCTION__, [sample valueForKey:@"batteryLevel"]);
            NSLog(@"%s\tbatteryState: %@",__PRETTY_FUNCTION__, [sample valueForKey:@"batteryState"]);
            NSLog(@"%s\ttriggeredBy: %@",__PRETTY_FUNCTION__, sampleToSend.triggeredBy);
            
            //
            //  Get all the process info objects for this sample.
            //
            NSSet *processInfosSet = sample.processInfos;
            NSArray *processInfoArray = [processInfosSet allObjects];
            
            for (CoreDataProcessInfo *processInfo in processInfoArray)
            {
                ProcessInfo *pInfo = [[[ProcessInfo alloc] init] autorelease];
                pInfo.pId = (int)[processInfo valueForKey:@"id"];
                pInfo.pName = (NSString *)[processInfo valueForKey:@"name"];
                [pInfoList addObject:pInfo];
            }
            
            //
            //  Try to send. If successful, delete. Note that the process info
            //  is cascaded with sample deletion.
            //
            BOOL ret = [[CommunicationManager instance] sendSample:sampleToSend];
            if (ret == YES) 
            {
                [managedObjectContext deleteObject:sample];
                if (![managedObjectContext save:&error])
                {
                    NSLog(@"%s Could not delete sample from coredata, error %@, %@", __PRETTY_FUNCTION__,error, [error userInfo]);
                }
            }
        }
        
    cleanup:
        [sortDescriptors release];
        [sortDescriptor release];
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

- (void) checkConnectivityAndSendStoredDataToServer
{
    if ([[CommunicationManager instance] isInternetReachable] == YES)
    {
        NSLog(@"%s Internet active", __PRETTY_FUNCTION__);
        dispatch_async( dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
            [self sendStoredDataToServer:100];
            dispatch_async( dispatch_get_main_queue(), ^{
                NSLog(@"%s Done!", __PRETTY_FUNCTION__);
            });
        });
    } 
    else 
    {
        NSLog(@"%s No connectivity", __PRETTY_FUNCTION__);
    }
}

- (NSDate *) getLastReportUpdateTimestamp
{
    if (self.LastUpdatedDate != nil) 
    {
        NSLog(@"%s %@", __PRETTY_FUNCTION__, self.LastUpdatedDate);
        return self.LastUpdatedDate;
    } 
    else
    {
        NSLog(@"%s LastUpdateDate is null", __PRETTY_FUNCTION__);
        NSDate *now = [[NSDate date] retain];
        return now;
    }
}

- (double) secondsSinceLastUpdate
{
    NSTimeInterval interval = 0.0;
    if (self.LastUpdatedDate != nil)
    {
        NSDate *now = [[NSDate date] retain];
        interval = [now timeIntervalSinceDate:self.LastUpdatedDate];
        [now release];
    }
    NSLog(@"%s %f", __PRETTY_FUNCTION__, interval);
    return interval;
}

- (HogBugReport *) getHogs 
{
    NSError *error = nil;
    NSManagedObjectContext *managedObjectContext = self.managedObjectContext;
    if (managedObjectContext != nil) 
    {
        NSFetchRequest *fetchRequest = [[[NSFetchRequest alloc] init] autorelease];
        NSPredicate *predicate = [NSPredicate predicateWithFormat:@"reportType == %@", @"Hog"];
        [fetchRequest setPredicate:predicate];
        NSSortDescriptor *sortDescriptor = [[NSSortDescriptor alloc] initWithKey:@"appScore" 
                                                                      ascending:NO];
        NSArray *sortDescriptors = [[NSArray alloc] initWithObjects:sortDescriptor, nil];
        [fetchRequest setSortDescriptors:sortDescriptors];

        NSEntityDescription *entity = [NSEntityDescription entityForName:@"CoreDataAppReport" 
                                                  inManagedObjectContext:managedObjectContext];
        [fetchRequest setEntity:entity];
        
        NSArray *fetchedObjects = [managedObjectContext executeFetchRequest:fetchRequest error:&error];
        if (fetchedObjects == nil) {
            NSLog(@"%s Could not fetch app report data, error %@, %@", __PRETTY_FUNCTION__,error, [error userInfo]);
            return nil;
        }
     
        HogBugReport * hogs = [[[HogBugReport alloc] init] autorelease];
        NSMutableArray * hbList = [[[NSMutableArray alloc] init] autorelease];
        [hogs setHbList:hbList];
        for (CoreDataAppReport *cdataAppReport in fetchedObjects)
        {
            HogsBugs *hog = [[[HogsBugs alloc] init] autorelease];
            [hog setAppName:[cdataAppReport valueForKey:@"appName"]];
            [hog setWDistance:[[cdataAppReport valueForKey:@"appScore"] doubleValue]];
            CoreDataDetail *cdataDetail = cdataAppReport.appDetails;
            [hog setXVals:(NSArray *) [cdataDetail valueForKey:@"distributionXWith"]];
            [hog setXValsWithout:(NSArray *) [cdataDetail valueForKey:@"distributionXWithout"]];
            [hog setYVals:(NSArray *) [cdataDetail valueForKey:@"distributionYWith"]];
            [hog setYValsWithout:(NSArray *) [cdataDetail valueForKey:@"distributionYWithout"]];
            [hbList addObject:hog];
        }
        return hogs;
    }
    return nil;
}

- (HogBugReport *) getBugs 
{
    NSError *error = nil;
    NSManagedObjectContext *managedObjectContext = self.managedObjectContext;
    if (managedObjectContext != nil) 
    {
        NSFetchRequest *fetchRequest = [[[NSFetchRequest alloc] init] autorelease];
        NSPredicate *predicate = [NSPredicate predicateWithFormat:@"reportType == %@", @"Bug"];
        [fetchRequest setPredicate:predicate];
        NSSortDescriptor *sortDescriptor = [[NSSortDescriptor alloc] initWithKey:@"appScore" 
                                                                       ascending:NO];
        NSArray *sortDescriptors = [[NSArray alloc] initWithObjects:sortDescriptor, nil];
        [fetchRequest setSortDescriptors:sortDescriptors];
        
        NSEntityDescription *entity = [NSEntityDescription entityForName:@"CoreDataAppReport" 
                                                  inManagedObjectContext:managedObjectContext];
        [fetchRequest setEntity:entity];
        
        NSArray *fetchedObjects = [managedObjectContext executeFetchRequest:fetchRequest error:&error];
        if (fetchedObjects == nil) {
            NSLog(@"%s Could not fetch app report data, error %@, %@", __PRETTY_FUNCTION__,error, [error userInfo]);
            return nil;
        }
        
        HogBugReport * bugs = [[[HogBugReport alloc] init] autorelease];
        NSMutableArray * hbList = [[[NSMutableArray alloc] init] autorelease];
        [bugs setHbList:hbList];
        for (CoreDataAppReport *cdataAppReport in fetchedObjects)
        {
            HogsBugs *bug = [[[HogsBugs alloc] init] autorelease];
            [bug setAppName:[cdataAppReport valueForKey:@"appName"]];
            [bug setWDistance:[[cdataAppReport valueForKey:@"appScore"] doubleValue]];
            CoreDataDetail *cdataDetail = cdataAppReport.appDetails;
            [bug setXVals:(NSArray *) [cdataDetail valueForKey:@"distributionXWith"]];
            [bug setXValsWithout:(NSArray *) [cdataDetail valueForKey:@"distributionXWithout"]];
            [bug setYVals:(NSArray *) [cdataDetail valueForKey:@"distributionYWith"]];
            [bug setYValsWithout:(NSArray *) [cdataDetail valueForKey:@"distributionYWithout"]];
            [hbList addObject:bug];
        }
        return bugs;
    }
    return nil;
}

- (double) getJScore
{
    NSLog(@"%s %f", __PRETTY_FUNCTION__, JScore);
    return JScore;
}

- (DetailScreenReport *) getOSInfo : (BOOL) with
{
    if (with == YES)
        if (self.OSInfo != nil)
            return self.OSInfo;
    else 
        if (self.OSInfoWithout != nil)
            return self.OSInfoWithout;
    return nil;
}

- (DetailScreenReport *) getModelInfo : (BOOL) with
{
    if (with == YES)
        if (self.ModelInfo != nil)
            return self.ModelInfo;
        else 
            if (self.ModelInfoWithout != nil)
                return self.ModelInfoWithout;
    return nil;
}

- (DetailScreenReport *) getSimilarAppsInfo : (BOOL) with
{
    if (with == YES)
        if (self.SimilarAppsInfo != nil)
            return self.SimilarAppsInfo;
        else 
            if (self.SimilarAppsInfoWithout != nil)
                return self.SimilarAppsInfoWithout;
    return nil;
}

- (NSArray *) getChangeSinceLastWeek
{
    if (ChangesSinceLastWeek == nil)
    {
        NSArray *dummy = [[NSArray alloc] initWithObjects:@"-6",@"-11", nil];
        return dummy;
    }
    return ChangesSinceLastWeek;
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
    NSLog(@"%s Coredatastore location: %@", __PRETTY_FUNCTION__, storeURL);
    
    NSError *error = nil;
    __persistentStoreCoordinator = [[NSPersistentStoreCoordinator alloc] initWithManagedObjectModel:[self managedObjectModel]];
    if (![__persistentStoreCoordinator addPersistentStoreWithType:NSSQLiteStoreType configuration:nil URL:storeURL options:nil error:&error])
    {
        /*
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
        /*
         Error is logged, but we soldier on. Should probably implement one of the solutions above.
         */
        [FlurryAnalytics logEvent:@"sampleForeground Error"
                   withParameters:[NSDictionary dictionaryWithObjectsAndKeys:[NSString stringWithFormat:@"Unresolved error %@, %@", error, [error userInfo]], @"Error Info", nil]];
        NSLog(@"%s Unresolved error %@, %@", __PRETTY_FUNCTION__,error, [error userInfo]);
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
