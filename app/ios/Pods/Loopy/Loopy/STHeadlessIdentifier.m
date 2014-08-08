//
//  STHeadlessIdentifier.m
//  Loopy
//
//  Created by David Jedeikin on 6/2/14.
//  Copyright (c) 2014 ShareThis. All rights reserved.
//

#import "STHeadlessIdentifier.h"

@implementation STHeadlessIdentifier {
    NSUUID *bogusIDFA;
    NSUUID *bogusIDFV;
}

- (NSUUID *)idfa {
    if(bogusIDFA == nil) {
        bogusIDFA = [NSUUID UUID];
    }
    
    return bogusIDFA;
}

- (NSUUID *)idfv {
    if(bogusIDFV == nil) {
        bogusIDFV = [NSUUID UUID];
    }
    
    return bogusIDFV;
}

@end
