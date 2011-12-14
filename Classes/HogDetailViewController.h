//
//  HogDetailViewController.h
//  Carat
//
//  Created by Adam Oliner on 12/1/11.
//  Copyright (c) 2011 UC Berkeley. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "CorePlot-CocoaTouch.h"
#import "DetailViewController.h"

@interface HogDetailViewController : DetailViewController
{
    NSArray *detailGraphView;
    NSArray *appName;
    NSArray *appIcon;
    NSArray *appScore;
    NSArray *numSamplesWith;
    NSArray *numSamplesWithout;
    NSArray *wassersteinDistance;
    IBOutlet UIView *portraitView;
	IBOutlet UIView *landscapeView;
}

@property (retain, nonatomic) IBOutletCollection(CPTGraphHostingView) NSArray *detailGraphView;
@property (retain, nonatomic) IBOutletCollection(UILabel) NSArray *appName;
@property (retain, nonatomic) IBOutletCollection(UIImageView) NSArray *appIcon;
@property (retain, nonatomic) IBOutletCollection(UIProgressView) NSArray *appScore;
@property (retain, nonatomic) IBOutletCollection(UILabel) NSArray *numSamplesWith;
@property (retain, nonatomic) IBOutletCollection(UILabel) NSArray *numSamplesWithout;
@property (retain, nonatomic) IBOutletCollection(UILabel) NSArray *wassersteinDistance;

@property (nonatomic, retain) IBOutlet UIView *portraitView;
@property (nonatomic, retain) IBOutlet UIView *landscapeView;

@end
