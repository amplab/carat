//
// CurrentViewController.h
//  Carat
//
//  Created by Adam Oliner on 10/6/11.
//  Copyright 2011 Stanford University. All rights reserved.
//

#import <UIKit/UIKit.h>


@interface CurrentViewController : UIViewController {
    IBOutlet UILabel *jscore;
    IBOutlet UILabel *lastUpdated;
    IBOutlet UILabel *sinceLastWeekString;
    IBOutlet UIProgressView *scoreSameOSProgBar;
    IBOutlet UIProgressView *scoreSameModelProgBar;
    IBOutlet UIProgressView *scoreSimilarAppsProgBar;
    NSDateFormatter *dateFormatter;
}

@property (retain, nonatomic) IBOutlet UILabel *jscore;
@property (retain, nonatomic) IBOutlet UILabel *lastUpdated;
@property (retain, nonatomic) IBOutlet UILabel *sinceLastWeekString;
@property (retain, nonatomic) IBOutlet UIProgressView *scoreSameOSProgBar;
@property (retain, nonatomic) IBOutlet UIProgressView *scoreSameModelProgBar;
@property (retain, nonatomic) IBOutlet UIProgressView *scoreSimilarAppsProgBar;

- (IBAction)getSameOSDetail:(id)sender;
- (IBAction)getSameModelDetail:(id)sender;
- (IBAction)getSimilarAppsDetail:(id)sender;

@end
