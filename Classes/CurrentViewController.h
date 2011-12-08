//
// CurrentViewController.h
//  Carat
//
//  Created by Adam Oliner on 10/6/11.
//  Copyright 2011 Stanford University. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MBProgressHUD.h"


@interface CurrentViewController : UIViewController <MBProgressHUDDelegate> {
    IBOutlet UILabel *jscore;
    IBOutlet UILabel *lastUpdated;
    IBOutlet UILabel *sinceLastWeekString;
    IBOutlet UIProgressView *scoreSameOSProgBar;
    IBOutlet UIProgressView *scoreSameModelProgBar;
    IBOutlet UIProgressView *scoreSimilarAppsProgBar;
    MBProgressHUD *HUD;
    BOOL firstAppearance;
}

@property (retain, nonatomic) IBOutlet UILabel *jscore;
@property (retain, nonatomic) IBOutlet UILabel *lastUpdated;
@property (retain, nonatomic) IBOutlet UILabel *sinceLastWeekString;
@property (retain, nonatomic) IBOutlet UIProgressView *scoreSameOSProgBar;
@property (retain, nonatomic) IBOutlet UIProgressView *scoreSameModelProgBar;
@property (retain, nonatomic) IBOutlet UIProgressView *scoreSimilarAppsProgBar;
@property (assign, nonatomic) BOOL firstAppearance;

- (void)loadDetailDataWithHUD;
- (BOOL)isFresh;

- (IBAction)getSameOSDetail:(id)sender;
- (IBAction)getSameModelDetail:(id)sender;
- (IBAction)getSimilarAppsDetail:(id)sender;

@end
