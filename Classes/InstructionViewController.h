//
//  InstructionViewController.h
//  Carat
//
//  Created by Adam Oliner on 2/9/12.
//  Copyright (c) 2012 UC Berkeley. All rights reserved.
//

#import <UIKit/UIKit.h>

typedef enum {
    ActionTypeKillApp,
    ActionTypeRestartApp,
    ActionTypeUpgradeOS,
    ActionTypeDimScreen,
    ActionTypeSpreadTheWord
    } ActionType;

@interface InstructionViewController : UIViewController {
    NSString *theHTML;
    ActionType actionType;
}

@property (nonatomic, copy) NSString *theHTML;
@property (nonatomic)       ActionType actionType;

@end
