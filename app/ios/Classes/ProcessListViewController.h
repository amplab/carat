//
//  ProcessListViewController.h
//  Carat
//
//  Created by Adam Oliner on 2/16/12.
//  Copyright (c) 2012 UC Berkeley. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "UIDeviceProc.h"

@interface ProcessListViewController : UIViewController {
    NSArray *processList;
    UITableView *procTable;
    NSDate *lastUpdate;
}

@property (retain, nonatomic) NSDate *lastUpdate;
@property (retain, nonatomic) NSArray *processList;
@property (retain, nonatomic) IBOutlet UITableView *procTable;

- (void) updateView;

@end
