//
//  Utilities.m
//  Carat
//
//  Created by Adam Oliner on 12/8/11.
//  Copyright (c) 2011 UC Berkeley. All rights reserved.
//

#import "Utilities.h"

@implementation Utilities

+ (NSString *)formatNSTimeIntervalAsUpdatedNSString:(NSTimeInterval)timeInterval {
    // some custom strings for character
    if (timeInterval < 0) { return @"(Updated in the future. How did you do that?)"; }
    else if (timeInterval < 5) { return @"(Updated just now.)"; }
    else if (timeInterval > 31536000) { return @"(Updated never.)"; }
    else { 
        return [@"(Updated " stringByAppendingString:[[Utilities formatNSTimeIntervalAsNSString:timeInterval] stringByAppendingString:@"ago)"]];
    }
}

+ (NSString *)formatNSTimeIntervalAsNSString:(NSTimeInterval)timeInterval {
    // some custom strings for character
    if (timeInterval < 1) { return @"None"; }
    else {
        // (Updated Dd Mm Ss ago)
        int days = (int)(timeInterval / 86400);
        int hours = (int)((timeInterval - (days * 86400)) / 3600);
        int mins = (int)((timeInterval - (days * 86400) - (hours * 3600)) / 60);
        int secs = (int)((int)timeInterval % 60);
        NSString *sDays = days > 0 ? [NSString stringWithFormat:@"%dd ", days] : @"";
        NSString *sHours = hours > 0 ? [NSString stringWithFormat:@"%dh ", hours] : @"";
        NSString *sMins = mins > 0 ? [NSString stringWithFormat:@"%dm ", mins] : @"";
        NSString *sSecs = secs > 0 ? [NSString stringWithFormat:@"%ds ", secs] : @"";
        
        return [sDays stringByAppendingString:[sHours stringByAppendingString:[sMins stringByAppendingString:sSecs]]];
    }
}

@end
