//
//  STDefaultIdentifier.m
//  Loopy
//
//  Created by David Jedeikin on 6/2/14.
//  Copyright (c) 2014 ShareThis. All rights reserved.
//

#import "STDefaultIdentifier.h"

@implementation STDefaultIdentifier

- (NSUUID *)idfa {
    NSUUID *retVal = nil;
    
    //conditional code for compliance purposes as Apple does not permit apps that don't serve ads to use IDFA
#if SHOULD_USE_IDFA
    ASIdentifierManager *idManager = [ASIdentifierManager sharedManager];
    retVal = idManager.advertisingIdentifier;
#endif
    
    return retVal;
}

- (NSUUID *)idfv {
    UIDevice *device = [UIDevice currentDevice];
    return device.identifierForVendor;
}

@end
