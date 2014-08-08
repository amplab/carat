//
//  STDevice.h
//  Loopy
//
//  Created by David Jedeikin on 4/16/14.
//  Copyright (c) 2014 ShareThis. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "STGeo.h"
#import "STObject.h"

@interface STDevice : STObject

@property (nonatomic,strong) NSString *id;
@property (nonatomic,strong) NSString *idv;
@property (nonatomic,strong) NSString *carrier;
@property (nonatomic,strong) NSString *model;
@property (nonatomic,strong) NSString *os;
@property (nonatomic,strong) NSString *osv;
@property (nonatomic,strong) NSString *wifi;
@property (nonatomic,strong) STGeo *geo;

@end
