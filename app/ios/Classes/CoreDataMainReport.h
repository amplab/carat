//
//  CoreDataMainReport.h
//  Carat
//
//  Created by Anand Padmanabha Iyer on 1/24/12.
//  Copyright (c) 2012 UC Berkeley. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class CoreDataSubReport;

@interface CoreDataMainReport : NSManagedObject
@property (nonatomic, retain) NSNumber * samplesSent;
@property (nonatomic, retain) NSNumber * jScore;
@property (nonatomic, retain) NSDate * lastUpdated;
@property (nonatomic, retain) id changesSinceLastWeek;
@property (nonatomic, retain) NSSet *subreports;
@end

@interface CoreDataMainReport (CoreDataGeneratedAccessors)

- (void)addSubreportsObject:(CoreDataSubReport *)value;
- (void)removeSubreportsObject:(CoreDataSubReport *)value;
- (void)addSubreports:(NSSet *)values;
- (void)removeSubreports:(NSSet *)values;
@end
