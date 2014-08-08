//
//  STTwitterActivity.h
//  Loopy
//
//  Created by David Jedeikin on 10/17/13.
//  Copyright (c) 2013 ShareThis. All rights reserved.
//

#import "STActivity.h"
#import <UIKit/UIKit.h>

@interface STTwitterActivity : UIActivity <STActivity>

@property (nonatomic, strong) NSArray *shareItems;

@end
