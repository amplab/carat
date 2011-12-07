//
//  CoreDataDetail.h
//  Carat
//
//  Created by Adam Oliner on 12/7/11.
//  Copyright (c) 2011 UC Berkeley. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>


@interface CoreDataDetail : NSManagedObject

@property (nonatomic, retain) id distributionWith;
@property (nonatomic, retain) id distributionWithout;
@property (nonatomic, retain) NSNumber * distance;
@property (nonatomic, retain) NSDate * lastUpdatedTime;

@end
