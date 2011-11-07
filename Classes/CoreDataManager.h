//
//  CoreDataManager.h
//  Carat
//
//  Created by Anand Padmanabha Iyer on 11/6/11.
//  Copyright (c) 2011 UC Berkeley. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@interface CoreDataManager : NSObject

@property (readonly, strong, nonatomic) NSManagedObjectContext *managedObjectContext;
@property (readonly, strong, nonatomic) NSManagedObjectModel *managedObjectModel;
@property (readonly, strong, nonatomic) NSPersistentStoreCoordinator *persistentStoreCoordinator;
@property (nonatomic, retain) NSFetchedResultsController *fetchedResultsController;

- (void) saveSample;
- (NSArray *) fetchSamples;
- (NSURL *) applicationDocumentsDirectory;

@end
