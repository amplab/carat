//
//  ReportItemCell.h
//  Carat
//
//  Created by Adam Oliner on 12/1/11.
//  Copyright (c) 2011 UC Berkeley. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface ReportItemCell : UITableViewCell
{
    IBOutlet UILabel *appName;
    IBOutlet UIImageView *appIcon;
    IBOutlet UIProgressView *appScore;
}

@property (retain, nonatomic) IBOutlet UILabel *appName;
@property (retain, nonatomic) IBOutlet UIImageView *appIcon;
@property (retain, nonatomic) IBOutlet UIProgressView *appScore;

@end
