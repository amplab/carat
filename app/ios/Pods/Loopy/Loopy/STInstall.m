//
//  STInstall.m
//  Loopy
//
//  Created by David Jedeikin on 4/16/14.
//  Copyright (c) 2014 ShareThis. All rights reserved.
//

#import "STInstall.h"

@implementation STInstall

@synthesize timestamp;
@synthesize stdid;
@synthesize referrer;
@synthesize device;
@synthesize app;
@synthesize client;
@synthesize md5id;

+ (STInstall *)installWithReferrer:(NSString *)installReferrer {
    STInstall *install = (STInstall *)[[STInstall alloc] init];
    install.referrer = installReferrer;
    
    return install;
}

@end
