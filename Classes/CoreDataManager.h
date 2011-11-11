//
//  CoreDataManager.h
//  Carat
//
//  Created by Anand Padmanabha Iyer on 11/6/11.
//  Copyright (c) 2011 UC Berkeley. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>
#import "CoreDataSample.h"
#import "CaratProtocol.h"

@interface CoreDataManager : NSObject 

@property (readonly, strong, nonatomic) NSManagedObjectContext *managedObjectContext;
@property (readonly, strong, nonatomic) NSManagedObjectModel *managedObjectModel;
@property (readonly, strong, nonatomic) NSPersistentStoreCoordinator *persistentStoreCoordinator;
@property (nonatomic, retain) NSFetchedResultsController *fetchedResultsController;

- (void) saveSample : (Sample *) sample 
          sampledAt : (NSDate *) nsTimestamp;
- (NSArray *) fetchSamples;
- (NSURL *) applicationDocumentsDirectory;

@end
