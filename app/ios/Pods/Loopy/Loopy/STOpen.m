//
//  STOpen.m
//  Loopy
//
//  Created by David Jedeikin on 4/16/14.
//  Copyright (c) 2014 ShareThis. All rights reserved.
//

#import "STOpen.h"

@implementation STOpen

@synthesize timestamp;
@synthesize stdid;
@synthesize referrer;
@synthesize device;
@synthesize app;
@synthesize client;
@synthesize md5id;

+ (STOpen *)openWithReferrer:(NSString *)openReferrer {
    STOpen *open = (STOpen *)[[STOpen alloc] init];
    open.referrer = openReferrer;
    
    return open;
}

@end
