//
//  STClient.h
//  Loopy
//
//  Created by David Jedeikin on 4/16/14.
//  Copyright (c) 2014 ShareThis. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "STObject.h"

@interface STClient : STObject

extern NSString *const LANGUAGE_ID;
extern NSString *const LANGUAGE_VERSION;

@property (nonatomic,strong) NSString *lang;
@property (nonatomic,strong) NSString *version;

+ (STClient *)client;

@end
