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
    NSArray *hogDetailGraphView;
    NSArray *appName;
    NSArray *appIcon;
    NSArray *appScore;
    NSArray *numSamplesWith;
    NSArray *numSamplesWithout;
    NSArray *wassersteinDistance;
    MBProgressHUD *HUD;
    BOOL firstAppearance;
    
    IBOutlet UIView *portraitView;
	IBOutlet UIView *landscapeView;
}

@property (retain, nonatomic) IBOutletCollection(CPTGraphHostingView) NSArray *hogDetailGraphView;
@property (retain, nonatomic) IBOutletCollection(UILabel) NSArray *appName;
@property (retain, nonatomic) IBOutletCollection(UIImageView) NSArray *appIcon;
@property (retain, nonatomic) IBOutletCollection(UIProgressView) NSArray *appScore;
@property (retain, nonatomic) IBOutletCollection(UILabel) NSArray *numSamplesWith;
@property (retain, nonatomic) IBOutletCollection(UILabel) NSArray *numSamplesWithout;
@property (retain, nonatomic) IBOutletCollection(UILabel) NSArray *wassersteinDistance;
@property (assign, nonatomic) BOOL firstAppearance;

@property (nonatomic, retain) IBOutlet UIView *portraitView;
@property (nonatomic, retain) IBOutlet UIView *landscapeView;

- (void)loadDetailDataWithHUD;
- (BOOL)isFresh;

@end
