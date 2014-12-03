//
//  DetailViewController.h
//  Carat
//
//  Created by Adam Oliner on 12/14/11.
//  Copyright (c) 2011 UC Berkeley. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MBProgressHUD.h"
#import "CoreDataManager.h"
#import "UIImageView+WebCache.h"

@interface DetailViewController : UIViewController <MBProgressHUDDelegate>
{
    NSString *navTitle;
    
    NSArray *appName;
    NSArray *appImpact;
    NSArray *appIcon;
    
    NSArray *samplesWith;
    NSArray *samplesWithout;
}

@property (assign, nonatomic) NSString *navTitle;

@property (retain, nonatomic) IBOutletCollection(UILabel) NSArray *appName;
@property (retain, nonatomic) IBOutletCollection(UILabel) NSArray *appImpact;
@property (retain, nonatomic) IBOutletCollection(UIImageView) NSArray *appIcon;

@property (retain, nonatomic) IBOutletCollection(UILabel) NSArray *samplesWith;
@property (retain, nonatomic) IBOutletCollection(UILabel) NSArray *samplesWithout;

- (IBAction)getDetailViewInfo:(id)sender;

@end
