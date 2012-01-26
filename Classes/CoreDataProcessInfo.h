//
//  CoreDataProcessInfo.h
//  Carat
//
//  Created by Anand Padmanabha Iyer on 1/24/12.
//  Copyright (c) 2012 UC Berkeley. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class CoreDataSample;

@interface CoreDataProcessInfo : NSManagedObject

@property (nonatomic, retain) NSNumber * id;
@property (nonatomic, retain) NSString * name;
@property (nonatomic, retain) CoreDataSample *coredatasample;

@end
