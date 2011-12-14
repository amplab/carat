//
//  HogReportViewController.h
//  Carat
//
//  Created by Adam Oliner on 10/6/11.
//  Copyright 2011 Stanford University. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface HogReportViewController : UIViewController {
    IBOutlet UITableView *dataTable;
    NSMutableArray *listOfAppNames;
    NSMutableArray *listOfAppScores;
}

@property (retain, nonatomic) IBOutlet UITableView *dataTable;

@end
