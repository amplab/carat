//
//  STJSONUtils.h
//  Loopy
//
//  Created by David Jedeikin on 9/13/13.
//  Copyright (c) 2013 ShareThis. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "STObject.h"

@interface STJSONUtils : NSObject

+ (NSData *)toJSONData:(NSDictionary *)jsonDict;
+ (NSString *)toJSONString:(NSData *)jsonData;
+ (NSDictionary *)toJSONDictionary:(NSData *)jsonData;
+ (NSData *)toJSONDataFromObject:(STObject *)obj;

@end
