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
    IBOutlet UILabel *sinceLastWeekString;
    IBOutlet UIProgressView *scoreSameOSProgBar;
    IBOutlet UIProgressView *scoreSameModelProgBar;
    IBOutlet UIProgressView *scoreSimilarAppsProgBar;
}

@property (weak, nonatomic) IBOutlet UILabel *jscore;
@property (weak, nonatomic) IBOutlet UILabel *sinceLastWeekString;
@property (weak, nonatomic) IBOutlet UIProgressView *scoreSameOSProgBar;
@property (weak, nonatomic) IBOutlet UIProgressView *scoreSameModelProgBar;
@property (weak, nonatomic) IBOutlet UIProgressView *scoreSimilarAppsProgBar;

- (IBAction)getSameOSDetail:(id)sender;
- (IBAction)getSameModelDetail:(id)sender;
- (IBAction)getSimilarAppsDetail:(id)sender;

@end
