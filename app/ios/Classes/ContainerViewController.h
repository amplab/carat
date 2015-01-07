//
//  ContainerViewController.h
//  Carat
//
//  Created by Muhammad Haris on 25/12/14.
//  Copyright (c) 2014 UC Berkeley. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface ContainerViewController : UIViewController

@property (nonatomic, retain) IBOutlet UIView* topBar;
@property (nonatomic, retain) IBOutlet UILabel* samplesSentLabel;
@property (nonatomic, retain) IBOutlet UILabel* pageTitle;
@property (nonatomic, retain) IBOutlet UILabel* updatedXAgo;
@end
