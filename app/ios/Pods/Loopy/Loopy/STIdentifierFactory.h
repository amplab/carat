//
//  STIdentifierFactory.h
//  Loopy
//
//  Created by David Jedeikin on 5/30/14.
//  Copyright (c) 2014 ShareThis. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "STIdentifier.h"

@interface STIdentifierFactory : NSObject

+(id)instance;
- (STIdentifier *)identifierForKey:(STIdentifierType)identifierType;

@end
