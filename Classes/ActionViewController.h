//
//  ActionViewController.h
//  Carat
//
//  Created by Adam Oliner on 2/7/12.
//  Copyright (c) 2012 UC Berkeley. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MBProgressHUD.h"

@interface ActionViewController : UIViewController <MBProgressHUDDelegate> {
    
    NSMutableArray *actionStrings;
    NSMutableArray *actionValues;
    MBProgressHUD *HUD;
    
    IBOutlet UIView *dataTable;
}

@property (retain, nonatomic) NSMutableArray *actionStrings;
@property (retain, nonatomic) NSMutableArray *actionValues;

@property (retain, nonatomic) IBOutlet UIView *dataTable;

- (void)updateView;
- (void)loadDataWithHUD;
- (BOOL)isFresh;

@end
