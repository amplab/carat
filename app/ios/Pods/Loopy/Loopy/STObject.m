//
//  STObject.m
//  Loopy
//
//  Created by David Jedeikin on 4/16/14.
//  Copyright (c) 2014 ShareThis. All rights reserved.
//

#import "STObject.h"
#import <objc/runtime.h>

@implementation STObject

//per http://stackoverflow.com/questions/11774162/list-of-class-properties-in-objective-c
- (NSArray *)allPropertyNames {
    unsigned count;
    objc_property_t *properties = class_copyPropertyList([self class], &count);
    
    NSMutableArray *rv = [NSMutableArray array];
    
    unsigned i;
    for (i = 0; i < count; i++) {
        objc_property_t property = properties[i];
        NSString *name = [NSString stringWithUTF8String:property_getName(property)];
        [rv addObject:name];
    }
    
    free(properties);
    
    return rv;
}

//recursive dictionary representation of object as key-value pairs
- (NSDictionary *)toDictionary {
    NSMutableDictionary *dict = [NSMutableDictionary dictionary];
    NSArray *properties = [self allPropertyNames];
    
    for(NSString *property in properties) {
        id value = [self valueForKey:property];
        if([value isKindOfClass:[NSString class]] || [value isKindOfClass:[NSNumber class]]) {
            [dict setObject:value forKey:property];
        }
        //recurse
        else if([value isKindOfClass:[STObject class]]) {
            STObject *childObj = (STObject *)value;
            [dict setObject:[childObj toDictionary] forKey:property];
        }
    }
    
    return dict;
}

@end
