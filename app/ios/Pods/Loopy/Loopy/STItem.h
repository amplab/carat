//
//  STItem.h
//  Loopy
//
//  Created by David Jedeikin on 4/17/14.
//  Copyright (c) 2014 ShareThis. All rights reserved.
//

#import "STObject.h"

@interface STItem : STObject

@property (nonatomic,strong) NSString *url;
@property (nonatomic,strong) NSString *title;
@property (nonatomic,strong) NSDictionary *meta;

@end
