//
//  STObject.h
//  Loopy
//
//  Created by David Jedeikin on 4/16/14.
//  Copyright (c) 2014 ShareThis. All rights reserved.
//

#import <Foundation/Foundation.h>

//superclass of all data objects
@interface STObject : NSObject

- (NSArray *)allPropertyNames;
- (NSDictionary *)toDictionary;

@end
