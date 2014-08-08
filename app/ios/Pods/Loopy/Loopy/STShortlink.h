//
//  STShortlink.h
//  Loopy
//
//  Created by David Jedeikin on 4/17/14.
//  Copyright (c) 2014 ShareThis. All rights reserved.
//

#import "STObject.h"
#import "STItem.h"

@interface STShortlink : STObject

@property (nonatomic,strong) NSNumber *timestamp;
@property (nonatomic,strong) NSString *stdid;
@property (nonatomic, strong) NSString *md5id;
@property (nonatomic, strong) STItem *item;
@property (nonatomic, strong) NSArray *tags;

@end
