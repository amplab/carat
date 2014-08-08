//
//  STIdentifier.m
//  Loopy
//
//  Created by David Jedeikin on 6/2/14.
//  Copyright (c) 2014 ShareThis. All rights reserved.
//

#import "STIdentifier.h"

@implementation STIdentifier {
    NSString *currentMD5;
}

//convenience method to return MD5 String
//per http://www.makebetterthings.com/iphone/how-to-get-md5-and-sha1-in-objective-c-ios-sdk/
- (NSString *)md5FromString:(NSString *)input {
    const char *cStr = [input UTF8String];
    unsigned char digest[16];
    CC_MD5( cStr, strlen(cStr), digest ); // This is the md5 call
    
    NSMutableString *output = [NSMutableString stringWithCapacity:CC_MD5_DIGEST_LENGTH * 2];
    
    for(int i = 0; i < CC_MD5_DIGEST_LENGTH; i++) {
        [output appendFormat:@"%02x", digest[i]];
    }
    
    return output;
}


- (NSString *)md5id {
    if(currentMD5 == nil) {
        if(self.idfa) {
            currentMD5 = [self md5FromString:[self.idfa UUIDString]];
        }
        //for other circumstances prohibiting idfa
        else if(self.idfv) {
            currentMD5 = [self md5FromString:[self.idfv UUIDString]];
        }
    }
    
    return currentMD5;
}

@end
