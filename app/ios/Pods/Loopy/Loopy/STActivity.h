//
//  STActivity.h
//  Loopy
//
//  Created by David Jedeikin on 10/29/13.
//  Copyright (c) 2013 ShareThis. All rights reserved.
//

#import <Foundation/Foundation.h>

@protocol STActivity <NSObject>

@property (nonatomic, strong) NSArray *shareItems;

- (NSString *)activityType;

@end
