//
//  STEvent.h
//  Loopy
//
//  Created by David Jedeikin on 4/17/14.
//  Copyright (c) 2014 ShareThis. All rights reserved.
//

#import "STObject.h"

@interface STEvent : STObject

@property (nonatomic,strong) NSString *type;
@property (nonatomic,strong) NSDictionary *meta;

@end
