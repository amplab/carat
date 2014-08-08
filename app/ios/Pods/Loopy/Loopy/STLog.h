//
//  STLog.h
//  Loopy
//
//  Created by David Jedeikin on 4/17/14.
//  Copyright (c) 2014 ShareThis. All rights reserved.
//

#import "STObject.h"
#import "STEvent.h"
#import "STDevice.h"
#import "STApp.h"
#import "STClient.h"

@interface STLog : STObject

@property (nonatomic,strong) NSNumber *timestamp;
@property (nonatomic,strong) NSString *stdid;
@property (nonatomic,strong) NSString *md5id;
@property (nonatomic,strong) STDevice *device;
@property (nonatomic,strong) STApp *app;
@property (nonatomic,strong) STClient *client;
@property (nonatomic,strong) STEvent *event;

@end
