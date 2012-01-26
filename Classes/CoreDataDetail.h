//
//  CoreDataDetail.h
//  Carat
//
//  Created by Anand Padmanabha Iyer on 1/24/12.
//  Copyright (c) 2012 UC Berkeley. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>


@interface CoreDataDetail : NSManagedObject

@property (nonatomic, retain) NSNumber * distance;
@property (nonatomic, retain) id distributionWith;
@property (nonatomic, retain) id distributionWithout;
@property (nonatomic, retain) NSDate * lastUpdatedTime;
@property (nonatomic, retain) NSManagedObject *appReport;

@end
