//
//  DetailViewController.m
//  Carat
//
//  Created by Adam Oliner on 12/14/11.
//  Copyright (c) 2011 UC Berkeley. All rights reserved.
//

#import "DetailViewController.h"
#import "Utilities.h"
#import "InstructionViewController.h"
#import "Flurry.h"

@implementation DetailViewController

@synthesize navTitle;

@synthesize appName;
@synthesize appImpact;
@synthesize appIcon;
@synthesize samplesWith, samplesWithout;

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // custom init
    }
    
    return self;
}

- (void)didReceiveMemoryWarning
{
    DLog(@"Memory warning.");
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
}

#pragma mark - button actions

- (IBAction)getDetailViewInfo:(id)sender
{
    InstructionViewController *ivController = [[InstructionViewController alloc] initWithNibName:@"InstructionView" actionType:ActionTypeDetailInfo];
    [self.navigationController pushViewController:ivController animated:YES];
    [ivController release];
    [Flurry logEvent:@"selectedDetailScreenInfo"];
}

#pragma mark - View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
    
    self.navigationItem.title = self.navTitle;
}

- (BOOL) shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)orientation {
    return YES;
}

- (void)viewDidUnload
{
    [appName release];
    [self setAppName:nil];
    [appImpact release];
    [self setAppImpact:nil];
    [appIcon release];
    [self setAppIcon:nil];
    [samplesWith release];
    [self setSamplesWith:nil];
    [samplesWithout release];
    [self setSamplesWithout:nil];
    
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

- (void)viewWillAppear:(BOOL)animated
{
	[self.navigationController setNavigationBarHidden:NO animated:YES];
    [super viewWillAppear:animated];
    
    [[CoreDataManager instance] checkConnectivityAndSendStoredDataToServer];
}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
}

- (void)dealloc {
    [navTitle release];
    [appName release];
    [appImpact release];
    [appIcon release];
    [samplesWith release];
    [samplesWithout release];
    
    [super dealloc];
}

@end
