//
//  CaratConstants.h
//  Carat
//
//  Created by Muhammad Haris on 26/12/14.
//  Copyright (c) 2014 UC Berkeley. All rights reserved.
//

#import <Foundation/Foundation.h>

extern NSString* const kSamplesSentCountUpdateNotification;
extern NSString* const kUpdateNetworkStatusNotification;
extern NSString* const kSamplesSent;
extern NSString* const kMemoryUsed;
extern NSString* const kMemoryActive;
extern NSString* const kUseWifiOnly;
extern NSString* const kIsInternetActive;
#define isUsingWifiOnly ([[[NSUserDefaults standardUserDefaults]objectForKey:kUseWifiOnly]boolValue]) ? 1 :0