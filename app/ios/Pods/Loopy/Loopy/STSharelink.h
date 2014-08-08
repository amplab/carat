//
//  STSharelink.h
//  Loopy
//
//  Created by David Jedeikin on 4/17/14.
//  Copyright (c) 2014 ShareThis. All rights reserved.
//

#import "STObject.h"
#import "STItem.h"
#import "STApp.h"
#import "STClient.h"
#import "STDevice.h"

@interface STSharelink : STObject

@property (nonatomic,strong) NSNumber *timestamp;
@property (nonatomic,strong) NSString *stdid;
@property (nonatomic,strong) NSString *md5id;
@property (nonatomic,strong) STItem *item;
@property (nonatomic,strong) NSArray *tags;
@property (nonatomic,strong) NSString *channel;
@property (nonatomic,strong) STDevice *device;
@property (nonatomic,strong) STApp *app;
@property (nonatomic,strong) STClient *client;

@end
