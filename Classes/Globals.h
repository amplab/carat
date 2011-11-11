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
+ (id) instance;
- (NSString*) getUUID;
@end
#endif