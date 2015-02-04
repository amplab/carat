//
// CurrentViewController.h
//  Carat
//
//  Created by Adam Oliner on 10/6/11.
//  Copyright 2011 Stanford University. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MBProgressHUD.h"
#import "CopyLabel.h"

@interface CurrentViewController : UIViewController <MBProgressHUDDelegate> {
    NSTimeInterval MAX_LIFE; // max battery life in seconds
    
    NSArray *jscore;
    NSArray *expectedLife;
    NSArray *lastUpdated;
    NSArray *osVersion;
    NSArray *deviceModel;
    NSArray *uuid;
    
    MBProgressHUD *HUD;
}

@property (retain, nonatomic) IBOutletCollection(UILabel) NSArray *jscore;
@property (retain, nonatomic) IBOutletCollection(UILabel) NSArray *expectedLife;
@property (retain, nonatomic) IBOutletCollection(UILabel) NSArray *lastUpdated;
@property (retain, nonatomic) IBOutletCollection(UILabel) NSArray *osVersion;
@property (retain, nonatomic) IBOutletCollection(UILabel) NSArray *deviceModel;
@property (retain, nonatomic) IBOutletCollection(CopyLabel) NSArray *uuid;
@property (retain, nonatomic) IBOutlet UILabel *memUsed;
@property (retain, nonatomic) IBOutlet UILabel *memActive;
@property (retain, nonatomic) IBOutlet UIScrollView* scrollView;
@property(retain,nonatomic) IBOutlet UIImageView* uhAmpLogo;

- (void)loadDataWithHUD:(id)obj;
- (void)updateView;

- (IBAction)getSameOSDetail:(id)sender;
- (IBAction)getSameModelDetail:(id)sender;
- (IBAction)getActiveBatteryLifeInfoScreen:(id)sender;
- (IBAction)getJScoreInfoScreen:(id)sender;
- (IBAction)getProcessList:(id)sender;
- (IBAction)getMemoryInfo:(id)sender;

@end
