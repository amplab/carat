//
//  ActionViewController.h
//  Carat
//
//  Created by Adam Oliner on 2/7/12.
//  Copyright (c) 2012 UC Berkeley. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface ActionViewController : UIViewController {
    
    NSMutableArray *actionStrings;
    NSMutableArray *actionValues;
    
    IBOutlet UIView *dataTable;
}

@property (retain, nonatomic) NSMutableArray *actionStrings;
@property (retain, nonatomic) NSMutableArray *actionValues;

@property (retain, nonatomic) IBOutlet UIView *dataTable;

@end
