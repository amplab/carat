//
//  CoreDataReport.h
//  Carat
//
//  Created by Adam Oliner on 12/7/11.
//  Copyright (c) 2011 UC Berkeley. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class CoreDataDetail;

@interface CoreDataReport : NSManagedObject

@property (nonatomic, retain) NSString * appName;
@property (nonatomic, retain) NSNumber * appScore;
@property (nonatomic, retain) NSDate * lastUpdated;
@property (nonatomic, retain) CoreDataDetail *appDetails;

@end
