//
//  ActionObject.m
//  Carat
//
//  Created by Adam Oliner on 2/14/12.
//  Copyright (c) 2012 UC Berkeley. All rights reserved.
//

#import "ActionObject.h"

@implementation ActionObject

@synthesize actionText, actionBenefit, actionError, actionType;

- (void) dealloc {
    [actionText release];
    [super dealloc];
}

@end
