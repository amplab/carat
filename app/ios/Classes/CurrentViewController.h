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
    
    NSArray *memUsed;
    NSArray *memActive;
    
    MBProgressHUD *HUD;
    
    IBOutlet UIView *portraitView;
	IBOutlet UIView *landscapeView;
}

@property (retain, nonatomic) IBOutletCollection(UILabel) NSArray *jscore;
@property (retain, nonatomic) IBOutletCollection(UILabel) NSArray *expectedLife;
@property (retain, nonatomic) IBOutletCollection(UILabel) NSArray *lastUpdated;
@property (retain, nonatomic) IBOutletCollection(UILabel) NSArray *osVersion;
@property (retain, nonatomic) IBOutletCollection(UILabel) NSArray *deviceModel;
@property (retain, nonatomic) IBOutletCollection(CopyLabel) NSArray *uuid;

@property (retain, nonatomic) IBOutletCollection(UIProgressView) NSArray *memUsed;
@property (retain, nonatomic) IBOutletCollection(UIProgressView) NSArray *memActive;

@property (nonatomic, retain) IBOutlet UIView *portraitView;
@property (nonatomic, retain) IBOutlet UIView *landscapeView;

- (void)loadDataWithHUD:(id)obj;
- (void)updateView;
- (void)orientationChanged:(id)object;

- (IBAction)getSameOSDetail:(id)sender;
- (IBAction)getSameModelDetail:(id)sender;
- (IBAction)getActiveBatteryLifeInfoScreen:(id)sender;
- (IBAction)getJScoreInfoScreen:(id)sender;
- (IBAction)getProcessList:(id)sender;
- (IBAction)getMemoryInfo:(id)sender;

@end
