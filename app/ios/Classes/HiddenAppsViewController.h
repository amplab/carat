//
//  HiddenAppsViewController.h
//  Carat
//
//  Created by Adam Oliner on 10/12/12.
//  Copyright (c) 2012 UC Berkeley. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "UIImageView+WebCache.h"

@interface HiddenAppsViewController : UIViewController {
    NSDate *lastUpdate;
    NSMutableArray *processList;
    UITableView *procTable;
}

@property (retain, nonatomic) NSDate *lastUpdate;
@property (retain, nonatomic) NSMutableArray *processList;
@property (retain, nonatomic) IBOutlet UITableView *procTable;

- (void) updateView;

@end
