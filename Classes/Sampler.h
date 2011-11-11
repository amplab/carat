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
#import "UIDeviceProc.h"
#import "CoreDataProcessInfo.h"
#import "CoreDataSample.h"

@interface Sampler : NSObject 

@property (readonly, strong, nonatomic) NSManagedObjectContext *managedObjectContext;
@property (readonly, strong, nonatomic) NSManagedObjectModel *managedObjectModel;
@property (readonly, strong, nonatomic) NSPersistentStoreCoordinator *persistentStoreCoordinator;
@property (nonatomic, retain) NSFetchedResultsController *fetchedResultsController;

- (void) sampleNow;
- (NSURL *) applicationDocumentsDirectory;

@end

#endif
