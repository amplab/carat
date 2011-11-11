//
//  CoreDataSample.h
//  Carat
//
//  Created by Anand Padmanabha Iyer on 11/11/11.
//  Copyright (c) 2011 UC Berkeley. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class CoreDataProcessInfo;

@interface CoreDataSample : NSManagedObject

@property (nonatomic, retain) NSDate * timestamp;
@property (nonatomic, retain) NSNumber * batteryLevel;
@property (nonatomic, retain) NSNumber * batteryState;
@property (nonatomic, retain) NSSet *processInfos;
@end

@interface CoreDataSample (CoreDataGeneratedAccessors)

- (void)addProcessInfosObject:(CoreDataProcessInfo *)value;
- (void)removeProcessInfosObject:(CoreDataProcessInfo *)value;
- (void)addProcessInfos:(NSSet *)values;
- (void)removeProcessInfos:(NSSet *)values;
@end
