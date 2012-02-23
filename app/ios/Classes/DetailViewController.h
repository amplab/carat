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
#import "CoreDataManager.h"

@interface DetailViewController : UIViewController <CPTPlotDataSource,MBProgressHUDDelegate>
{
    NSString *navTitle;
    
    NSArray *xVals;
    NSArray *yVals;
    NSArray *xValsWithout;
    NSArray *yValsWithout;
    
    NSArray *detailGraphView;
    NSArray *appName;
    NSArray *appIcon;
    NSArray *appScore;
    NSArray *thisText;
    NSArray *thatText;
    IBOutlet UIView *portraitView;
	IBOutlet UIView *landscapeView;
}

@property (assign, nonatomic) NSString *navTitle;

@property (assign, nonatomic) NSArray *xVals;
@property (assign, nonatomic) NSArray *yVals;
@property (assign, nonatomic) NSArray *xValsWithout;
@property (assign, nonatomic) NSArray *yValsWithout;

@property (retain, nonatomic) IBOutletCollection(CPTGraphHostingView) NSArray *detailGraphView;
@property (retain, nonatomic) IBOutletCollection(UILabel) NSArray *appName;
@property (retain, nonatomic) IBOutletCollection(UIImageView) NSArray *appIcon;
@property (retain, nonatomic) IBOutletCollection(UIProgressView) NSArray *appScore;
@property (retain, nonatomic) IBOutletCollection(UILabel) NSArray *thisText;
@property (retain, nonatomic) IBOutletCollection(UILabel) NSArray *thatText;

@property (nonatomic, retain) IBOutlet UIView *portraitView;
@property (nonatomic, retain) IBOutlet UIView *landscapeView;

- (IBAction)getDetailViewInfo:(id)sender;

@end
