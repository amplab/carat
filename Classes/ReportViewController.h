//
//  ReportViewController.h
//  Carat
//
//  Created by Adam Oliner on 12/14/11.
//  Copyright (c) 2011 UC Berkeley. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "Sampler.h"
@class DetailViewController;

@interface ReportViewController : UIViewController {
    NSString *detailViewName;
    NSString *tableTitle;
    NSString *thisText;
    NSString *thatText;
    
    HogBugReport *report;
    
    IBOutlet UITableView *dataTable;
}

@property (retain, nonatomic) NSString *detailViewName;
@property (retain, nonatomic) NSString *tableTitle;
@property (retain, nonatomic) NSString *thisText;
@property (retain, nonatomic) NSString *thatText;

@property (retain, nonatomic) IBOutlet UITableView *dataTable;

- (DetailViewController *)getDetailView;

@end
