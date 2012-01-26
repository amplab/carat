//
//  CoreDataAppReport.h
//  Carat
//
//  Created by Anand Padmanabha Iyer on 1/24/12.
//  Copyright (c) 2012 UC Berkeley. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class CoreDataDetail;

@interface CoreDataAppReport : NSManagedObject

@property (nonatomic, retain) NSString * appName;
@property (nonatomic, retain) NSNumber * appScore;
@property (nonatomic, retain) NSDate * lastUpdated;
@property (nonatomic, retain) NSString * reportType;
@property (nonatomic, retain) CoreDataDetail *appDetails;

@end
