//
//  CurrentViewController.m
//  Carat
//
//  Created by Adam Oliner on 10/6/11.
//  Copyright 2011 UC Berkeley. All rights reserved.
//

#import "CurrentViewController.h"
#import "SHK.h"

@implementation CurrentViewController

@synthesize jscore = _jscore;
@synthesize lastUpdated = _lastUpdated;
@synthesize sinceLastWeekString = _sinceLastWeekString;
@synthesize scoreSameOSProgBar = _scoreSameOSProgBar;
@synthesize scoreSameModelProgBar = _scoreSameModelProgBar;
@synthesize scoreSimilarAppsProgBar = _scoreSimilarAppsProgBar;

// The designated initializer. Override to perform setup that is required before the view is loaded.
- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
	self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
	if (self) {
        self.title = @"Current State";
        self.tabBarItem.image = [UIImage imageNamed:@"53-house"];
    }
    return self;
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Release any cached data, images, etc that aren't in use.
}

#pragma mark - button actions

- (IBAction)getSameOSDetail:(id)sender
{
    NSLog(@"same OS detail");
}

- (IBAction)getSameModelDetail:(id)sender
{
    NSLog(@"same Model detail");
}

- (IBAction)getSimilarAppsDetail:(id)sender
{
    NSLog(@"similar Apps detail");
}

- (IBAction)shareButtonHandlerAction
{
	// Create the item to share (in this example, a url)
	NSURL *url = [NSURL URLWithString:@"http://carat.cs.berkeley.edu"];
	SHKItem *item = [SHKItem URL:url title:@"Learn about your phone's battery usage. For science! (Seriously.)"];
    
	// Get the ShareKit action sheet
	SHKActionSheet *actionSheet = [SHKActionSheet actionSheetForItem:item];
    
	// Display the action sheet
	[actionSheet showFromTabBar:self.tabBarController.tabBar];
}

#pragma mark - View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view, typically from a nib.
    dateFormatter = [[NSDateFormatter alloc] init];
    [dateFormatter setDateFormat:@"'( Updated:' yyyy-MM-dd, hh:mm:ss ')'"];
    self.lastUpdated.text = [dateFormatter stringFromDate:[NSDate date]];
}

- (void)viewDidUnload
{
    [scoreSameOSProgBar release];
    scoreSameOSProgBar = nil;
    [scoreSameModelProgBar release];
    scoreSameModelProgBar = nil;
    [scoreSimilarAppsProgBar release];
    scoreSimilarAppsProgBar = nil;
    [jscore release];
    jscore = nil;
    [lastUpdated release];
    [self setLastUpdated:nil];
    [sinceLastWeekString release];
    sinceLastWeekString = nil;
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
}

- (void)viewWillDisappear:(BOOL)animated
{
	[super viewWillDisappear:animated];
}

- (void)viewDidDisappear:(BOOL)animated
{
	[super viewDidDisappear:animated];
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    // Return YES for supported orientations
    if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPhone) {
        return (interfaceOrientation != UIInterfaceOrientationPortraitUpsideDown);
    } else {
        return YES;
    }
}


- (void)dealloc {
    [scoreSameOSProgBar release];
    [scoreSameModelProgBar release];
    [scoreSimilarAppsProgBar release];
    [jscore release];
    [lastUpdated release];
    [sinceLastWeekString release];
    [super dealloc];
}

@end
