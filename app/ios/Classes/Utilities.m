//
//  Utilities.m
//  Carat
//
//  Created by Adam Oliner on 12/8/11.
//  Copyright (c) 2011 UC Berkeley. All rights reserved.
//

#import "Utilities.h"
#import "CoreDataManager.h"
#import "CaratConstants.h"

@implementation Utilities

+ (NSString *)formatNSTimeIntervalAsUpdatedNSString:(NSTimeInterval)timeInterval {
    // some custom strings for character
	NSString* result = @"";
	;
    if (timeInterval < 0) {
		result = [NSString stringWithFormat:@"Updated in the future. How did you do that?    %i Samples sent", [[CoreDataManager instance] getSampleSent]];
		return result;
	}
    else if (timeInterval < 5) {
		result = [NSString stringWithFormat:@"Just Updated    %i Samples sent", [[CoreDataManager instance] getSampleSent]];
		return result;
	}
    else if (timeInterval > 31536000) {
		result = [NSString stringWithFormat:@"Updated never   %i Samples sent", [[CoreDataManager instance] getSampleSent]];
		return result;
	}
    else { 
		result =  [@"Updated " stringByAppendingString:[[Utilities doubleAsTimeNSString:timeInterval] stringByAppendingString:@"ago"]];
		result = [result stringByAppendingFormat:@"   %i Samples sent", [[CoreDataManager instance] getSampleSent]];
		return result;
    }
}

+ (NSString *)doubleAsTimeNSString:(double)timeInterval {
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

+ (BOOL) canUpgradeOS {
    NSString *osVersion = [UIDevice currentDevice].systemVersion;
    return ([osVersion rangeOfString:@"6."].location == NSNotFound);
}

+(CGSize) orientationIndependentScreenSize{
	CGSize screenSize = [UIScreen mainScreen].bounds.size;
	return CGSizeMake(MIN(screenSize.width, screenSize.height), MAX(screenSize.width, screenSize.height));
}

+(BOOL) isOlderHeightDevice{

	CGSize screenSize = [self orientationIndependentScreenSize];

	if (screenSize.height == 480)
		return YES;

	return NO;
}

+(NSDictionary*) getMemoryInfo{
	//  Memory info.
	mach_msg_type_number_t count = HOST_VM_INFO_COUNT;
	vm_statistics_data_t vmstat;

	NSMutableDictionary *memoryInfo = [NSMutableDictionary dictionary];
	if (host_statistics(mach_host_self(), HOST_VM_INFO, (host_info_t)&vmstat, &count) == KERN_SUCCESS)
	{
		int active = vmstat.active_count;
		int free = vmstat.free_count;
		int used = vmstat.wire_count+active+vmstat.inactive_count;
		float frac_used = ((float)(used) / (float)(used+free));
		float frac_active = ((float)(active) / (float)(used));
		memoryInfo[kMemoryUsed] = [NSNumber numberWithFloat:frac_active];
		memoryInfo[kMemoryActive] = [NSNumber numberWithFloat:frac_used];
		DLog(@"Active memory: %f, Used memory: %f", frac_active, frac_used);
		return memoryInfo;
	}

	return nil;
}

@end
