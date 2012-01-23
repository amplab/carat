//
//  CurrentViewController.m
//  Carat
//
//  Created by Adam Oliner on 10/6/11.
//  Copyright 2011 UC Berkeley. All rights reserved.
//

#import "CurrentViewController.h"
#import "SHK.h"
#import "Utilities.h"
#import "DetailViewController.h"
#import "FlurryAnalytics.h"
#import "Sampler.h"
#import "CommunicationManager.h"

@implementation CurrentViewController

@synthesize jscore = _jscore;
@synthesize lastUpdated;
@synthesize sinceLastWeekString = _sinceLastWeekString;
@synthesize scoreSameOSProgBar = _scoreSameOSProgBar;
@synthesize scoreSameModelProgBar = _scoreSameModelProgBar;
@synthesize scoreSimilarAppsProgBar = _scoreSimilarAppsProgBar;
@synthesize portraitView, landscapeView;

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

#pragma mark - Data management

- (void)loadDataWithHUD
{
    HUD = [[MBProgressHUD alloc] initWithView:self.tabBarController.view];
	[self.tabBarController.view addSubview:HUD];
	
	HUD.dimBackground = YES;
	
	// Regiser for HUD callbacks so we can remove it from the window at the right time
    HUD.delegate = self;
    HUD.labelText = @"Loading";
	
    [HUD showWhileExecuting:@selector(loadData) onTarget:self withObject:nil animated:YES];
}

- (void)loadData
{
    // this shouldn't trigger; just being defensive
    if ([self isFresh]) {
        // The checkmark image is based on the work by http://www.pixelpressicons.com, http://creativecommons.org/licenses/by/2.5/ca/
        HUD.customView = [[[UIImageView alloc] initWithImage:[UIImage imageNamed:@"37x-Checkmark.png"]] autorelease];
        HUD.mode = MBProgressHUDModeCustomView;
        HUD.labelText = @"Completed";
        sleep(1);
    }
    
    // TODO UPDATE DATA

    // display result
    if ([self isFresh]) {
        // The checkmark image is based on the work by http://www.pixelpressicons.com, http://creativecommons.org/licenses/by/2.5/ca/
        HUD.customView = [[[UIImageView alloc] initWithImage:[UIImage imageNamed:@"37x-Checkmark.png"]] autorelease];
        HUD.mode = MBProgressHUDModeCustomView;
        HUD.labelText = @"Completed";
        sleep(1);
    } else {
        HUD.customView = [[[UIImageView alloc] initWithImage:[UIImage imageNamed:@"37x-X.png"]] autorelease];
        HUD.mode = MBProgressHUDModeCustomView;
        HUD.labelText = @"Failed";
        HUD.detailsLabelText = @"(showing stale data)";
        sleep(2);
    }
}

- (BOOL)isFresh
{
    return [[Sampler instance] secondsSinceLastUpdate] < 86400; // 1 day
}

#pragma mark - MBProgressHUDDelegate method

- (void)hudWasHidden:(MBProgressHUD *)hud
{
    // Remove HUD from screen when the HUD was hidded
    [HUD removeFromSuperview];
    [HUD release];
	HUD = nil;
}

#pragma mark - button actions

- (DetailViewController *)getDetailView
{
    DetailViewController *detailView = [[[DetailViewController alloc] initWithNibName:@"DetailView" bundle:nil] autorelease];
    detailView.navTitle = @"Category Detail";
    return detailView;
}

- (IBAction)getSameOSDetail:(id)sender
{
    DetailViewController *dvController = [self getDetailView];
    [self.navigationController pushViewController:dvController animated:YES];
    
    [[dvController appName] makeObjectsPerformSelector:@selector(setText:) withObject:@"Same Operating System"];
    [[dvController appIcon] makeObjectsPerformSelector:@selector(setImage:) withObject:[UIImage imageNamed:@"icon57.png"]];
    for (UIProgressView *pBar in [dvController appScore]) {
        [pBar setProgress:((UIProgressView *)[self.scoreSameOSProgBar objectAtIndex:1]).progress animated:NO];
    }
    
    [[dvController thisText] makeObjectsPerformSelector:@selector(setText:) withObject:@"Same OS"];
    [[dvController thatText] makeObjectsPerformSelector:@selector(setText:) withObject:@"Different OS"];
    
    [dvController setDetailDataThis:[[Sampler instance] getOSInfo:YES]];
    [dvController setDetailDataThat:[[Sampler instance] getOSInfo:NO]];
    
    [FlurryAnalytics logEvent:@"selectedSameOS"
               withParameters:[NSDictionary dictionaryWithObjectsAndKeys:[[UIDevice currentDevice] systemVersion], @"OS Version", nil]];
}

- (IBAction)getSameModelDetail:(id)sender
{
    DetailViewController *dvController = [self getDetailView];
    [self.navigationController pushViewController:dvController animated:YES];
    
    [[dvController appName] makeObjectsPerformSelector:@selector(setText:) withObject:@"Same Device Model"];
    [[dvController appIcon] makeObjectsPerformSelector:@selector(setImage:) withObject:[UIImage imageNamed:@"icon57.png"]];
    for (UIProgressView *pBar in [dvController appScore]) {
        [pBar setProgress:((UIProgressView *)[self.scoreSameModelProgBar objectAtIndex:1]).progress animated:NO];
    }
    
    [[dvController thisText] makeObjectsPerformSelector:@selector(setText:) withObject:@"Same Model"];
    [[dvController thatText] makeObjectsPerformSelector:@selector(setText:) withObject:@"Different Model"];
    
    [dvController setDetailDataThis:[[Sampler instance] getModelInfo:YES]];
    [dvController setDetailDataThat:[[Sampler instance] getModelInfo:NO]];
    
    [FlurryAnalytics logEvent:@"selectedSameModel"
               withParameters:[NSDictionary dictionaryWithObjectsAndKeys:[[UIDevice currentDevice] model], @"Model", nil]];
}

- (IBAction)getSimilarAppsDetail:(id)sender
{
    DetailViewController *dvController = [self getDetailView];
    [self.navigationController pushViewController:dvController animated:YES];
    
    [[dvController appName] makeObjectsPerformSelector:@selector(setText:) withObject:@"Similar Apps"];
    [[dvController appIcon] makeObjectsPerformSelector:@selector(setImage:) withObject:[UIImage imageNamed:@"icon57.png"]];
    for (UIProgressView *pBar in [dvController appScore]) {
        [pBar setProgress:((UIProgressView *)[self.scoreSimilarAppsProgBar objectAtIndex:1]).progress animated:NO];
    }
    
    [[dvController thisText] makeObjectsPerformSelector:@selector(setText:) withObject:@"Similar Apps"];
    [[dvController thatText] makeObjectsPerformSelector:@selector(setText:) withObject:@"Different Apps"];
    
    [dvController setDetailDataThis:[[Sampler instance] getSimilarAppsInfo:YES]];
    [dvController setDetailDataThat:[[Sampler instance] getSimilarAppsInfo:NO]];
    
    [FlurryAnalytics logEvent:@"selectedSimilarApps"];
}

- (IBAction)shareButtonHandlerAction
{
	// Create the item to share (in this example, a url)
	NSURL *url = [NSURL URLWithString:@"http://carat.cs.berkeley.edu"];
	SHKItem *item = [SHKItem URL:url title:@"Learn about your mobile device's battery usage. For science! (Seriously.)"];
    
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
    NSTimeInterval howLong = [[NSDate date] timeIntervalSinceDate:[[Sampler instance] getLastReportUpdateTimestamp]];
    
    for (UILabel *lastUp in self.lastUpdated) {
        lastUp.text = [Utilities formatNSTimeIntervalAsNSString:howLong];
    }
    
    [[UIDevice currentDevice] beginGeneratingDeviceOrientationNotifications];
	[[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(orientationChanged:) name:UIDeviceOrientationDidChangeNotification object:nil];
    
    if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPhone) {
        if ([[UIApplication sharedApplication] statusBarOrientation] == UIInterfaceOrientationPortrait ||
            [[UIApplication sharedApplication] statusBarOrientation] == UIInterfaceOrientationPortraitUpsideDown)
        {
            self.view = self.portraitView;
        } else {
            self.view = self.landscapeView;
        }
    }
}

- (void)viewDidUnload
{
    [scoreSameOSProgBar release];
    [self setScoreSameOSProgBar:nil];
    [scoreSameModelProgBar release];
    [self setScoreSameModelProgBar:nil];
    [scoreSimilarAppsProgBar release];
    [self setScoreSimilarAppsProgBar:nil];
    [jscore release];
    [self setJscore:nil];
    [lastUpdated release];
    [self setLastUpdated:nil];
    [sinceLastWeekString release];
    [self setSinceLastWeekString:nil];
    [portraitView release];
    [self setPortraitView:nil];
    [landscapeView release];
    [self setLandscapeView:nil];
    
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    
    if ([self isFresh]) {
        [self updateView];
    }
    
    if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPhone) {
        if ([[UIApplication sharedApplication] statusBarOrientation] == UIInterfaceOrientationPortrait ||
            [[UIApplication sharedApplication] statusBarOrientation] == UIInterfaceOrientationPortraitUpsideDown)
        {
            self.view = self.portraitView;
        } else {
            self.view = self.landscapeView;
        }
    }
    
    [self.navigationController setNavigationBarHidden:YES animated:YES];
}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    
    // loads data while showing busy indicator
    if (![self isFresh]) {
        [self loadDataWithHUD];
        
        if ([self isFresh]) {
            [self updateView];
        }

        [self.view setNeedsDisplay];
    }
}

- (void)viewWillDisappear:(BOOL)animated
{
	[super viewWillDisappear:animated];
    
    [self.navigationController setNavigationBarHidden:NO animated:YES];
}

- (void)viewDidDisappear:(BOOL)animated
{
	[super viewDidDisappear:animated];
}

- (void)updateView
{
    // J-Score
    [[self jscore] makeObjectsPerformSelector:@selector(setText:) withObject:[[NSNumber numberWithDouble:[[Sampler instance] getJScore]] stringValue]];
    
    // Last Updated
    NSTimeInterval howLong = [[NSDate date] timeIntervalSinceDate:[[Sampler instance] getLastReportUpdateTimestamp]];
    for (UILabel *lastUp in self.lastUpdated) {
        lastUp.text = [Utilities formatNSTimeIntervalAsNSString:howLong];
    }
    
    // Change since last week
    [[self sinceLastWeekString] makeObjectsPerformSelector:@selector(setText:) withObject:[[[[Sampler instance] getChangeSinceLastWeek] objectAtIndex:0] stringByAppendingString:[@" (" stringByAppendingString:[[[[Sampler instance] getChangeSinceLastWeek] objectAtIndex:1] stringByAppendingString:@"%)"]]]];
    
    // Progress Bars
    
}

- (void) orientationChanged:(id)object
{  
    if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad) {
        self.view = self.portraitView;
    } else if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPhone) {
        if ([[UIApplication sharedApplication] statusBarOrientation] == UIInterfaceOrientationPortrait ||
            [[UIApplication sharedApplication] statusBarOrientation] == UIInterfaceOrientationPortraitUpsideDown)
        {
            self.view = self.portraitView;
        } else {
            self.view = self.landscapeView;
        }
    }
}

- (BOOL) shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)orientation {
    return YES;
}

- (void)dealloc {
    [scoreSameOSProgBar release];
    [scoreSameModelProgBar release];
    [scoreSimilarAppsProgBar release];
    [jscore release];
    [lastUpdated release];
    [sinceLastWeekString release];
    [portraitView release];
    [landscapeView release];
    [super dealloc];
}

@end
