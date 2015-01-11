//
//  NSString+CaratAdditions.m
//  Carat
//
//  Created by Muhammad Haris on 11/01/15.
//  Copyright (c) 2015 UC Berkeley. All rights reserved.
//

#import "NSString+CaratAdditions.h"

@implementation NSString (CaratAdditions)

-(BOOL) containsSubstring:(NSString *)substring
{
	if(substring == nil)
		return NO;

	BOOL contains = [self rangeOfString:substring].location != NSNotFound;

	return contains;
}

@end
