//
//  STIdentifierFactory.m
//  Loopy
//
//  Created by David Jedeikin on 5/30/14.
//  Copyright (c) 2014 ShareThis. All rights reserved.
//

#import "STIdentifierFactory.h"
#import "STDefaultIdentifier.h"
#import "STHeadlessIdentifier.h"

@implementation STIdentifierFactory

+ (id)instance {
    static STIdentifierFactory *sharedFactory = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sharedFactory = [[self alloc] init];
    });
    return sharedFactory;
}

- (STIdentifier *)identifierForKey:(STIdentifierType)identifierType {
    STIdentifier *identifier;
    switch (identifierType) {
        case STIdentifierTypeStandard:
            identifier = [[STDefaultIdentifier alloc] init];
            break;
            
        case STIdentifierTypeHeadless:
            identifier = [[STHeadlessIdentifier alloc] init];
            break;
            
        default:
            break;
    }
    
    return identifier;
}


@end
