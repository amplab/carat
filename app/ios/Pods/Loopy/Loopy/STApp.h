//
//  STApp.h
//  Loopy
//
//  Created by David Jedeikin on 4/16/14.
//  Copyright (c) 2014 ShareThis. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "STObject.h"

@interface STApp : STObject

@property (nonatomic,strong) NSString *id;
@property (nonatomic,strong) NSString *name;
@property (nonatomic,strong) NSString *version;

@end
