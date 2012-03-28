//
//  HogReportViewController.m
//  Carat
//
//  Created by Adam Oliner on 10/6/11.
//  Copyright 2011 UC Berkeley. All rights reserved.
//

#import "HogReportViewController.h"
#import "HogDetailViewController.h"
#import "DetailViewController.h"

@implementation HogReportViewController

// The designated initializer. Override to perform setup that is required before the view is loaded.
- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
	self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
	if (self) {
        self.title = @"Hog Report";
        self.tabBarItem.image = [UIImage imageNamed:@"hog"];
        self.detailViewName = @"HogDetailView";
        self.tableTitle = @"Energy Hogs";
        self.thisText = @"(Running)";
        self.thatText = @"(Not Running)";
    }
    return self;
}

- (DetailViewController *)getDetailView
{
    return [[[HogDetailViewController alloc] initWithNibName:@"DetailView" bundle:nil] autorelease];
}

- (void)loadDataWithHUD:(id)obj
{
    if ([[CoreDataManager instance] getReportUpdateStatus] != nil) {
        // update in progress, only update footer
        [self.dataTable reloadData];
        [self.view setNeedsDisplay];
    } else {
        // *probably* no update in progress, reload table data while locking out view
        HUD = [[MBProgressHUD alloc] initWithView:self.tabBarController.view];
        [self.tabBarController.view addSubview:HUD];
        
        HUD.dimBackground = YES;
        
        // Register for HUD callbacks so we can remove it from the window at the right time
        HUD.delegate = self;
        HUD.labelText = @"Updating Hog List";
        
        [HUD showWhileExecuting:@selector(updateView) onTarget:self withObject:nil animated:YES];
    }
}

- (void)updateView {
    [self setReport:[[CoreDataManager instance] getHogs:NO]];
    [self.dataTable reloadData];
    [self.view setNeedsDisplay];
}

#pragma mark - View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view, typically from a nib.
    [self setReport:[[CoreDataManager instance] getHogs:NO]];
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    
    [self.navigationController setNavigationBarHidden:YES animated:YES];
    
    [self setReport:[[CoreDataManager instance] getHogs:NO]];
    
    [[CoreDataManager instance] checkConnectivityAndSendStoredDataToServer];
    [self.dataTable reloadData];
}

@end
