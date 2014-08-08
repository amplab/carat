//
//  STClient.m
//  Loopy
//
//  Created by David Jedeikin on 4/16/14.
//  Copyright (c) 2014 ShareThis. All rights reserved.
//

#import "STClient.h"

@implementation STClient

NSString *const LANGUAGE_ID = @"objc";
NSString *const LANGUAGE_VERSION = @"1.3";

+ (STClient *)client {
    STClient *client = [[STClient alloc] init];
    
    client.lang = LANGUAGE_ID;
    client.version = LANGUAGE_VERSION;
    
    return client;
}

@end
