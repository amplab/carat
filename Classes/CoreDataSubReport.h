//
//  CoreDataSubReport.h
//  Carat
//
//  Created by Anand Padmanabha Iyer on 1/24/12.
//  Copyright (c) 2012 UC Berkeley. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class CoreDataMainReport;

@interface CoreDataSubReport : NSManagedObject

@property (nonatomic, retain) id distributionYWith;
@property (nonatomic, retain) id distributionXWithout;
@property (nonatomic, retain) NSString * name;
@property (nonatomic, retain) NSNumber * score;
@property (nonatomic, retain) id distributionXWith;
@property (nonatomic, retain) id distributionYWithout;
@property (nonatomic, retain) CoreDataMainReport *mainreport;

@end
