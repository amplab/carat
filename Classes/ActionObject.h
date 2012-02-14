//
//  ActionObject.h
//  Carat
//
//  Created by Adam Oliner on 2/14/12.
//  Copyright (c) 2012 UC Berkeley. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "InstructionViewController.h"

@interface ActionObject : NSObject {
    NSString *actionText;
    NSInteger actionBenefit;
    ActionType actionType;
}

@property (retain, nonatomic) NSString *actionText;
@property (nonatomic)         NSInteger actionBenefit;
@property (nonatomic)         ActionType actionType;

@end
