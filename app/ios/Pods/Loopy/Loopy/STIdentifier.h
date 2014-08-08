//
//  STIdentifier.h
//  Loopy
//
//  Created by David Jedeikin on 6/2/14.
//  Copyright (c) 2014 ShareThis. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CommonCrypto/CommonDigest.h>

typedef enum STIdentifierType : NSInteger STIdentifierType;
enum STIdentifierType : NSInteger {
    STIdentifierTypeHeadless,
    STIdentifierTypeStandard
};

@interface STIdentifier : NSObject

@property (nonatomic, strong) NSUUID *idfa;
@property (nonatomic, strong) NSUUID *idfv;
@property (nonatomic, strong) NSString *md5id;

- (NSString *)md5FromString:(NSString *)input;
@end
