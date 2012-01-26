//
// CurrentViewController.h
//  Carat
//
//  Created by Adam Oliner on 10/6/11.
//  Copyright 2011 Stanford University. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MBProgressHUD.h"
//#import "Reachability.h"

//@class Reachability;

@interface CurrentViewController : UIViewController <MBProgressHUDDelegate> {
    NSArray *jscore;
    NSArray *lastUpdated;
    NSArray *sinceLastWeekString;
    NSArray *scoreSameOSProgBar;
    NSArray *scoreSameModelProgBar;
    NSArray *scoreSimilarAppsProgBar;
    MBProgressHUD *HUD;
    //Reachability* internetReachable;
    //BOOL isInternetActive;
    
    IBOutlet UIView *portraitView;
	IBOutlet UIView *landscapeView;
}

@property (retain, nonatomic) IBOutletCollection(UILabel) NSArray *jscore;
@property (retain, nonatomic) IBOutletCollection(UILabel) NSArray *lastUpdated;
@property (retain, nonatomic) IBOutletCollection(UILabel) NSArray *sinceLastWeekString;
@property (retain, nonatomic) IBOutletCollection(UIProgressView) NSArray *scoreSameOSProgBar;
@property (retain, nonatomic) IBOutletCollection(UIProgressView) NSArray *scoreSameModelProgBar;
@property (retain, nonatomic) IBOutletCollection(UIProgressView) NSArray *scoreSimilarAppsProgBar;
//@property (nonatomic) BOOL isInternetActive;

@property (nonatomic, retain) IBOutlet UIView *portraitView;
@property (nonatomic, retain) IBOutlet UIView *landscapeView;

- (void)loadDataWithHUD;
- (BOOL)isFresh;
- (void)updateView;
//- (void) checkNetworkStatus:(NSNotification *)notice;

- (IBAction)getSameOSDetail:(id)sender;
- (IBAction)getSameModelDetail:(id)sender;
- (IBAction)getSimilarAppsDetail:(id)sender;

@end
