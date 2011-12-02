//
//  HogDetailViewController.h
//  Carat
//
//  Created by Adam Oliner on 12/1/11.
//  Copyright (c) 2011 UC Berkeley. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface HogDetailViewController : UIViewController
{
    
    IBOutlet UILabel *appName;
    IBOutlet UILabel *numSamplesWith;
    IBOutlet UILabel *numSamplesWithout;
    IBOutlet UILabel *wassersteinDistance;
}

@property (retain, nonatomic) IBOutlet UILabel *appName;
@property (retain, nonatomic) IBOutlet UILabel *numSamplesWith;
@property (retain, nonatomic) IBOutlet UILabel *numSamplesWithout;
@property (retain, nonatomic) IBOutlet UILabel *wassersteinDistance;

@end
