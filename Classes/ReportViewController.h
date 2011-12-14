//
//  ReportViewController.h
//  Carat
//
//  Created by Adam Oliner on 12/14/11.
//  Copyright (c) 2011 UC Berkeley. All rights reserved.
//

#import <UIKit/UIKit.h>
@class DetailViewController;

@interface ReportViewController : UIViewController {
    NSMutableArray *listOfAppNames;
    NSMutableArray *listOfAppScores;
    
    NSString *detailViewName;
    NSString *tableTitle;
    
    IBOutlet UITableView *dataTable;
}

@property (retain, nonatomic) NSString *detailViewName;
@property (retain, nonatomic) NSString *tableTitle;

@property (retain, nonatomic) IBOutlet UITableView *dataTable;

- (DetailViewController *)getDetailView;

@end
