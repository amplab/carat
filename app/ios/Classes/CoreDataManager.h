//
//  CoreDataManager.h
//  Carat
//
//  Created by Anand Padmanabha Iyer on 11/5/11.
//  Copyright (c) 2011 UC Berkeley. All rights reserved.
//

#ifndef Carat_CoreDataManager_h
#define Carat_CoreDataManager_h

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>
#import <mach/mach_host.h>
#import "Globals.h"
#import "UIDeviceProc.h"
#import "CoreDataProcessInfo.h"
#import "CoreDataSample.h"
#import "CoreDataAppReport.h"
#import "CoreDataDetail.h"
#import "CoreDataMainReport.h"
#import "CoreDataSubReport.h"
#import "CoreDataRegistration.h"
#import "CommunicationManager.h"

@interface CoreDataManager : NSObject 
{
    NSDate * LastUpdatedDate;
    DetailScreenReport * OSInfo;
    DetailScreenReport * OSInfoWithout;
    DetailScreenReport * ModelInfo;
    DetailScreenReport * ModelInfoWithout;
    DetailScreenReport * SimilarAppsInfo;
    DetailScreenReport * SimilarAppsInfoWithout;
    NSArray * ChangesSinceLastWeek;
    NSURLConnection *connection;
    NSMutableData *receivedData;
}

@property (readonly, strong, nonatomic) NSManagedObjectContext *managedObjectContext;
@property (readonly, strong, nonatomic) NSManagedObjectModel *managedObjectModel;
@property (readonly, strong, nonatomic) NSPersistentStoreCoordinator *persistentStoreCoordinator;
@property (nonatomic, retain) NSFetchedResultsController *fetchedResultsController;
@property (nonatomic, retain) NSDate * LastUpdatedDate;
@property (nonatomic, retain) DetailScreenReport * OSInfo;
@property (nonatomic, retain) DetailScreenReport * OSInfoWithout;
@property (nonatomic, retain) DetailScreenReport * ModelInfo;
@property (nonatomic, retain) DetailScreenReport * ModelInfoWithout;
@property (nonatomic, retain) DetailScreenReport * SimilarAppsInfo;
@property (nonatomic, retain) DetailScreenReport * SimilarAppsInfoWithout;
@property (nonatomic, retain) NSArray * ChangesSinceLastWeek;
@property (nonatomic, retain) NSURLConnection *connection;
@property (nonatomic, retain) NSMutableData *receivedData;
@property (nonatomic, retain) NSLock *lockReportSync;
@property (nonatomic, retain) NSString * daemonsFilePath;

+ (id) instance;
- (void) generateSaveRegistration;
- (void) sampleNow : (NSString *) triggeredBy;
- (void) checkConnectivityAndSendStoredDataToServer;
- (void) updateLocalReportsFromServer;
- (NSURL *) applicationDocumentsDirectory;
- (NSDate *) getLastReportUpdateTimestamp; 
- (double) secondsSinceLastUpdate;
- (HogBugReport *) getHogs : (BOOL) filterNonRunning;
- (HogBugReport *) getBugs : (BOOL) filterNonRunning;
- (double) getJScore;
- (DetailScreenReport *) getOSInfo : (BOOL) with;
- (DetailScreenReport *) getModelInfo : (BOOL) with;
- (DetailScreenReport *) getSimilarAppsInfo : (BOOL) with;
- (NSArray *) getChangeSinceLastWeek;
- (Sample *) getSample;
- (NSString *) getReportUpdateStatus;
@end

#endif
