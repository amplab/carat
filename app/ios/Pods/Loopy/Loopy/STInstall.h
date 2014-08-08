//
//  STInstall.h
//  Loopy
//
//  Created by David Jedeikin on 4/16/14.
//  Copyright (c) 2014 ShareThis. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "STObject.h"
#import "STDevice.h"
#import "STClient.h"
#import "STApp.h"

@interface STInstall : STObject

@property (nonatomic,strong) NSNumber *timestamp;
@property (nonatomic,strong) NSString *stdid;
@property (nonatomic,strong) NSString *referrer;
@property (nonatomic,strong) STDevice *device;
@property (nonatomic,strong) STApp *app;
@property (nonatomic,strong) STClient *client;
@property (nonatomic,strong) NSString *md5id;

+ (STInstall *)installWithReferrer:(NSString *)installReferrer;

@end
