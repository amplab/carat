//
//  HogDetailViewController.m
//  Carat
//
//  Created by Adam Oliner on 12/1/11.
//  Copyright (c) 2011 UC Berkeley. All rights reserved.
//

#import "HogDetailViewController.h"

@implementation HogDetailViewController

@synthesize wassersteinDistance;
@synthesize appName;
@synthesize numSamplesWith;
@synthesize numSamplesWithout;

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)didReceiveMemoryWarning
{
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc that aren't in use.
}

#pragma mark - View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
    
    self.navigationItem.title = @"Hog Detail";
}

- (void)viewDidUnload
{
    [numSamplesWithout release];
    [self setNumSamplesWithout:nil];
    [numSamplesWith release];
    [self setNumSamplesWith:nil];
    [wassersteinDistance release];
    [self setWassersteinDistance:nil];
    [appName release];
    [self setAppName:nil];
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    // Return YES for supported orientations
    return (interfaceOrientation == UIInterfaceOrientationPortrait);
}

- (void)dealloc {
    [numSamplesWith release];
    [numSamplesWithout release];
    [wassersteinDistance release];
    [appName release];
    [super dealloc];
}
@end
