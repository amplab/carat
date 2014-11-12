//
//  CoreDataDetail.h
//  Carat
//
//  Created by Anand Padmanabha Iyer on 1/25/12.
//  Copyright (c) 2012 UC Berkeley. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class CoreDataAppReport;

@interface CoreDataDetail : NSManagedObject

@property (nonatomic, retain) NSNumber * distance;
@property (nonatomic, retain) id distributionXWith;
@property (nonatomic, retain) id distributionXWithout;
@property (nonatomic, retain) id distributionYWith;
@property (nonatomic, retain) id distributionYWithout;
@property (nonatomic, retain) CoreDataAppReport *appReport;

@end
