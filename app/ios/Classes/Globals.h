//
//  Globals.m
//  Carat
//
//  Created by Anand Padmanabha Iyer on 11/10/11.
//  Copyright (c) 2011 UC Berkeley. All rights reserved.
//

#ifndef Globals_h
#define Globals_h
@interface Globals : NSObject
{
    NSString * myUUID;
    NSUserDefaults * defaults; 
}

@property (nonatomic, retain) NSString * myUUID;
@property (nonatomic, retain) NSUserDefaults * defaults;

+ (id) instance;
- (void) getUUIDFromNSUserDefaults;
- (NSString*) getUUID;
- (NSDate *) utcDateTime;
- (double) utcSecondsSinceEpoch;
- (void) userHasConsented;
- (BOOL) hasUserConsented;
- (void) setDistanceTraveled : (double) distance;
- (double) getDistanceTraveled;

@end
#endif