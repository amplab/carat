//
//  UIDevice.m
//  Carat
//
//  Created by Adam Oliner on 10/18/11.
//  Copyright (c) 2011 Stanford University. All rights reserved.
//
//  Code from http://forrst.com/posts/UIDevice_Category_For_Processes-h1H
//  and https://github.com/erica/uidevice-extension/blob/master/UIDevice-Hardware.m
// Example usage.
//NSArray * processes = [[UIDevice currentDevice] runningProcesses];
//for (NSDictionary * dict in processes){
//    NSLog(@"%@ - %@", [dict objectForKey:@"ProcessID"], [dict objectForKey:@"ProcessName"]);
//}
//

#import "UIDeviceProc.h"
#import <sys/sysctl.h>

@implementation UIDevice (ProcessesAdditions) 

- (NSUInteger) getSysInfo: (uint) typeSpecifier
{
    size_t size = sizeof(int);
    int results;
    int mib[2] = {CTL_HW, typeSpecifier};
    sysctl(mib, 2, &results, &size, NULL, 0);
    return (NSUInteger) results;
}

- (NSUInteger) pageSize
{
    return [self getSysInfo:HW_PAGESIZE];
}

- (NSUInteger) cpuFrequency
{
    return [self getSysInfo:HW_CPU_FREQ];
}

- (NSUInteger) busFrequency
{
    return [self getSysInfo:HW_BUS_FREQ];
}

- (NSUInteger) totalMemory
{
    return [self getSysInfo:HW_PHYSMEM];
}

- (NSUInteger) userMemory
{
    return [self getSysInfo:HW_USERMEM];
}

- (NSUInteger) maxSocketBufferSize
{
    return [self getSysInfo:KIPC_MAXSOCKBUF];
}

- (NSNumber *) totalDiskSpace
{
    NSDictionary *fattributes = [[NSFileManager defaultManager] attributesOfFileSystemForPath:NSHomeDirectory() error:nil];
    return [fattributes objectForKey:NSFileSystemSize];
}

- (NSNumber *) freeDiskSpace
{
    NSDictionary *fattributes = [[NSFileManager defaultManager] attributesOfFileSystemForPath:NSHomeDirectory() error:nil];
    return [fattributes objectForKey:NSFileSystemFreeSize];
}

- (NSArray *) runningProcesses {
    
    int mib[4] = {CTL_KERN, KERN_PROC, KERN_PROC_ALL, 0};
    size_t miblen = 4;
    
    size_t size;
    int st = sysctl(mib, miblen, NULL, &size, NULL, 0);
    
    struct kinfo_proc * process = NULL;
    struct kinfo_proc * newprocess = NULL;
    
    do {
        
        size += size / 10;
        newprocess = realloc(process, size);
        
        if (!newprocess){
            
            if (process){
                free(process);
            }
            
            return nil;
        }
        
        process = newprocess;
        st = sysctl(mib, miblen, process, &size, NULL, 0);
        
    } while (st == -1 && errno == ENOMEM);
    
    if (st == 0){
        
        if (size % sizeof(struct kinfo_proc) == 0){
            int nprocess = size / sizeof(struct kinfo_proc);
            
            if (nprocess){
                
                NSMutableArray * array = [[NSMutableArray alloc] init];
                
                for (int i = nprocess - 1; i >= 0; i--){
                    
                    NSString * processID = [[NSString alloc] initWithFormat:@"%d", process[i].kp_proc.p_pid];
                    NSString * processName = [[NSString alloc] initWithFormat:@"%s", process[i].kp_proc.p_comm];
                    
                    NSDictionary * dict = [[NSDictionary alloc] initWithObjects:[NSArray arrayWithObjects:processID, processName, nil] 
                                                                        forKeys:[NSArray arrayWithObjects:@"ProcessID", @"ProcessName", nil]];
                    [array addObject:dict];
                    [processID release];
                    [processName release];
                    [dict release];
                }
                
                free(process);
                return [array autorelease];
            }
        }
    }
    
    free(process);
    return nil;
}

- (NSArray *) runningProcessNames 
{    
    NSMutableArray *runningProcessNamesArray = [[[NSMutableArray alloc] init ] autorelease];
    NSArray *processes = [[UIDevice currentDevice] runningProcesses];
    for (NSDictionary *dict in processes)
    {
        [runningProcessNamesArray addObject:[dict objectForKey:@"ProcessName"]];
    }
    return runningProcessNamesArray;
}

/**
 * Returns the battery state string.
 */
- (NSString *) batteryStateString
{
    NSString * batteryStateString = @"Unknown";
    if ([UIDevice currentDevice].batteryMonitoringEnabled) 
    {
        switch ([UIDevice currentDevice].batteryState) 
        {
            case UIDeviceBatteryStateUnknown:
                batteryStateString = @"Unknown";
                break;
            case UIDeviceBatteryStateUnplugged:
                batteryStateString = @"Unplugged";
                break;
            case UIDeviceBatteryStateCharging:
                batteryStateString = @"Charging";
                break;
            case UIDeviceBatteryStateFull:
                batteryStateString = @"Full";
                break;
            default:
                break;
        }
    }
    return batteryStateString;
}

@end
