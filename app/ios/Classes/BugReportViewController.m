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
    HUD = [[MBProgressHUD alloc] initWithView:self.tabBarController.view];
	[self.tabBarController.view addSubview:HUD];
	
	HUD.dimBackground = YES;
	
	// Register for HUD callbacks so we can remove it from the window at the right time
    HUD.delegate = self;
    HUD.labelText = @"Updating Bug List";
	
    [HUD showWhileExecuting:@selector(updateView) onTarget:self withObject:nil animated:YES];
}

- (void)updateView {
    [self setReport:[[CoreDataManager instance] getBugs:NO]];
    [self.dataTable reloadData];
    [self.view setNeedsDisplay];
}

#pragma mark - View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view, typically from a nib.

    [self setReport:[[CoreDataManager instance] getBugs:NO]];
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    
    [self.navigationController setNavigationBarHidden:YES animated:YES];
    
    [self setReport:[[CoreDataManager instance] getBugs:NO]];
    
    [[CoreDataManager instance] checkConnectivityAndSendStoredDataToServer];
    [self.dataTable reloadData];
}

@end
