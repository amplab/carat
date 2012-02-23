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
    ActionTypeSpreadTheWord,
    ActionTypeJScoreInfo,
    ActionTypeMemoryInfo
    } ActionType;

@interface InstructionViewController : UIViewController <UIWebViewDelegate> {
    ActionType actionType;
    UIWebView *webView;
}

@property (nonatomic)         ActionType actionType;
@property (retain, nonatomic) IBOutlet UIWebView *webView;

- (id)initWithNibName:(NSString *)nibNameOrNil actionType:(ActionType)action;

@end
