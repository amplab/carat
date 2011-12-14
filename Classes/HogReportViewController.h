//
//  HogReportViewController.h
//  Carat
//
//  Created by Adam Oliner on 10/6/11.
//  Copyright 2011 Stanford University. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "ReportViewController.h"

@interface HogReportViewController : ReportViewController {
    IBOutlet UITableView *dataTable;
}

@property (retain, nonatomic) IBOutlet UITableView *dataTable;

@end
