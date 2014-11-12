//
//  UIDevice.h
//  Carat
//
//  Created by Adam Oliner on 10/18/11.
//  Copyright (c) 2011 Stanford University. All rights reserved.
//
//  Code from http://forrst.com/posts/UIDevice_Category_For_Processes-h1H
//

@interface UIDevice (ProcessesAdditions) 
- (NSArray *) runningProcesses;
- (NSArray *) runningProcessNames;
- (NSUInteger) cpuFrequency;
- (NSUInteger) busFrequency;
- (NSUInteger) totalMemory;
- (NSUInteger) userMemory;
- (NSUInteger) pageSize;
- (NSUInteger) maxSocketBufferSize;
- (NSNumber *) totalDiskSpace;
- (NSNumber *) freeDiskSpace;
- (NSString *) batteryStateString;
@end
