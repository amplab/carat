//
//  CoreDataManager.m
//  Carat
//
//  Manages the core data store.
//
//  Created by Anand Padmanabha Iyer on 11/5/11.
//  Copyright (c) 2011 UC Berkeley. All rights reserved.
//

#import "CoreDataManager.h"
#import "FlurryAnalytics.h"
#import "UIDeviceHardware.h"
#import "Utilities.h"

@implementation CoreDataManager (hidden)

static NSArray * SubReports = nil;
static double JScore;
static NSString * reportUpdateStatus = nil;
static dispatch_semaphore_t sendStoredDataToServerSemaphore;
static NSMutableDictionary * daemonsList = nil;

- (void) postNotification
{
    [[NSNotificationCenter defaultCenter] postNotificationName:@"CCDMReportUpdateStatusNotification" 
                                                        object:nil];
}

- (void) postNotificationOnMainThread
{
    [self performSelectorOnMainThread:@selector(postNotification) 
                           withObject:nil 
                        waitUntilDone:YES];
}

/**
 *  Initialize the core data store reports table.
 */
- (void) initLocalReportStore : (NSManagedObjectContext *) managedObjectContext
{
    NSError *error = nil;
    //NSManagedObjectContext *managedObjectContext = self.managedObjectContext;
    
    if (managedObjectContext != nil) 
    {
        CoreDataMainReport *cdataMainReport = (CoreDataMainReport *) [NSEntityDescription 
                                                                      insertNewObjectForEntityForName:@"CoreDataMainReport" 
                                                                      inManagedObjectContext:managedObjectContext];
        cdataMainReport.jScore = [NSNumber numberWithDouble:0.0];
        
        NSDateFormatter *dateFormatter = [[NSDateFormatter alloc]init];
        [dateFormatter setDateFormat:@"yyyy-MM-dd"];
        cdataMainReport.lastUpdated = [dateFormatter dateFromString:@"1970-01-01"];
        [dateFormatter release];
        
        cdataMainReport.changesSinceLastWeek = [[[NSArray alloc] initWithObjects:@"0.0",@"0.0", nil] autorelease];
        
        for (NSString * subReportName in SubReports)
        {
            CoreDataSubReport *cdataSubReport = (CoreDataSubReport *) [NSEntityDescription 
                                                                       insertNewObjectForEntityForName:@"CoreDataSubReport" 
                                                                       inManagedObjectContext:managedObjectContext];
            cdataSubReport.name = subReportName;
            cdataSubReport.score = [NSNumber numberWithDouble:0.0];
            cdataSubReport.expectedValue = [NSNumber numberWithDouble:0.0];
            cdataSubReport.expectedValueWithout = [NSNumber numberWithDouble:0.0];
            cdataSubReport.error = [NSNumber numberWithDouble:0.0];
            cdataSubReport.errorWithout = [NSNumber numberWithDouble:0.0];
            cdataSubReport.samples = [NSNumber numberWithDouble:0.0];
            cdataSubReport.samplesWithout = [NSNumber numberWithDouble:0.0];
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
        DLog(@"%s Could not save coredata, error: %@, %@.", __PRETTY_FUNCTION__, error, [error userInfo]);
        return;
    }
}

/** 
 *  Load the report data (except bug and hog report) to memory so that 
 *  we don't have to keep going to the core data store.
 */
- (void) loadLocalReportsToMemory : (NSManagedObjectContext *) managedObjectContext
{    
    NSError *error = nil;
    
    if (managedObjectContext != nil) 
    {
        NSFetchRequest *fetchRequest = [[[NSFetchRequest alloc] init] autorelease];
        NSEntityDescription *entity = [NSEntityDescription entityForName:@"CoreDataMainReport" 
                                                  inManagedObjectContext:managedObjectContext];
        [fetchRequest setEntity:entity];
        
        NSArray *fetchedObjects = [managedObjectContext executeFetchRequest:fetchRequest error:&error];
        if (fetchedObjects == nil) {
            DLog(@"%s Could not fetch main report data, error %@, %@", __PRETTY_FUNCTION__, error, [error userInfo]);
            goto cleanup;
        } 
        
        DLog(@"%s Number of main reports fetched: %u", __PRETTY_FUNCTION__, [fetchedObjects count]);
        
        //
        // If the store is empty, let us create a dummy placeholder until we
        // get back real stuff from the server.
        //
        if ([fetchedObjects count] == 0)
        {
            DLog(@"%s Reports core data store not initialized. Initing...", __PRETTY_FUNCTION__);
            [self initLocalReportStore : managedObjectContext];
            fetchedObjects = [managedObjectContext executeFetchRequest:fetchRequest error:&error];
        }
        else if ([fetchedObjects count] > 1)    // This should not happen!!!!
        {
            DLog(@"%s Found more than 1 item in main reports core data store!", __PRETTY_FUNCTION__);
            for (CoreDataMainReport *mainReport in fetchedObjects)
            {
                [managedObjectContext deleteObject:mainReport];
                [managedObjectContext save:nil];
            }
            [self initLocalReportStore : managedObjectContext];
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
            DLog(@"%s Number of sub reports fetched: %u", __PRETTY_FUNCTION__, [subReportsArray count]);
            
            for (CoreDataSubReport *subReport in subReportsArray)
            {
                NSString *subReportName = (NSString *) [subReport valueForKey:@"name"];
                if ([subReportName isEqualToString:@"JScoreInfo"]) 
                {
                    if (JScoreInfo == nil)
                        JScoreInfo = [[DetailScreenReport alloc] init];
                    JScoreInfo.score = [[subReport valueForKey:@"score"] doubleValue];
                    JScoreInfo.xVals = (NSArray *) [subReport valueForKey:@"distributionXWith"];  
                    JScoreInfo.yVals = (NSArray *) [subReport valueForKey:@"distributionYWith"];
                    JScoreInfo.expectedValue = [[subReport valueForKey:@"expectedValue"] doubleValue];
                    JScoreInfo.error = [[subReport valueForKey:@"error"] doubleValue];
                    JScoreInfo.samples = [[subReport valueForKey:@"samples"] doubleValue];
                    if (JScoreInfoWithout == nil)
                        JScoreInfoWithout = [[DetailScreenReport alloc] init];
                    JScoreInfoWithout.score = [[subReport valueForKey:@"score"] doubleValue]; 
                    JScoreInfoWithout.expectedValue = [[subReport valueForKey:@"expectedValueWithout"] doubleValue];
                    JScoreInfoWithout.error = [[subReport valueForKey:@"errorWithout"] doubleValue];
                    JScoreInfoWithout.samples = [[subReport valueForKey:@"samplesWithout"] doubleValue];
                    JScoreInfoWithout.xVals = (NSArray *) [subReport valueForKey:@"distributionXWithout"];  
                    JScoreInfoWithout.yVals = (NSArray *) [subReport valueForKey:@"distributionYWithout"];
                }
                else if ([subReportName isEqualToString:@"OSInfo"]) 
                {
                    if (OSInfo == nil)
                        OSInfo = [[DetailScreenReport alloc] init];
                    OSInfo.score = [[subReport valueForKey:@"score"] doubleValue];
                    OSInfo.xVals = (NSArray *) [subReport valueForKey:@"distributionXWith"];  
                    OSInfo.yVals = (NSArray *) [subReport valueForKey:@"distributionYWith"];
                    OSInfo.expectedValue = [[subReport valueForKey:@"expectedValue"] doubleValue];
                    OSInfo.error = [[subReport valueForKey:@"error"] doubleValue];
                    OSInfo.samples = [[subReport valueForKey:@"samples"] doubleValue];
                    if (OSInfoWithout == nil)
                        OSInfoWithout = [[DetailScreenReport alloc] init];
                    OSInfoWithout.score = [[subReport valueForKey:@"score"] doubleValue]; 
                    OSInfoWithout.expectedValue = [[subReport valueForKey:@"expectedValueWithout"] doubleValue];
                    OSInfoWithout.error = [[subReport valueForKey:@"errorWithout"] doubleValue];
                    OSInfoWithout.samples = [[subReport valueForKey:@"samplesWithout"] doubleValue];
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
                    ModelInfo.expectedValue = [[subReport valueForKey:@"expectedValue"] doubleValue];
                    ModelInfo.error = [[subReport valueForKey:@"error"] doubleValue];
                    ModelInfo.samples = [[subReport valueForKey:@"samples"] doubleValue];
                    if (ModelInfoWithout == nil)
                        ModelInfoWithout = [[DetailScreenReport alloc] init];
                    ModelInfoWithout.score = [[subReport valueForKey:@"score"] doubleValue]; 
                    ModelInfoWithout.expectedValue = [[subReport valueForKey:@"expectedValueWithout"] doubleValue];
                    ModelInfoWithout.error = [[subReport valueForKey:@"errorWithout"] doubleValue];
                    ModelInfoWithout.samples = [[subReport valueForKey:@"samplesWithout"] doubleValue];
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
                    SimilarAppsInfo.expectedValue = [[subReport valueForKey:@"expectedValue"] doubleValue];
                    SimilarAppsInfo.error = [[subReport valueForKey:@"error"] doubleValue];
                    SimilarAppsInfo.samples = [[subReport valueForKey:@"samples"] doubleValue];
                    if (SimilarAppsInfoWithout == nil)
                        SimilarAppsInfoWithout = [[DetailScreenReport alloc] init];
                    SimilarAppsInfoWithout.score = [[subReport valueForKey:@"score"] doubleValue]; 
                    SimilarAppsInfoWithout.expectedValue = [[subReport valueForKey:@"expectedValueWithout"] doubleValue];
                    SimilarAppsInfoWithout.error = [[subReport valueForKey:@"errorWithout"] doubleValue];
                    SimilarAppsInfoWithout.samples = [[subReport valueForKey:@"samplesWithout"] doubleValue];
                    SimilarAppsInfoWithout.xVals = (NSArray *) [subReport valueForKey:@"distributionXWithout"];
                    SimilarAppsInfoWithout.yVals = (NSArray *) [subReport valueForKey:@"distributionYWithout"];
                }
            }
        }
        
    cleanup:
        return;
    }
}
#pragma mark - Daemons list syncing
/**
 * Gets the modification date for a file.
 */
- (NSDate *) getFileModificationDate : (NSString *) filePath
{
    DLog(@"%s getting modification date for %@", __PRETTY_FUNCTION__, filePath);
    NSDate *fileModificationDate = [NSDate dateWithTimeIntervalSinceReferenceDate:0];
    NSError *error = nil;
    
    if ([[NSFileManager defaultManager] fileExistsAtPath:filePath]) 
    {
        NSDictionary *attributes = [[NSFileManager defaultManager] attributesOfItemAtPath:filePath 
                                                                                    error:&error];
        
        if (attributes != nil) { fileModificationDate = [attributes fileModificationDate]; }
        else { DLog(@"%s Could not retrieve file modification date, %@", __PRETTY_FUNCTION__, error); }
    }
    
    return fileModificationDate;
}

- (void) connectionDidFinishLoading: (NSURLConnection *) connection
{   
    NSString * responseString = [[NSString alloc] initWithData:self.receivedData 
                                                   encoding:NSUTF8StringEncoding];
    DLog(@"%s Daemon list received from server: [%@]", __PRETTY_FUNCTION__, responseString);
    if ([responseString length] > 0) 
    {
        [daemonsList removeAllObjects];
        for (NSString *line in [responseString componentsSeparatedByString:@"\n"]) 
        {
            DLog(@"%s %@", __PRETTY_FUNCTION__, line);
            [daemonsList setObject:@"1" forKey:line];
        }
    }
    [responseString release];
    
    //
    // Remove the file.
    //
    if (![[NSFileManager defaultManager] removeItemAtPath:self.daemonsFilePath 
                                                    error:nil]) 
    {
        DLog(@"%s Could not remove the cached file.", __PRETTY_FUNCTION__);
        return;
    }
    
    //
    // Recreate it
    //
    if (![[NSFileManager defaultManager] createFileAtPath:self.daemonsFilePath
                                            contents:self.receivedData
                                          attributes:nil])
    {
        DLog(@"%s Could not create cache file.", __PRETTY_FUNCTION__);
        return;
    }
    
    //
    // Set modification date.
    //
    NSDictionary *dict = [[[NSDictionary alloc] initWithObjectsAndKeys:[NSDate date], NSFileModificationDate, nil] autorelease];
    if (![[NSFileManager defaultManager] setAttributes:dict 
                                          ofItemAtPath:self.daemonsFilePath 
                                                 error:nil]) 
    {
        DLog(@"%s Could not set modification date for the cached file.", __PRETTY_FUNCTION__);
        return;    
    }
}

- (void) connection:(NSURLConnection *) connection didFailWithError:(NSError *)error 
{
    DLog(@"%s %@", __PRETTY_FUNCTION__, error);
}

- (void) connection:(NSURLConnection *) connection didReceiveData:(NSData *)data
{
    [self.receivedData appendData:data];
}

- (void) connection:(NSURLConnection *) connection didReceiveResponse:(NSURLResponse *)response
{
    long long contentLength = [response expectedContentLength];
    if (contentLength == NSURLResponseUnknownLength) { contentLength = 0; }
    self.receivedData = [NSMutableData dataWithLength:(NSUInteger) contentLength];
}

- (void) checkStalenessAndSyncDaemons
{
    NSDate * fileDate = [self getFileModificationDate:self.daemonsFilePath];
    NSTimeInterval time = fabs([fileDate timeIntervalSinceNow]);
    if ((time > 86400.0) &&  ([[CommunicationManager instance] isInternetReachable] == YES))
    {
        NSURLRequest *urlRequest = [NSURLRequest requestWithURL:[NSURL URLWithString:@"http://carat.cs.berkeley.edu/daemons.txt"]
                                                    cachePolicy:NSURLRequestReloadIgnoringLocalCacheData
                                                timeoutInterval:60];
        self.connection = [NSURLConnection connectionWithRequest:urlRequest delegate:self];
        if (self.connection == nil) { DLog(@"%s Could not initialize NSURLConnection", __PRETTY_FUNCTION__); }
    } else { DLog(@"%s Cache up-to-date (%@), skipping update.", __PRETTY_FUNCTION__, fileDate); }
}

- (void) initDaemonCache
{
    NSError *error = nil;
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, 
                                                         NSUserDomainMask, 
                                                         YES);
    self.daemonsFilePath = [[paths objectAtIndex:0] stringByAppendingPathComponent:@"daemons.txt"];
    
    //
    // Already existing, load that daemons from file.
    //
    if ([[NSFileManager defaultManager] fileExistsAtPath:self.daemonsFilePath]) 
    {
        [daemonsList removeAllObjects];
        
        NSString *fh = [NSString stringWithContentsOfFile:self.daemonsFilePath 
                                                 encoding:NSUTF8StringEncoding 
                                                    error:NULL];
        DLog(@"%s Cache file already exists with contents %@, loading...", __PRETTY_FUNCTION__, fh);
        for (NSString *line in [fh componentsSeparatedByString:@"\n"]) {
            [daemonsList setObject:@"1" forKey:line];
        }
        return; 
    }
    
    //
    // Try to create one.
    //
    if (![[NSFileManager defaultManager] createFileAtPath:self.daemonsFilePath 
                                                 contents:nil 
                                               attributes:nil])
    {
        DLog(@"%s Could not create daemon cache. %@", __PRETTY_FUNCTION__, error);
        return;
    }
    
    //
    // Set the modification date to a very old date.
    //
    NSDateFormatter *dateFormatter = [[NSDateFormatter alloc]init];
    [dateFormatter setDateFormat:@"yyyy-MM-dd"];
    NSDate* oldDate = [dateFormatter dateFromString:@"1970-01-01"];
    [dateFormatter release];
    NSDictionary *dict = [[[NSDictionary alloc] initWithObjectsAndKeys:oldDate, NSFileModificationDate, nil] autorelease];
    if (![[NSFileManager defaultManager] setAttributes:dict 
                                          ofItemAtPath:self.daemonsFilePath 
                                                 error:nil]) 
    {
        DLog(@"%s Could not set modification date for the daemon cache.", __PRETTY_FUNCTION__);
        return;    
    }

}
#pragma mark -
- (void) initialize
{
    self.lockReportSync = [[[NSLock alloc] init] autorelease];
    
    //  We don't want to create huge number of threads to send 
    //  registrations/samples, so limit them.
    sendStoredDataToServerSemaphore = dispatch_semaphore_create(1);
    
    [self loadLocalReportsToMemory:self.managedObjectContext];
    
    [self initDaemonCache];
}

#pragma mark - Report Syncing
/**
 */
- (BOOL) clearLocalAppReports : (NSManagedObjectContext *) managedObjectContext
                forEntityType : (NSString *) entityType
{
    NSError *error = nil;
    if (managedObjectContext != nil) 
    {
        NSFetchRequest *fetchRequest = [[[NSFetchRequest alloc] init] autorelease];
        NSEntityDescription *entity = [NSEntityDescription entityForName:@"CoreDataAppReport" 
                                                  inManagedObjectContext:managedObjectContext];
        [fetchRequest setEntity:entity];
        
        NSArray *fetchedObjects = [managedObjectContext executeFetchRequest:fetchRequest error:&error];
        if (fetchedObjects == nil) {
            DLog(@"%s Could not fetch app report data, error %@, %@", __PRETTY_FUNCTION__,error, [error userInfo]);
            return NO;
        } 
        
        // NOTE: We don't want to save this unless putting new data is successful. 
        // That will happen in the calling function.
        for (CoreDataAppReport *appReport in fetchedObjects)
        {
            if ([[appReport reportType] isEqualToString:entityType]) [managedObjectContext deleteObject:appReport];
        }
        
        return YES;
    }
    return NO;
}

/**
 */
- (void) updateLocalSubReport: (CoreDataSubReport *) cdataSubReport 
         withThisDetailReport: (DetailScreenReport *) detailScreenReportWith 
          andThatDetailReport: (DetailScreenReport *) detailScreenReportWithout
{
    cdataSubReport.score = detailScreenReportWith.scoreIsSet ? 
    [NSNumber numberWithDouble:detailScreenReportWith.score] :
    [NSNumber numberWithDouble:0.0];
    
    cdataSubReport.expectedValue = detailScreenReportWith.expectedValueIsSet ? 
    [NSNumber numberWithDouble:detailScreenReportWith.expectedValue] :
    [NSNumber numberWithDouble:0.0];
    
    cdataSubReport.expectedValueWithout = detailScreenReportWithout.expectedValueIsSet ? 
    [NSNumber numberWithDouble:detailScreenReportWithout.expectedValue] :
    [NSNumber numberWithDouble:0.0];
    
    cdataSubReport.distributionXWith = detailScreenReportWith.xValsIsSet ? 
    detailScreenReportWith.xVals :
    [[[NSArray alloc] init] autorelease];
    
    cdataSubReport.distributionYWith = detailScreenReportWith.yValsIsSet ? 
    detailScreenReportWith.yVals :
    [[[NSArray alloc] init] autorelease];
    
    cdataSubReport.distributionXWithout = detailScreenReportWithout.xValsIsSet ? 
    detailScreenReportWithout.xVals :
    [[[NSArray alloc] init] autorelease];
    
    cdataSubReport.distributionYWithout = detailScreenReportWithout.yValsIsSet ? 
    detailScreenReportWithout.yVals :
    [[[NSArray alloc] init] autorelease];
}

/**
 * Refresh all the local reports from server. 
 */
- (void) updateReportsFromServer
{
    NSError *error = nil;
    NSString *entityType = nil;
    
    NSManagedObjectContext *managedObjectContext = [[[NSManagedObjectContext alloc] init] autorelease];
    [managedObjectContext setUndoManager:nil];
    [managedObjectContext setPersistentStoreCoordinator:self.persistentStoreCoordinator];
    
    DLog(@"%s Initilializing report syncing...", __PRETTY_FUNCTION__);
    
    if (managedObjectContext != nil) 
    {
        NSFetchRequest *fetchRequest = [[[NSFetchRequest alloc] init] autorelease];
        NSEntityDescription *entity = [NSEntityDescription entityForName:@"CoreDataMainReport" 
                                                  inManagedObjectContext:managedObjectContext];
        [fetchRequest setEntity:entity];
        
        NSArray *fetchedObjects = [managedObjectContext executeFetchRequest:fetchRequest error:&error];
        if (fetchedObjects == nil) {
            DLog(@"%s Could not fetch CoreDataMainReport, error %@, %@", __PRETTY_FUNCTION__, error, [error userInfo]);
            return;
        } 
        
        DLog(@"%s Number of main reports in reports core data store: %u", __PRETTY_FUNCTION__, [fetchedObjects count]);
        
        // Check for sanity.
        if ([fetchedObjects count] == 0)
        {
            DLog(@"%s Reports core data store not initialized. Initing...", __PRETTY_FUNCTION__);
            [self initLocalReportStore : managedObjectContext];
            fetchedObjects = [managedObjectContext executeFetchRequest:fetchRequest error:&error];
        }
        else if ([fetchedObjects count] > 1)    // This should not happen!!!!
        {
            DLog(@"%s Found more than 1 item in main reports core data store!", __PRETTY_FUNCTION__);
            for (CoreDataMainReport *mainReport in fetchedObjects)
            {
                [managedObjectContext deleteObject:mainReport];
                [managedObjectContext save:nil];
            }
            [self initLocalReportStore : managedObjectContext];
            fetchedObjects = [managedObjectContext executeFetchRequest:fetchRequest error:&error];
        }
        
        // Now let's get the report data from the server. First main reports.
        DLog(@"%s Updating main reports...", __PRETTY_FUNCTION__);
        reportUpdateStatus = @"(Updating main reports...)";
        [self postNotificationOnMainThread];
        
        Reports *reports = [[CommunicationManager instance] getReports];
        //if (reports == nil || reports == NULL) return;  // Being extra-cautious.
        if (reports != nil && reports != NULL)
        {
            CoreDataMainReport *cdataMainReport = [fetchedObjects objectAtIndex:0];
            if (cdataMainReport == nil) return;
            
            [cdataMainReport setLastUpdated:[NSDate date]];
            double lastJScore = [[cdataMainReport valueForKey:@"jScore"] doubleValue];
            double change = round((reports.jScore - lastJScore)*100.0);
            double changePercentage = 0.0;
            if (lastJScore > 0.0) {
                changePercentage = change / lastJScore; 
            }
            
            [cdataMainReport setJScore:[NSNumber numberWithDouble:reports.jScore]];
            //NSArray *existing = (NSArray *) [cdataMainReport valueForKey:@"changesSinceLastWeek"];
            //[existing release];
            NSArray *new = [[NSArray alloc] initWithObjects:
                            //[NSString stringWithFormat: @"%.2f", change*100.0],
                            [NSString stringWithFormat: @"%.2f", change],
                            [NSString stringWithFormat: @"%.2f", changePercentage],
                            nil];
            cdataMainReport.changesSinceLastWeek = new;
            
            // Subreports.
            NSSet *subReportsSet = cdataMainReport.subreports;
            NSArray *subReportsArray = [subReportsSet allObjects];
            DLog(@"%s Number of sub reports fetched: %u", __PRETTY_FUNCTION__, [subReportsArray count]);
            
            for (CoreDataSubReport *cdataSubReport in subReportsArray)
            {
                NSString *subReportName = (NSString *) [cdataSubReport valueForKey:@"name"];
                if ([subReportName isEqualToString:@"JScoreInfo"]) 
                {
                    [self updateLocalSubReport:cdataSubReport 
                          withThisDetailReport:reports.jScoreWith 
                           andThatDetailReport:reports.jScoreWithout];
                }
                else if ([subReportName isEqualToString:@"OSInfo"]) 
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
        } else { DLog(@"%s Main report update failed.", __PRETTY_FUNCTION__); }
        
        // Save progress
        @try {
            if ([managedObjectContext hasChanges] && ![managedObjectContext save:&error])
            {
                DLog(@"%s Could not save coredata, error: %@, %@.", __PRETTY_FUNCTION__, error, [error userInfo]);
                return;
            }
        }
        @catch (NSException *exception) {
            DLog(@"%s Exception while trying to save coredata, %@, %@", __PRETTY_FUNCTION__, [exception name], [exception reason]);
        }
        
        entityType = @"Hog";
        // Clear local hog reports.
        if ([self clearLocalAppReports:managedObjectContext forEntityType:entityType] == NO)
            return;
        
        // Hog report
        DLog(@"%s Updating hog report...", __PRETTY_FUNCTION__);
        reportUpdateStatus = @"(Updating hog report...)";
        [self postNotificationOnMainThread];
        
        Feature *feature1 = [[[Feature alloc] init] autorelease];
        [feature1 setKey:@"ReportType"];
        [feature1 setValue:entityType];
        
        Feature *feature2 = [[[Feature alloc] init] autorelease];
        [feature2 setKey:@"Model"];
        UIDeviceHardware *h =[[[UIDeviceHardware alloc] init] autorelease];
        [feature2 setValue: [h platformString]];
        
        FeatureList list = [[NSArray alloc] initWithObjects:feature1,feature2, nil];
        
        HogBugReport *hogReport = [[CommunicationManager instance] getHogOrBugReport:list];
        
        //if (hogReport == nil || hogReport == NULL) return;
        if (hogReport != nil && hogReport != NULL)
        {
            HogsBugsList hogList = hogReport.hbList;
            for(HogsBugs * hog in hogList)
            {
                CoreDataAppReport *cdataAppReport = (CoreDataAppReport *) [NSEntityDescription 
                                                                           insertNewObjectForEntityForName:@"CoreDataAppReport" 
                                                                           inManagedObjectContext:managedObjectContext];
                if (!hog.appNameIsSet) { 
                    DLog([[@"%s App name not set for " stringByAppendingString:entityType] stringByAppendingString:@" report, ignoring..."], __PRETTY_FUNCTION__);
                    continue; 
                }
                
                [cdataAppReport setAppName:hog.appName];
                [cdataAppReport setAppScore:(hog.wDistanceIsSet ? 
                                             [NSNumber numberWithDouble:hog.wDistance] : 
                                             [NSNumber numberWithDouble:0.0])];
                
                [cdataAppReport setExpectedValue:(hog.expectedValueIsSet ? 
                                                  [NSNumber numberWithDouble:hog.expectedValue] :
                                                  [NSNumber numberWithDouble:0.0])];
                [cdataAppReport setExpectedValueWithout:(hog.expectedValueWithoutIsSet ? 
                                                         [NSNumber numberWithDouble:hog.expectedValueWithout] : 
                                                         [NSNumber numberWithDouble:0.0])];
                [cdataAppReport setError:(hog.errorIsSet ?
                                                  [NSNumber numberWithDouble:hog.error] :
                                                  [NSNumber numberWithDouble:0.0])];
                [cdataAppReport setErrorWithout:(hog.errorWithoutIsSet ?
                                          [NSNumber numberWithDouble:hog.errorWithout] :
                                          [NSNumber numberWithDouble:0.0])];
                [cdataAppReport setSamples:(hog.samplesIsSet ?
                                                  [NSNumber numberWithDouble:hog.samples] :
                                                  [NSNumber numberWithDouble:0.0])];
                [cdataAppReport setSamplesWithout:(hog.samplesWithoutIsSet ?
                                            [NSNumber numberWithDouble:hog.samplesWithout] :
                                            [NSNumber numberWithDouble:0.0])];
                [cdataAppReport setReportType:entityType];
                [cdataAppReport setLastUpdated:[NSDate date]];
                CoreDataDetail *cdataDetail = (CoreDataDetail *) [NSEntityDescription 
                                                                  insertNewObjectForEntityForName:@"CoreDataDetail" 
                                                                  inManagedObjectContext:managedObjectContext];
                [cdataDetail setDistance:(hog.wDistanceIsSet ? 
                                          [NSNumber numberWithDouble:hog.wDistance] : 
                                          [NSNumber numberWithDouble:0.0])];
                cdataDetail.distributionXWith = hog.xValsIsSet ? hog.xVals : [[[NSArray alloc] init] autorelease];
                cdataDetail.distributionXWithout = hog.xValsWithoutIsSet ? hog.xValsWithout : [[[NSArray alloc] init] autorelease];
                cdataDetail.distributionYWith = hog.yValsIsSet ? hog.yVals : [[[NSArray alloc] init] autorelease];
                cdataDetail.distributionYWithout = hog.yValsWithoutIsSet ? hog.yValsWithout : [[[NSArray alloc] init] autorelease];
                
                [cdataDetail setAppReport:cdataAppReport];
                [cdataAppReport setAppDetails:cdataDetail];
            }
        } else { 
            DLog([[@"%s " stringByAppendingString:entityType] stringByAppendingString:@" report update failed."], __PRETTY_FUNCTION__);
            [managedObjectContext rollback];
        }
        [list release];
        
        // Save progress
        @try {
            if ([managedObjectContext hasChanges] && ![managedObjectContext save:&error])
            {
                DLog(@"%s Could not save coredata, error: %@, %@.", __PRETTY_FUNCTION__, error, [error userInfo]);
                return;
            }
        }
        @catch (NSException *exception) {
            DLog(@"%s Exception while trying to save coredata, %@, %@", __PRETTY_FUNCTION__, [exception name], [exception reason]);
        }
        
        entityType = @"Bug";
        
        // Clear local bug reports.
        if ([self clearLocalAppReports:managedObjectContext forEntityType:entityType] == NO)
            return;
        
        // Bug report
        DLog(@"%s Updating bug report...", __PRETTY_FUNCTION__);
        reportUpdateStatus = @"(Updating bug report...)";
        [self postNotificationOnMainThread];
        
        [feature1 setValue:entityType];
        list = [[NSArray alloc] initWithObjects:feature1, feature2, nil];
        
        HogBugReport *bugReport = [[CommunicationManager instance] getHogOrBugReport:list];
        //if (bugReport == nil || bugReport == NULL) return;
        if (bugReport != nil && bugReport != NULL)
        {
            HogsBugsList bugList = bugReport.hbList;
            for(HogsBugs * bug in bugList)
            {
                CoreDataAppReport *cdataAppReport = (CoreDataAppReport *) [NSEntityDescription 
                                                                           insertNewObjectForEntityForName:@"CoreDataAppReport" 
                                                                           inManagedObjectContext:managedObjectContext];
                if (!bug.appNameIsSet) { 
                    DLog([[@"%s App name not set for " stringByAppendingString:entityType] stringByAppendingString:@" report, ignoring..."], __PRETTY_FUNCTION__);
                    continue; 
                }
                
                [cdataAppReport setAppName:bug.appName];
                [cdataAppReport setAppScore:(bug.wDistanceIsSet ? 
                                             [NSNumber numberWithDouble:bug.wDistance] : 
                                             [NSNumber numberWithDouble:0.0])];
                
                [cdataAppReport setExpectedValue:(bug.expectedValueIsSet ? 
                                                  [NSNumber numberWithDouble:bug.expectedValue] :
                                                  [NSNumber numberWithDouble:0.0])];
                [cdataAppReport setExpectedValueWithout:(bug.expectedValueWithoutIsSet ? 
                                                         [NSNumber numberWithDouble:bug.expectedValueWithout] : 
                                                         [NSNumber numberWithDouble:0.0])];
                [cdataAppReport setError:(bug.errorIsSet ?
                                          [NSNumber numberWithDouble:bug.error] :
                                          [NSNumber numberWithDouble:0.0])];
                [cdataAppReport setErrorWithout:(bug.errorWithoutIsSet ?
                                                 [NSNumber numberWithDouble:bug.errorWithout] :
                                                 [NSNumber numberWithDouble:0.0])];
                [cdataAppReport setSamples:(bug.samplesIsSet ?
                                            [NSNumber numberWithDouble:bug.samples] :
                                            [NSNumber numberWithDouble:0.0])];
                [cdataAppReport setSamplesWithout:(bug.samplesWithoutIsSet ?
                                                   [NSNumber numberWithDouble:bug.samplesWithout] :
                                                   [NSNumber numberWithDouble:0.0])];
                [cdataAppReport setReportType:entityType];
                [cdataAppReport setLastUpdated:[NSDate date]];
                CoreDataDetail *cdataDetail = (CoreDataDetail *) [NSEntityDescription 
                                                                  insertNewObjectForEntityForName:@"CoreDataDetail" 
                                                                  inManagedObjectContext:managedObjectContext];
                [cdataDetail setDistance:(bug.wDistanceIsSet ? 
                                          [NSNumber numberWithDouble:bug.wDistance] : 
                                          [NSNumber numberWithDouble:0.0])];
                cdataDetail.distributionXWith = bug.xValsIsSet ? bug.xVals : [[[NSArray alloc] init] autorelease];
                cdataDetail.distributionXWithout = bug.xValsWithoutIsSet ? bug.xValsWithout : [[[NSArray alloc] init] autorelease];
                cdataDetail.distributionYWith = bug.yValsIsSet ? bug.yVals : [[[NSArray alloc] init] autorelease];
                cdataDetail.distributionYWithout = bug.yValsWithoutIsSet ? bug.yValsWithout : [[[NSArray alloc] init] autorelease];

                [cdataDetail setAppReport:cdataAppReport];
                [cdataAppReport setAppDetails:cdataDetail];
            }
        } else {
            DLog([[@"%s " stringByAppendingString:entityType] stringByAppendingString:@" report update failed."], __PRETTY_FUNCTION__);
            [managedObjectContext rollback];
        }
        [list release];
        
        // Save progress
        @try {
            if ([managedObjectContext hasChanges] && ![managedObjectContext save:&error])
            {
                DLog(@"%s Could not save coredata, error: %@, %@.", __PRETTY_FUNCTION__, error, [error userInfo]);
                return;
            }
        }
        @catch (NSException *exception) {
            DLog(@"%s Exception while trying to save coredata, %@, %@", __PRETTY_FUNCTION__, [exception name], [exception reason]);
        }
        
        // Reload data in memory.
        [self loadLocalReportsToMemory : managedObjectContext];
    }    
}

#pragma mark - Registration & Sampling
/**
 *  Get the list of running processes and put it in core data. Note that we
 *  don't call save on the managed object here, as we will do a final call to
 *  save the entire sample.
 */
- (void) sampleProcessInfo : (CoreDataSample *) currentCDSample 
  withManagedObjectContext : (NSManagedObjectContext *) managedObjectContext
{   
    if (managedObjectContext != nil)
    {
        NSArray *processes = [[UIDevice currentDevice] runningProcesses];
        
        for (NSDictionary *dict in processes)
        {
            if([daemonsList objectForKey:[dict objectForKey:@"ProcessName"]] != nil) { continue; }
            CoreDataProcessInfo *cdataProcessInfo = (CoreDataProcessInfo *) [NSEntityDescription insertNewObjectForEntityForName:@"CoreDataProcessInfo" inManagedObjectContext:managedObjectContext];
            [cdataProcessInfo setId: [NSNumber numberWithInt:[[dict objectForKey:@"ProcessID"] intValue]]];
            [cdataProcessInfo setName:[dict objectForKey:@"ProcessName"]];
            [cdataProcessInfo setCoredatasample:currentCDSample];
            [currentCDSample addProcessInfosObject:cdataProcessInfo];
        }
    }
}

/**
 *  Do a sampling while the app is in the foreground. There shouldn't be any
 *  restrictions on CPU usage as we are active. 
 *  NOTE: This method is called on a new thread.
 */
- (void) sampleForeground : (NSString *) triggeredBy
{
    NSError *error = nil;
    
    NSManagedObjectContext *managedObjectContext = [[[NSManagedObjectContext alloc] init] autorelease];
    [managedObjectContext setUndoManager:nil];
    [managedObjectContext setPersistentStoreCoordinator:self.persistentStoreCoordinator];
    
    if (managedObjectContext == nil) { return; }
    
    CoreDataSample *cdataSample = (CoreDataSample *) [NSEntityDescription insertNewObjectForEntityForName:@"CoreDataSample" 
                                                                                   inManagedObjectContext:managedObjectContext];
    [cdataSample setTriggeredBy:triggeredBy];
    [cdataSample setTimestamp:[NSNumber numberWithDouble:
                               [[Globals instance] utcSecondsSinceEpoch]]];
    [cdataSample setNetworkStatus:[[CommunicationManager instance] networkStatusString]];
    
    [cdataSample setDistanceTraveled:0];
    if ([triggeredBy isEqualToString:@"didUpdateToLocation"])
        [cdataSample setDistanceTraveled:[NSNumber numberWithDouble:[[Globals instance] getDistanceTraveled]]];
    
    //
    //  Running processes.
    //
    [self sampleProcessInfo:cdataSample withManagedObjectContext:managedObjectContext];
    
    //
    //  Battery state and level.
    //
    [cdataSample setBatteryLevel:[NSNumber numberWithFloat:
                                  [UIDevice currentDevice].batteryLevel]];
    [cdataSample setBatteryState:[UIDevice currentDevice].batteryStateString];
    
    //
    //  Memory info.
    //
    mach_msg_type_number_t count = HOST_VM_INFO_COUNT;
    vm_statistics_data_t vmstat;
    if (host_statistics(mach_host_self(), HOST_VM_INFO, (host_info_t)&vmstat, &count) != KERN_SUCCESS)
    {
        DLog(@"Failed to get VM statistics.");
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
    @try 
    {
        if ([managedObjectContext hasChanges] && ![managedObjectContext save:&error])
        {
            DLog(@"%s Could not save sample in coredata, error: %@, %@.", __PRETTY_FUNCTION__, error, [error userInfo]);
            [FlurryAnalytics logEvent:@"sampleForeground Error"
                       withParameters:[NSDictionary dictionaryWithObjectsAndKeys:[NSString stringWithFormat:@"Unresolved error %@, %@", error, [error userInfo]], @"Error Info", nil]];
        }
    }
    @catch (NSException *exception) {
        DLog(@"%s Exception while trying to save coredata, details: %@, %@", 
             __PRETTY_FUNCTION__, [exception name], [exception reason]);
    }
}

- (void) sampleBackground : (NSString *) triggeredBy
{
    // REMEMBER. We are running in the background if this is being executed.
    // We can't assume normal network access.
    // bgTask is defined as an instance variable of type UIBackgroundTaskIdentifier
    
    // Note that the expiration handler block simply ends the task. It is important that we always
    // end tasks that we have started.
    
    UIApplication *app = [UIApplication sharedApplication];
    UIBackgroundTaskIdentifier bgTask = 0;
    bgTask = [app
              beginBackgroundTaskWithExpirationHandler:^{
                  [app endBackgroundTask:bgTask];
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

/**
 *  Retrieve and send registration messages to the server. Assumes that this 
 *  method is called in a different thread, so creates its own 
 *  managedobjectcontext. 
 */
- (void) fetchAndSendRegistrations : (NSUInteger) limitMessagesTo
{
    NSError *error = nil;
    
    NSManagedObjectContext *managedObjectContext = [[[NSManagedObjectContext alloc] init] autorelease];
    [managedObjectContext setUndoManager:nil];
    [managedObjectContext setPersistentStoreCoordinator:self.persistentStoreCoordinator];
    
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
        
        NSUInteger count = [managedObjectContext countForFetchRequest:fetchRequest error:&error];
        if (!error) {
            DLog(@"%s Total registrations in store: %d", __PRETTY_FUNCTION__, count);
        }
        
        [fetchRequest setFetchLimit:limitMessagesTo];  
        
        NSArray *fetchedObjects = [managedObjectContext executeFetchRequest:fetchRequest error:&error];
        if (fetchedObjects == nil) {
            DLog(@"%s Could not fetch registrations, error %@, %@", __PRETTY_FUNCTION__, error, [error userInfo]);
            goto cleanup;
        } 
        
        DLog(@"%s # registrations fetched: %u", __PRETTY_FUNCTION__, [fetchedObjects count]);
        
        for (CoreDataRegistration *registration in fetchedObjects)
        {
            if (registration == nil) 
                break;
            
            Registration* registrationToSend = [[[Registration alloc] init] autorelease];
            registrationToSend.uuId = [[Globals instance] getUUID ];
            registrationToSend.timestamp = [[registration valueForKey:@"timestamp"] doubleValue]; 
            registrationToSend.platformId = (NSString*) [registration valueForKey:@"platformId"];
            registrationToSend.systemVersion = (NSString*) [registration valueForKey:@"systemVersion"]; 
            
            DLog(@"%s\ttimestamp: %f", __PRETTY_FUNCTION__, registrationToSend.timestamp);
            DLog(@"%s\tplatformId: %@", __PRETTY_FUNCTION__,registrationToSend.platformId);
            DLog(@"%s\tsystemVersion: %@", __PRETTY_FUNCTION__,registrationToSend.systemVersion);
            
            //
            //  Try to send. If successful, delete. 
            //
            BOOL ret = [[CommunicationManager instance] sendRegistrationMessage:registrationToSend];
            if (ret == YES) 
            {
                [managedObjectContext deleteObject:registration];
            }
            [NSThread sleepForTimeInterval:0.1];
        }
        
    cleanup:
        [sortDescriptors release];
        [sortDescriptor release];
        
        @try {
            if ([managedObjectContext hasChanges] && ![managedObjectContext save:&error])
            {
                DLog(@"%s Could not delete registrations from coredata, error: %@, %@.", __PRETTY_FUNCTION__, error, [error userInfo]);
                return;
            }
        }
        @catch (NSException *exception) {
            DLog(@"%s Exception while trying to save coredata, details: %@, %@", 
                 __PRETTY_FUNCTION__, [exception name], [exception reason]);
        }
    }
}

/**
 *  Retrieve and send samples to the server. Assumes that this method is called
 *  in a different thread, so creates its own managedobjectcontext. 
 */
- (void) fetchAndSendSamples : (NSUInteger) limitSamplesTo
{
    NSError *error = nil;
    
    NSManagedObjectContext *managedObjectContext = [[[NSManagedObjectContext alloc] init] autorelease];
    [managedObjectContext setUndoManager:nil];
    [managedObjectContext setPersistentStoreCoordinator:self.persistentStoreCoordinator];
    
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
        
        NSUInteger count = [managedObjectContext countForFetchRequest:fetchRequest error:&error];
        if (!error) {
            DLog(@"%s Total samples in store: %d", __PRETTY_FUNCTION__, count);
        }
        
        [fetchRequest setFetchLimit:limitSamplesTo];  
        
        NSArray *fetchedObjects = [managedObjectContext executeFetchRequest:fetchRequest error:&error];
        if (fetchedObjects == nil) {
            DLog(@"%s Could not fetch samples, error %@, %@", __PRETTY_FUNCTION__,error, [error userInfo]);
            goto cleanup;
        } 
        
        DLog(@"%s Number of samples fetched: %u",__PRETTY_FUNCTION__,[fetchedObjects count]);
        
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
            sampleToSend.networkStatus = (NSString *) [sample valueForKey:@"networkStatus"];
            sampleToSend.distanceTraveled = (double) [[sample valueForKey:@"distanceTraveled"] doubleValue];
            
            NSMutableArray *pInfoList = [[[NSMutableArray alloc] init] autorelease];
            sampleToSend.piList = pInfoList;
            
            DLog(@"%s\ttimestamp: %f",__PRETTY_FUNCTION__, sampleToSend.timestamp);
            DLog(@"%s\tbatteryLevel: %@",__PRETTY_FUNCTION__, [sample valueForKey:@"batteryLevel"]);
            DLog(@"%s\tbatteryState: %@",__PRETTY_FUNCTION__, [sample valueForKey:@"batteryState"]);
            DLog(@"%s\ttriggeredBy: %@",__PRETTY_FUNCTION__, sampleToSend.triggeredBy);
            DLog(@"%s\tnetworkStatus: %@",__PRETTY_FUNCTION__, sampleToSend.networkStatus);
            DLog(@"%s\tdistanceTraveled: %f",__PRETTY_FUNCTION__, sampleToSend.distanceTraveled);

            
            if (sample.processInfos == nil || sample.processInfos == NULL) {
                DLog(@"%s Process Info list is Nil in the sample!!", __PRETTY_FUNCTION__);
                [managedObjectContext deleteObject:sample];
                continue;
            }
            
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
                // Lock core data and delete stuff.
                //if (![managedObjectContext save:&error])
                //{
                //    DLog(@"%s Could not delete sample from coredata, error %@, %@", __PRETTY_FUNCTION__,error, [error userInfo]);
                //}
            }
            [NSThread sleepForTimeInterval:0.1];
        }
        
    cleanup:
        [sortDescriptors release];
        [sortDescriptor release];
        
        @try {
            if ([managedObjectContext hasChanges] && ![managedObjectContext save:&error])
            {
                DLog(@"%s Could not delete samples, error: %@, %@.", __PRETTY_FUNCTION__, error, [error userInfo]);
                return;
            }
        }
        @catch (NSException *exception) {
            DLog(@"%s Exception while trying to save coredata, details: %@, %@", 
                 __PRETTY_FUNCTION__, [exception name], [exception reason]);
        }
    }
}


@end

#pragma mark -

@implementation CoreDataManager

@synthesize managedObjectContext = __managedObjectContext;
@synthesize managedObjectModel = __managedObjectModel;
@synthesize persistentStoreCoordinator = __persistentStoreCoordinator;
@synthesize fetchedResultsController = __fetchResultsController;
@synthesize LastUpdatedDate;
@synthesize JScoreInfo;
@synthesize JScoreInfoWithout;
@synthesize OSInfo;
@synthesize OSInfoWithout;
@synthesize ModelInfo;
@synthesize ModelInfoWithout;
@synthesize SimilarAppsInfo;
@synthesize SimilarAppsInfoWithout;
@synthesize ChangesSinceLastWeek;
@synthesize connection;
@synthesize receivedData;
@synthesize lockReportSync;
@synthesize daemonsFilePath;

static id instance = nil;

+ (void) initialize {
    if (self == [CoreDataManager class]) {
        instance = [[self alloc] init];
    }
    SubReports = [[NSArray alloc] initWithObjects:@"JScoreInfo",@"OSInfo",@"ModelInfo",@"SimilarAppsInfo", nil];
    daemonsList = [[NSMutableDictionary alloc] init];
    [instance initialize];
    //[instance loadLocalReportsToMemory];
    [[CommunicationManager instance] isInternetReachable]; // This is here just to make sure CommunicationManager subscribes 
                                                           // to reachability updates.
}

+ (id) instance {
    return instance;
}

- (void) dealloc
{
    [__managedObjectContext release];
    [__managedObjectModel release];
    [__persistentStoreCoordinator release];
    [__fetchResultsController release];
    [LastUpdatedDate release];
    [JScoreInfo release];
    [JScoreInfoWithout release];
    [OSInfo release];
    [OSInfoWithout release];
    [ModelInfo release];
    [ModelInfoWithout release];
    [SimilarAppsInfo release];
    [SimilarAppsInfoWithout release];
    [ChangesSinceLastWeek release];
    [lockReportSync release];
    [daemonsFilePath release];
    [daemonsList release];
    dispatch_release(sendStoredDataToServerSemaphore);
    [super dealloc];
}

#pragma mark - Registration & Sampling
/**
 * Generate and save registration information in the database.
 */
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
        DLog(@"%s Unresolved error %@, %@", __PRETTY_FUNCTION__,error, [error userInfo]);
    } 
}

/**
 *  Take a sample.
 */
- (void) sampleNow : (NSString *) triggeredBy
{
    dispatch_async( dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
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
    });    
}

/**
 *  Check for internet connectivity and send sample & registration messages to 
 *  the server.
 */
- (void) checkConnectivityAndSendStoredDataToServer
{
    if ([[CommunicationManager instance] isInternetReachable] == YES)
    {
        DLog(@"%s Internet connection active", __PRETTY_FUNCTION__);
        long available = dispatch_semaphore_wait(sendStoredDataToServerSemaphore, DISPATCH_TIME_NOW);
        if (available != 0)
        {
            DLog(@"%s Not enough resources available, aborting.", __PRETTY_FUNCTION__);
            return;
        }
        
        dispatch_async( dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
            [self fetchAndSendRegistrations:10];
            [self fetchAndSendSamples:10];
            dispatch_async( dispatch_get_main_queue(), ^{
                dispatch_semaphore_signal(sendStoredDataToServerSemaphore);
                DLog(@"%s Done!", __PRETTY_FUNCTION__);
            });
        });
    } 
    else { DLog(@"%s No connectivity", __PRETTY_FUNCTION__); }
}

#pragma mark - Report Syncing

//
// Send stored data in coredatastore to the server. First, we 
// send all the registration messages. Then we send samples.
//
/*- (void) sendStoredDataToServer : (NSUInteger) limitEntriesTo
 {
 //if ([lockCoreDataStore tryLock]) {
 [self fetchAndSendRegistrations: limitEntriesTo];
 [self fetchAndSendSamples:limitEntriesTo];
 //  [lockCoreDataStore unlock];
 //}
 }*/

/**
 *  Update reports from Carat Server.
 */
- (void) updateLocalReportsFromServer
{
    /*if ([lockCoreDataStore tryLock]) {
        [self updateReportsFromServer];
        [lockCoreDataStore unlock];
    }*/
    //
    /*dispatch_async( dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        if ([lockReportSync tryLock]) {
            [self checkStalenessAndSyncDaemons];
            [self updateReportsFromServer];
            reportUpdateStatus = nil;
            [lockReportSync unlock];
        } else {
            DLog(@"%s Could not get a lock on report syncing module. Perhaps an update is already in progress?", __PRETTY_FUNCTION__);
        }
        dispatch_async( dispatch_get_main_queue(), ^{
            DLog(@"%s Report fetching thread completed!", __PRETTY_FUNCTION__);
        });
    });*/
    if ([lockReportSync tryLock]) 
    {
        dispatch_async( dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
                [self updateReportsFromServer];
                reportUpdateStatus = nil;
                [self postNotificationOnMainThread];
                
                dispatch_async( dispatch_get_main_queue(), ^{
                    [lockReportSync unlock];
                    [self checkStalenessAndSyncDaemons];
                    DLog(@"%s Report fetching thread completed!", __PRETTY_FUNCTION__);
                });
        });
    } 
    else 
    { 
        DLog(@"%s Could not get a lock on report syncing module. Perhaps an update is already in progress?", __PRETTY_FUNCTION__);
    }
}

#pragma mark - UI APIs
/**
 * Get the time stamp from the last successful report update.
 */
- (NSDate *) getLastReportUpdateTimestamp
{
    if (self.LastUpdatedDate != nil) 
    {
        DLog(@"%s %@", __PRETTY_FUNCTION__, self.LastUpdatedDate);
        return self.LastUpdatedDate;
    } 
    else
    {
        NSDateFormatter *dateFormatter = [[[NSDateFormatter alloc] init] autorelease];
        [dateFormatter setDateFormat:@"yyyy-MM-dd"];
        DLog(@"%s LastUpdateDate null, defaulting to %@", __PRETTY_FUNCTION__, [dateFormatter dateFromString:@"1970-01-01"]);
        return [dateFormatter dateFromString:@"1970-01-01"];
    }
}

/**
 * Return number of seconds since last successful report update.
 */
- (double) secondsSinceLastUpdate
{
    NSTimeInterval interval = 1000000.0;
    if (self.LastUpdatedDate != nil)
    {
        NSDate *now = [NSDate date];
        DLog(@"CurrentDate: %@ LastUpdatedDate: %@", now, self.LastUpdatedDate );
        interval = [now timeIntervalSinceDate:self.LastUpdatedDate];
    }
    DLog(@"%s %@", __PRETTY_FUNCTION__, [NSString stringWithFormat:@"%g",interval]);
    return interval;
}

/**
 * Fetch the list of bugs from core data.
 */
- (HogBugReport *) getBugs : (BOOL) filterNonRunning withoutHidden : (BOOL) filterHidden
{
    DLog(@"Getting bugs from core data...");
    NSError *error = nil;
    NSArray *runningProcessNames = nil;
    NSArray *hiddenProcessNames = [[Globals instance] getHiddenApps];
    NSManagedObjectContext *managedObjectContext = self.managedObjectContext;
    
    if (managedObjectContext != nil) 
    {
        NSFetchRequest *fetchRequest = [[[NSFetchRequest alloc] init] autorelease];
        NSPredicate *predicate = [NSPredicate predicateWithFormat:@"reportType == %@", @"Bug"];
        [fetchRequest setPredicate:predicate];
        NSSortDescriptor *sortDescriptor = [[[NSSortDescriptor alloc] initWithKey:@"appScore" 
                                                                        ascending:NO] autorelease];
        NSArray *sortDescriptors = [NSArray arrayWithObject:sortDescriptor];
        [fetchRequest setSortDescriptors:sortDescriptors];
        
        NSEntityDescription *entity = [NSEntityDescription entityForName:@"CoreDataAppReport" 
                                                  inManagedObjectContext:managedObjectContext];
        [fetchRequest setEntity:entity];
        
        NSArray *fetchedObjects = [managedObjectContext executeFetchRequest:fetchRequest error:&error];
        if (fetchedObjects == nil) {
            DLog(@"%s Could not fetch app report data, error %@, %@", __PRETTY_FUNCTION__,error, [error userInfo]);
            return nil;
        }
        
        DLog(@"%s Found %d bug, loading...",__PRETTY_FUNCTION__, [fetchedObjects count]);
        
        if (filterNonRunning) { runningProcessNames = [[UIDevice currentDevice] runningProcessNames]; }
        
        HogBugReport * bugs = [[[HogBugReport alloc] init] autorelease];
        NSMutableArray * hbList = [[[NSMutableArray alloc] init] autorelease];
        [bugs setHbList:hbList];
        for (CoreDataAppReport *cdataAppReport in fetchedObjects)
        {
            if ((filterNonRunning) && (![runningProcessNames containsObject:[cdataAppReport valueForKey:@"appName"]]))
            {
                DLog(@"%s '%@' not in running processes, filtering it out.", 
                     __PRETTY_FUNCTION__,
                     [cdataAppReport valueForKey:@"appName"]);
                continue; 
            }
            
            if ((filterHidden) && ([hiddenProcessNames containsObject:[cdataAppReport valueForKey:@"appName"]]))
            {
                DLog(@"%s '%@' hidden by user, filtering it out.",
                     __PRETTY_FUNCTION__,
                     [cdataAppReport valueForKey:@"appName"]);
                continue;
            }

            HogsBugs *bug = [[[HogsBugs alloc] init] autorelease];
            [bug setAppName:[cdataAppReport valueForKey:@"appName"]];
            [bug setWDistance:[[cdataAppReport valueForKey:@"appScore"] doubleValue]];
            [bug setExpectedValue:[[cdataAppReport valueForKey:@"expectedValue"] doubleValue]];
            [bug setExpectedValueWithout:[[cdataAppReport valueForKey:@"expectedValueWithout"] doubleValue]];
            CoreDataDetail *cdataDetail = cdataAppReport.appDetails;
            [bug setXVals:(NSArray *) [cdataDetail valueForKey:@"distributionXWith"]];
            [bug setXValsWithout:(NSArray *) [cdataDetail valueForKey:@"distributionXWithout"]];
            [bug setYVals:(NSArray *) [cdataDetail valueForKey:@"distributionYWith"]];
            [bug setYValsWithout:(NSArray *) [cdataDetail valueForKey:@"distributionYWithout"]];
            
            [bug setError:[[cdataAppReport valueForKey:@"error"] doubleValue]];
            [bug setErrorWithout:[[cdataAppReport valueForKey:@"errorWithout"] doubleValue]];
            [bug setSamples:[[cdataAppReport valueForKey:@"samples"] doubleValue]];
            [bug setSamplesWithout:[[cdataAppReport valueForKey:@"samplesWithout"] doubleValue]];
            
            [hbList addObject:bug];
            
            DLog(@"%s '%@' action list bug candidate: %.9f %.9f", __PRETTY_FUNCTION__, [cdataAppReport valueForKey:@"appName"], [bug error], [bug errorWithout]);
        }
        return bugs;
    }
    return nil;
}

/**
 * Fetch the list of hogs from core data.
 */
- (HogBugReport *) getHogs : (BOOL) filterNonRunning withoutHidden : (BOOL) filterHidden
{
    DLog(@"Getting hogs from core data...");
    NSError *error = nil;
    NSArray *runningProcessNames = nil;
    NSArray *hiddenProcessNames = [[Globals instance] getHiddenApps];
    NSManagedObjectContext *managedObjectContext = self.managedObjectContext;
    if (managedObjectContext != nil) 
    {
        NSFetchRequest *fetchRequest = [[[NSFetchRequest alloc] init] autorelease];
        NSPredicate *predicate = [NSPredicate predicateWithFormat:@"reportType == %@", @"Hog"];
        [fetchRequest setPredicate:predicate];
        NSSortDescriptor *sortDescriptor = [[[NSSortDescriptor alloc] initWithKey:@"appScore" 
                                                                        ascending:NO] autorelease];
        NSArray *sortDescriptors = [NSArray arrayWithObject:sortDescriptor];
        [fetchRequest setSortDescriptors:sortDescriptors];
        
        NSEntityDescription *entity = [NSEntityDescription entityForName:@"CoreDataAppReport" 
                                                  inManagedObjectContext:managedObjectContext];
        [fetchRequest setEntity:entity];
        
        NSArray *fetchedObjects = [managedObjectContext executeFetchRequest:fetchRequest error:&error];
        if (fetchedObjects == nil) {
            DLog(@"%s Could not fetch app report data, error %@, %@", __PRETTY_FUNCTION__,error, [error userInfo]);
            return nil;
        }
        
        DLog(@"%s Found %d hogs, loading...",__PRETTY_FUNCTION__, [fetchedObjects count]);
        if ([fetchedObjects count] == 0)
            return nil;
        
        if (filterNonRunning) { runningProcessNames = [[UIDevice currentDevice] runningProcessNames]; }
        
        HogBugReport * hogs = [[[HogBugReport alloc] init] autorelease];
        NSMutableArray * hbList = [[[NSMutableArray alloc] init] autorelease];
        [hogs setHbList:hbList];
        for (CoreDataAppReport *cdataAppReport in fetchedObjects)
        {
            if ((filterNonRunning) && (![runningProcessNames containsObject:[cdataAppReport valueForKey:@"appName"]]))
            {
                DLog(@"%s '%@' not in running processes, filtering it out.", 
                     __PRETTY_FUNCTION__,
                     [cdataAppReport valueForKey:@"appName"]);
                continue; 
            }
            
            if ((filterHidden) && ([hiddenProcessNames containsObject:[cdataAppReport valueForKey:@"appName"]]))
            {
                DLog(@"%s '%@' hidden by user, filtering it out.",
                     __PRETTY_FUNCTION__,
                     [cdataAppReport valueForKey:@"appName"]);
                continue;
            }

            HogsBugs *hog = [[[HogsBugs alloc] init] autorelease];
            [hog setAppName:[cdataAppReport valueForKey:@"appName"]];
            [hog setWDistance:[[cdataAppReport valueForKey:@"appScore"] doubleValue]];
            [hog setExpectedValue:[[cdataAppReport valueForKey:@"expectedValue"] doubleValue]];
            [hog setExpectedValueWithout:[[cdataAppReport valueForKey:@"expectedValueWithout"] doubleValue]];
            CoreDataDetail *cdataDetail = cdataAppReport.appDetails;
            [hog setXVals:(NSArray *) [cdataDetail valueForKey:@"distributionXWith"]];
            [hog setXValsWithout:(NSArray *) [cdataDetail valueForKey:@"distributionXWithout"]];
            [hog setYVals:(NSArray *) [cdataDetail valueForKey:@"distributionYWith"]];
            [hog setYValsWithout:(NSArray *) [cdataDetail valueForKey:@"distributionYWithout"]];
            
            [hog setError:[[cdataAppReport valueForKey:@"error"] doubleValue]];
            [hog setErrorWithout:[[cdataAppReport valueForKey:@"errorWithout"] doubleValue]];
            [hog setSamples:[[cdataAppReport valueForKey:@"samples"] doubleValue]];
            [hog setSamplesWithout:[[cdataAppReport valueForKey:@"samplesWithout"] doubleValue]];
            
            [hbList addObject:hog];
            
            DLog(@"%s '%@' action list hog candidate: %.9f %.9f", __PRETTY_FUNCTION__, [cdataAppReport valueForKey:@"appName"], [hog error], [hog errorWithout]);
        }
        return hogs;
    } else {
        DLog(@"... managed object context was nil.");
        return nil;
    }
}
/*- (HogBugReport *) getHogs
{
    //HogBugReport* hogs = nil;
    //if ([lockCoreDataStore tryLock]) {
    //    hogs = [self getHogsFromCoreData];
    //    [lockCoreDataStore unlock];
    //} else { DLog(@"%s Cannot get lock, sending nil for hog report.", __PRETTY_FUNCTION__);}
    //return hogs;
    return [self getHogsFromCoreData];
}*/

- (double) getJScore
{
    //DLog(@"%s %f", __PRETTY_FUNCTION__, JScore);
    return JScore;
}

- (DetailScreenReport *) getJScoreInfo : (BOOL) with
{
    if (with) 
    {
        if (self.JScoreInfo != nil)
            return self.JScoreInfo;
    } 
    else 
    {
        if (self.JScoreInfoWithout != nil)
            return self.JScoreInfoWithout;
    }
    return nil;
}

- (DetailScreenReport *) getOSInfo : (BOOL) with
{
    if (with == YES) {
        if (self.OSInfo != nil)
            return self.OSInfo;
    }
    if (with == NO) { 
        if (self.OSInfoWithout != nil)
            return self.OSInfoWithout;
    }
    return nil;
}

- (DetailScreenReport *) getModelInfo : (BOOL) with
{
    if (with == YES) {
        if (self.ModelInfo != nil)
            return self.ModelInfo;
    }
    
    if (with == NO) { 
        if (self.ModelInfoWithout != nil)
            return self.ModelInfoWithout;
    }
    return nil;
}

- (DetailScreenReport *) getSimilarAppsInfo : (BOOL) with
{
    if (with == YES) {
        if (self.SimilarAppsInfo != nil)
            return self.SimilarAppsInfo;
    }
    if (with == NO) {
        if (self.SimilarAppsInfoWithout != nil)
            return self.SimilarAppsInfoWithout;
    }
    return nil;
}

- (NSArray *) getChangeSinceLastWeek
{
    if (ChangesSinceLastWeek == nil)
    {
        return [[[NSArray alloc] initWithObjects:@"0.0",@"0.0", nil] autorelease];
    }
    return ChangesSinceLastWeek;
}

/**
 *  Generate and return a sample. 
 *  TODO: add memory related stuff.
 */
- (Sample *) getSample
{
    Sample *sample = [[[Sample alloc] init] autorelease];
    [sample setUuId:[[Globals instance] getUUID]];
    [sample setTimestamp:[[Globals instance] utcSecondsSinceEpoch]];
    [sample setBatteryLevel:[UIDevice currentDevice].batteryLevel];
    [sample setBatteryState:[UIDevice currentDevice].batteryStateString];
    
    NSMutableArray *pInfoList = [[[NSMutableArray alloc] init] autorelease];
    [sample setPiList: pInfoList];
    
    NSArray *processes = [[UIDevice currentDevice] runningProcesses];
    for (NSDictionary *dict in processes)
    {
        ProcessInfo *pInfo = [[[ProcessInfo alloc] init] autorelease];
        pInfo.pId = [[dict objectForKey:@"ProcessID"] intValue];
        pInfo.pName = (NSString *)[dict objectForKey:@"ProcessName"];
        [pInfoList addObject:pInfo];
    }
    return sample;
}

/**
 *  The current status of the local reports. 
 */
- (NSString *) getReportUpdateStatus
{
    return reportUpdateStatus;
}

/* 
 * Wipe the database.
 */
- (void) wipeDB
{
    NSURL *storeURL = [[self applicationDocumentsDirectory] URLByAppendingPathComponent:@"Carat.sqlite"];
    DLog(@"%s Wiping database at location: %@", __PRETTY_FUNCTION__, storeURL);
    [[NSFileManager defaultManager] removeItemAtURL:storeURL error:nil];
    [__persistentStoreCoordinator release];
    __persistentStoreCoordinator = nil; 
}

// TODO I temporarily abandoned this effort because this call blocks
// I need to call it in a separate thread, but I don't want to spawn a dozen of them all at once. This belongs as a background task similar to uploading samples. Another day...
- (UIImage *) getIconForApp: (NSString *)appName
{
    NSString *iconURL = [[@"https://s3.amazonaws.com/carat.icons/" stringByAppendingString:appName] stringByAppendingString:@".jpg"];
    DLog(@"Getting icon at %@", iconURL);
    return [[[UIImage alloc] initWithData:[NSData dataWithContentsOfURL:[NSURL URLWithString:iconURL]]] autorelease];
}


//
//  Get information about the memory.
//
- (void) printMemoryInfo
{
    int pagesize = [[UIDevice currentDevice] pageSize];
    DLog(@"Memory Info");
    DLog(@"-----------");
    DLog(@"Page size = %d bytes", pagesize);
    
    mach_msg_type_number_t count = HOST_VM_INFO_COUNT;
    
    vm_statistics_data_t vmstat;
    if (host_statistics(mach_host_self(), HOST_VM_INFO, (host_info_t)&vmstat, &count) != KERN_SUCCESS)
    {
        DLog(@"Failed to get VM statistics.");
    }
    
    double total = vmstat.wire_count + vmstat.active_count + vmstat.inactive_count + vmstat.free_count;
    double wired = vmstat.wire_count / total;
    double active = vmstat.active_count / total;
    double inactive = vmstat.inactive_count / total;
    double free = vmstat.free_count / total;
    
    DLog(@"Total =    %8d pages", vmstat.wire_count + vmstat.active_count + vmstat.inactive_count + vmstat.free_count);
    
    DLog(@"Wired =    %8d bytes", vmstat.wire_count * pagesize);
    DLog(@"Active =   %8d bytes", vmstat.active_count * pagesize);
    DLog(@"Inactive = %8d bytes", vmstat.inactive_count * pagesize);
    DLog(@"Free =     %8d bytes", vmstat.free_count * pagesize);
    
    DLog(@"Total =    %8d bytes", (vmstat.wire_count + vmstat.active_count + vmstat.inactive_count + vmstat.free_count) * pagesize);
    
    DLog(@"Wired =    %0.2f %%", wired * 100.0);
    DLog(@"Active =   %0.2f %%", active * 100.0);
    DLog(@"Inactive = %0.2f %%", inactive * 100.0);
    DLog(@"Free =     %0.2f %%", free * 100.0);
    
    DLog(@"Physical memory = %8d bytes", [[UIDevice currentDevice] totalMemory]);
    DLog(@"User memory =     %8d bytes", [[UIDevice currentDevice] userMemory]);
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
    DLog(@"%s Coredatastore location: %@", __PRETTY_FUNCTION__, storeURL);
    
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
                   withParameters:[NSDictionary dictionaryWithObjectsAndKeys:[NSString stringWithFormat:@"Unresolved error %@, %@, trying to fix by deleting persistent store", error, [error userInfo]], @"Error Info", nil]];
        
        [[NSFileManager defaultManager] removeItemAtURL:storeURL error:nil];
        [__persistentStoreCoordinator release];
        __persistentStoreCoordinator = nil;
        DLog(@"%s Unresolved error %@, %@, trying to fix by deleting persistent store", __PRETTY_FUNCTION__,error, [error userInfo]);
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
