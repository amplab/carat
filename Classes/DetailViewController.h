//
//  DetailViewController.h
//  Carat
//
//  Created by Adam Oliner on 12/14/11.
//  Copyright (c) 2011 UC Berkeley. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "CorePlot-CocoaTouch.h"
#import "MBProgressHUD.h"

@interface DetailViewController : UIViewController <CPTPlotDataSource,MBProgressHUDDelegate>
{
    BOOL firstAppearance;
    MBProgressHUD *HUD;
    
    NSString *navTitle;
}

@property (assign, nonatomic) BOOL firstAppearance;
@property (assign, nonatomic) NSString *navTitle;

- (void)loadDetailDataWithHUD;
- (BOOL)isFresh;

@end
