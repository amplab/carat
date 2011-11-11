//
//  CoreDataProcessInfo.h
//  Carat
//
//  Created by Anand Padmanabha Iyer on 11/11/11.
//  Copyright (c) 2011 UC Berkeley. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class CoreDataSample;

@interface CoreDataProcessInfo : NSManagedObject

@property (nonatomic, retain) NSNumber * id;
@property (nonatomic, retain) NSString * name;
@property (nonatomic, retain) CoreDataSample *coredatasample;

@end
