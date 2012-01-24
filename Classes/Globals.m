//
//  Globals.m
//  Carat
//
//  Singleton globals, courtesy http://www.eschatologist.net/blog/?p=178 which
//  suggests avoiding the complicated stuff in Cocoa Fundamentals Guide.
//
//  Created by Anand Padmanabha Iyer on 11/10/11.
//  Copyright (c) 2011 UC Berkeley. All rights reserved.
//

#import "Globals.h"

@implementation Globals

static id instance = nil;
static NSUserDefaults* defaults = nil;
static NSString* myUUID = nil;

+ (void) initialize {
    if (self == [Globals class]) {
        instance = [[self alloc] init];
        defaults = [NSUserDefaults standardUserDefaults];
        myUUID = [defaults objectForKey:@"CaratUUID"];
    }
}

+ (id) instance {
    return instance;
}

//
// Generate a new UUID using CFUUIDCreateString. Save the generated ID in 
// NSUserDefaults.
//
- (NSString *) generateUUID {

        CFUUIDRef uuidObject = CFUUIDCreate(kCFAllocatorDefault);
        NSString *uuidStr = [(NSString *)CFUUIDCreateString(kCFAllocatorDefault, uuidObject) autorelease];
        CFRelease(uuidObject);
        [defaults setObject:uuidStr forKey:@"CaratUUID"];
        [defaults synchronize];
        return uuidStr;
}

//
// Get the Unique ID for the device. 
// 
- (NSString *) getUUID {
    
    if (myUUID == nil) {
        myUUID = [self generateUUID];
    }
    
    return myUUID;
}

//
// Convert local datetime to UTC.
// From: http://stackoverflow.com/questions/1081647/how-to-convert-time-to-the-timezone-of-the-iphone-device
//
- (NSDate *) utcDateTime {
    NSDate* sourceDate = [NSDate date];
    NSTimeZone* sourceTimeZone = [NSTimeZone systemTimeZone];
    NSTimeZone* destinationTimeZone = [NSTimeZone timeZoneWithAbbreviation:@"UTC"];
    NSInteger sourceGMTOffset = [sourceTimeZone secondsFromGMTForDate:sourceDate];
    NSInteger destinationGMTOffset = [destinationTimeZone secondsFromGMTForDate:sourceDate];
    NSTimeInterval interval = destinationGMTOffset - sourceGMTOffset;
    NSDate* destinationDate = [[[NSDate alloc] initWithTimeInterval:interval sinceDate:sourceDate] autorelease];
    return destinationDate;
}

//
// Return seconds since epoch.
//
- (double) utcSecondsSinceEpoch {
    NSDate* dateTimeInUTC = [self utcDateTime];
    return (double) [dateTimeInUTC timeIntervalSince1970];
}


@end
