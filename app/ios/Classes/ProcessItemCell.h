//
//  ProcessItemCell.h
//  Carat
//
//  Created by Adam Oliner on 12/1/11.
//  Copyright (c) 2011 UC Berkeley. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface ProcessItemCell : UITableViewCell
{
    IBOutlet UILabel *appName;
    IBOutlet UIImageView *appIcon;
    IBOutlet UILabel *procID;
}

@property (retain, nonatomic) IBOutlet UILabel *appName;
@property (retain, nonatomic) IBOutlet UIImageView *appIcon;
@property (retain, nonatomic) IBOutlet UILabel *procID;

@end
