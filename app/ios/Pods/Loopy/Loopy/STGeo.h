//
//  STGeo.h
//  Loopy
//
//  Created by David Jedeikin on 4/16/14.
//  Copyright (c) 2014 ShareThis. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "STObject.h"

@interface STGeo : STObject

@property (nonatomic,strong) NSNumber *lat;
@property (nonatomic,strong) NSNumber *lon;

@end
