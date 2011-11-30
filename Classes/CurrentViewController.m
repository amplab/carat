//
//  CurrentViewController.m
//  Carat
//
//  Created by Adam Oliner on 10/6/11.
//  Copyright 2011 UC Berkeley. All rights reserved.
//

#import "CurrentViewController.h"

@implementation CurrentViewController

@synthesize jscore = _jscore;
@synthesize sinceLastWeekString = _sinceLastWeekString;
@synthesize scoreSameOSProgBar = _scoreSameOSProgBar;
@synthesize scoreSameModelProgBar = _scoreSameModelProgBar;
@synthesize scoreSimilarAppsProgBar = _scoreSimilarAppsProgBar;

// The designated initializer. Override to perform setup that is required before the view is loaded.
- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
	self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
	if (self) {
        self.title = @"Current State";
        self.tabBarItem.image = [UIImage imageNamed:@"house"];
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

#pragma mark - View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view, typically from a nib.
}

- (void)viewDidUnload
{
    [scoreSameOSProgBar release];
    scoreSameOSProgBar = nil;
    [scoreSameModelProgBar release];
    scoreSameModelProgBar = nil;
    [scoreSimilarAppsProgBar release];
    scoreSimilarAppsProgBar = nil;
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
    [sinceLastWeekString release];
    [super dealloc];
}

@end
