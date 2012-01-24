//
//  CoreDataRegistration.h
//  Carat
//
//  Created by Anand Padmanabha Iyer on 1/23/12.
//  Copyright (c) 2012 UC Berkeley. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>


@interface CoreDataRegistration : NSManagedObject

@property (nonatomic, retain) NSNumber * timestamp;
@property (nonatomic, retain) NSString * platformId;
@property (nonatomic, retain) NSString * systemVersion;

@end
