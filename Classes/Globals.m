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
    
    if (myUUID == Nil) {
        myUUID = [self generateUUID];
    }
    
    return myUUID;
}

@end
