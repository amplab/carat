//
//  Utilities.m
//  Carat
//
//  Created by Adam Oliner on 12/8/11.
//  Copyright (c) 2011 UC Berkeley. All rights reserved.
//

#import "Utilities.h"

@implementation Utilities

+ (NSString *)formatNSTimeIntervalAsNSString:(NSTimeInterval)timeInterval {
    // (Updated Dd Mm Ss ago)
    int days = (int)(timeInterval / 86400);
    int mins = (int)((timeInterval - (days * 86400)) / 3600);
    int secs = (int)((int)timeInterval % 60);
    NSString *sDays = days > 0 ? [NSString stringWithFormat:@"%dd ", days] : @"";
    NSString *sMins = mins > 0 ? [NSString stringWithFormat:@"%dm ", mins] : @"";
    NSString *sSecs = secs > 0 ? [NSString stringWithFormat:@"%ds ", secs] : @"";
    
    return [@"(Updated " stringByAppendingString:[sDays stringByAppendingString:[sMins stringByAppendingString:[sSecs stringByAppendingString:@"ago)"]]]];
}

@end
