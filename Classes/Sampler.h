//
//  Sampler.h
//  Carat
//
//  Created by Anand Padmanabha Iyer on 11/5/11.
//  Copyright (c) 2011 UC Berkeley. All rights reserved.
//

#ifndef Carat_Sampler_h
#define Carat_Sampler_h

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>
#import <mach/mach_host.h>
#import "Globals.h"
#import "UIDeviceProc.h"
#import "CoreDataProcessInfo.h"
#import "CoreDataSample.h"
#import "CoreDataRegistration.h"
#import "CommunicationManager.h"

@interface Sampler : NSObject 

@property (readonly, strong, nonatomic) NSManagedObjectContext *managedObjectContext;
@property (readonly, strong, nonatomic) NSManagedObjectModel *managedObjectModel;
@property (readonly, strong, nonatomic) NSPersistentStoreCoordinator *persistentStoreCoordinator;
@property (nonatomic, retain) NSFetchedResultsController *fetchedResultsController;

+ (id) instance;
- (id) initWithCommManager : cManager;
- (void) generateSaveRegistration;
- (void) sampleNow : (NSString *) triggeredBy;
- (void) fetchAndSendSamples : (NSUInteger) limitSamplesTo;
- (void) fetchAndSendRegistrations : (NSUInteger) limitMessagesTo;
- (void) sendStoredDataToServer : (NSUInteger) limitEntriesTo;
- (NSURL *) applicationDocumentsDirectory;
- (NSDate *) getLastReportUpdateTimestamp; 
- (double) secondsSinceLastUpdate;
- (HogBugReport *) getHogs;
- (HogBugReport *) getBugs;
- (double) getJScore;
- (DetailScreenReport *) getOSInfo : (BOOL) with;
- (DetailScreenReport *) getModelInfo : (BOOL) with;
- (DetailScreenReport *) getSimilarAppsInfo : (BOOL) with;
- (NSArray *) getChangeSinceLastWeek;
@end

#endif
