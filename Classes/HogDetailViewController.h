//
//  HogDetailViewController.h
//  Carat
//
//  Created by Adam Oliner on 12/1/11.
//  Copyright (c) 2011 UC Berkeley. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "CorePlot-CocoaTouch.h"
#import "MBProgressHUD.h"

@interface HogDetailViewController : UIViewController <CPTPlotDataSource,MBProgressHUDDelegate>
{
    CPTXYGraph *graph;
    IBOutlet CPTGraphHostingView *hogDetailGraphView;
    IBOutlet UILabel *appName;
    IBOutlet UIImageView *appIcon;
    IBOutlet UIProgressView *appScore;
    IBOutlet UILabel *numSamplesWith;
    IBOutlet UILabel *numSamplesWithout;
    IBOutlet UILabel *wassersteinDistance;
    MBProgressHUD *HUD;
    BOOL firstAppearance;
}

@property (retain, nonatomic) IBOutlet CPTGraphHostingView *hogDetailGraphView;
@property (retain, nonatomic) IBOutlet UILabel *appName;
@property (retain, nonatomic) IBOutlet UIImageView *appIcon;
@property (retain, nonatomic) IBOutlet UIProgressView *appScore;
@property (retain, nonatomic) IBOutlet UILabel *numSamplesWith;
@property (retain, nonatomic) IBOutlet UILabel *numSamplesWithout;
@property (retain, nonatomic) IBOutlet UILabel *wassersteinDistance;
@property (assign, nonatomic) BOOL firstAppearance;

- (void)loadDetailDataWithHUD;
- (BOOL)isFresh;

@end
