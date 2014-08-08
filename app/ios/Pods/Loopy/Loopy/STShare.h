//
//  STReportShare.h
//  Loopy
//
//  Created by David Jedeikin on 4/17/14.
//  Copyright (c) 2014 ShareThis. All rights reserved.
//

#import "STObject.h"
#import "STDevice.h"
#import "STClient.h"
#import "STApp.h"

@interface STShare : STObject

@property (nonatomic,strong) NSNumber *timestamp;
@property (nonatomic,strong) NSString *stdid;
@property (nonatomic, strong) NSString *md5id;
@property (nonatomic,strong) NSString *referrer;
@property (nonatomic, strong) NSString *channel;
@property (nonatomic, strong) NSString *shortlink;
@property (nonatomic,strong) STDevice *device;
@property (nonatomic,strong) STApp *app;
@property (nonatomic,strong) STClient *client;

@end
