//
//  Utilities.h
//  Carat
//
//  Created by Adam Oliner on 12/8/11.
//  Copyright (c) 2011 UC Berkeley. All rights reserved.
//

#ifdef DEBUG_MODE
#define DLog( s, ... ) NSLog( @"<%p %@:(%d)> %@", self, [[NSString stringWithUTF8String:__FILE__] lastPathComponent], __LINE__, [NSString stringWithFormat:(s), ##__VA_ARGS__] )
#else
#define DLog( s, ... ) 
#endif

#import <Foundation/Foundation.h>

@interface Utilities : NSObject

+ (NSString *)formatNSTimeIntervalAsUpdatedNSString:(NSTimeInterval)timeInterval;
+ (NSString *)doubleAsTimeNSString:(NSTimeInterval)timeInterval;
+ (BOOL) canUpgradeOS;
+ (CGSize) orientationIndependentScreenSize;
+(BOOL) isOlderHeightDevice;
+(NSDictionary*) getMemoryInfo;

@end
