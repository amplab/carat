//
//  BugReportViewController.h
//  Carat
//
//  Created by Adam Oliner on 10/6/11.
//  Copyright 2011 Stanford University. All rights reserved.
//

#import <UIKit/UIKit.h>


@interface BugReportViewController : UIViewController {
    IBOutlet UITableView *bugTable;
    NSString *lastUpdatedString;
    NSMutableArray *listOfAppNames;
    NSMutableArray *listOfAppScores;
    NSDateFormatter *dateFormatter;
}

@property (retain, nonatomic) IBOutlet UITableView *bugTable;
@property (retain, nonatomic) NSString *lastUpdatedString;

@end
