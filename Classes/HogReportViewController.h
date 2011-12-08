//
//  HogReportViewController.h
//  Carat
//
//  Created by Adam Oliner on 10/6/11.
//  Copyright 2011 Stanford University. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MBProgressHUD.h"


@interface HogReportViewController : UIViewController {
    IBOutlet UITableView *hogTable;
    NSMutableArray *listOfAppNames;
    NSMutableArray *listOfAppScores;
    NSDateFormatter *dateFormatter;
}

@property (retain, nonatomic) IBOutlet UITableView *hogTable;

@end
