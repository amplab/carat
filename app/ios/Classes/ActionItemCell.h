//
//  ActionItemCell.h
//  Carat
//
//  Created by Adam Oliner on 2/7/12.
//  Copyright (c) 2012 UC Berkeley. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "InstructionViewController.h"

@interface ActionItemCell : UITableViewCell
{
    IBOutlet UILabel *actionString;
    IBOutlet UILabel *actionValue;
    ActionType actionType;
}

@property (retain, nonatomic) IBOutlet UILabel *actionString;
@property (retain, nonatomic) IBOutlet UILabel *actionValue;
@property (nonatomic) ActionType actionType;

@end
