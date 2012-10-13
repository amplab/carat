//
//  BugReportViewController.m
//  Carat
//
//  Created by Adam Oliner on 10/6/11.
//  Copyright 2011 UC Berkeley. All rights reserved.
//

#import "BugReportViewController.h"
#import "BugDetailViewController.h"
#import "ReportItemCell.h"
#import "CoreDataManager.h"
#import "SVPullToRefresh.h"

@implementation BugReportViewController

// The designated initializer. Override to perform setup that is required before the view is loaded.
- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
	self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
	if (self) {
        self.title = @"Bug Report";
        self.tabBarItem.image = [UIImage imageNamed:@"bug"];
        self.detailViewName = @"BugDetailView";
        self.tableTitle = @"Energy Bugs";
        self.thisText = @"(Running Here)";
        self.thatText = @"(Running Elsewhere)";
    }
    return self;
}

- (DetailViewController *)getDetailView
{
    return [[[BugDetailViewController alloc] initWithNibName:@"DetailView" bundle:nil] autorelease];
}

- (void)loadDataWithHUD:(id)obj
{
    if ([[CoreDataManager instance] getReportUpdateStatus] != nil) {
        // update in progress, only update footer
        [self.dataTable reloadData];
        [self.view setNeedsDisplay];
    } else {
        // *probably* no update in progress, reload table data while locking out view
        [self.dataTable.pullToRefreshView stopAnimating];
        HUD = [[MBProgressHUD alloc] initWithView:self.tabBarController.view];
        [self.tabBarController.view addSubview:HUD];
        
        HUD.dimBackground = YES;
        
        // Register for HUD callbacks so we can remove it from the window at the right time
        HUD.delegate = self;
        HUD.labelText = @"Updating Bug List";
        
        [HUD showWhileExecuting:@selector(updateView) onTarget:self withObject:nil animated:YES];
    }
}

- (void)updateView {
    HogBugReport * bugs = [[CoreDataManager instance] getBugs:NO withoutHidden:YES];
    if (bugs != nil) {
        [self setReport:bugs];
        [self.dataTable reloadData];
    }
    [self.view setNeedsDisplay];
}

#pragma mark - View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view, typically from a nib.
    [self setReport:[[CoreDataManager instance] getBugs:NO withoutHidden:YES]];
    
    [self.dataTable addPullToRefreshWithActionHandler:^{
        if ([[CommunicationManager instance] isInternetReachable] == YES && // online
            [[CoreDataManager instance] getReportUpdateStatus] == nil) // not already updating
        {
            [[CoreDataManager instance] updateLocalReportsFromServer];
            [self updateView];
        }
    }];
    
    [self updateView];
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    
    //[self.navigationController setNavigationBarHidden:YES animated:YES];
    
    if ([[CoreDataManager instance] getReportUpdateStatus] == nil) {
        [self.dataTable.pullToRefreshView stopAnimating];
    } else {
        [self.dataTable.pullToRefreshView startAnimating];
    }
    
    [self setReport:[[CoreDataManager instance] getBugs:NO withoutHidden:YES]];
    
    [[CoreDataManager instance] checkConnectivityAndSendStoredDataToServer];
    [self.dataTable reloadData];
}

@end
