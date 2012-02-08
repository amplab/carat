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
#import "UIDeviceHardware.h"
#import "Globals.h"
#import "UIImageDoNotCache.h"

@implementation CurrentViewController

@synthesize jscore = _jscore;
@synthesize lastUpdated = _lastUpdated;
@synthesize sinceLastWeekString = _sinceLastWeekString;
@synthesize scoreSameOSProgBar = _scoreSameOSProgBar;
@synthesize scoreSameModelProgBar = _scoreSameModelProgBar;
@synthesize scoreSimilarAppsProgBar = _scoreSimilarAppsProgBar;
@synthesize portraitView, landscapeView;

// The designated initializer. Override to perform setup that is required before the view is loaded.
- (id) initWithNibName: (NSString *) nibNameOrNil 
                bundle: (NSBundle *)nibBundleOrNil 
{
	self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
	if (self) {
        self.title = @"My Device";
        self.tabBarItem.image = [UIImage imageNamed:@"32-iphone"];
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
	
	// Register for HUD callbacks so we can remove it from the window at the right time
    HUD.delegate = self;
    HUD.labelText = @"Loading";
	
    [HUD showWhileExecuting:@selector(loadData) onTarget:self withObject:nil animated:YES];
}

- (void)loadData
{    
    // this shouldn't trigger; just being defensive
    if ([self isFresh]) {
        // The checkmark image is based on the work by http://www.pixelpressicons.com, http://creativecommons.org/licenses/by/2.5/ca/
        UIImage *icon = [UIImage newImageNotCached:@"37x-Checkmark.png"];
        UIImageView *imgView = [[UIImageView alloc] initWithImage:icon];
        HUD.customView = imgView;
        [HUD setMode:MBProgressHUDModeCustomView];
        HUD.labelText = @"Completed";
        [icon release];
        [imgView release];
        sleep(1);
    }

    // UPDATE REPORT DATA
    if ([[CommunicationManager instance] isInternetReachable] == YES)
    {
        [[Sampler instance] updateLocalReportsFromServer];
    }
    
    [self updateView];
    
    // display result
    if ([self isFresh]) {
        // The checkmark image is based on the work by http://www.pixelpressicons.com, http://creativecommons.org/licenses/by/2.5/ca/
        UIImage *icon = [UIImage newImageNotCached:@"37x-Checkmark.png"];
        UIImageView *imgView = [[UIImageView alloc] initWithImage:icon];
        HUD.customView = imgView;
        [HUD setMode:MBProgressHUDModeCustomView];
        HUD.labelText = @"Completed";
        [icon release];
        [imgView release];
        sleep(1);
    } else {
        UIImage *icon = [UIImage newImageNotCached:@"37x-X.png"];
        UIImageView *imgView = [[UIImageView alloc] initWithImage:icon];
        HUD.customView = imgView;
        [HUD setMode:MBProgressHUDModeCustomView];
        HUD.labelText = @"Update Failed";
        HUD.detailsLabelText = @"(showing stale data)";
        [icon release];
        [imgView release];
        sleep(2);
    }
}

- (BOOL) isFresh
{
    return [[Sampler instance] secondsSinceLastUpdate] < 600; // 10 minutes
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
    DetailScreenReport *dsr = [[Sampler instance] getOSInfo:YES];
    if ([dsr xVals] == nil || [[dsr xVals] count] == 0) {
        UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Nothing to Report!" 
                                                        message:@"Please check back later; we should have results for your device soon." 
                                                       delegate:nil 
                                              cancelButtonTitle:@"OK"
                                              otherButtonTitles:nil];
        [alert show];
        [alert release];
    } else {
        DetailViewController *dvController = [self getDetailView];

        [dvController setXVals:[dsr xVals]];
        [dvController setYVals:[dsr yVals]];
        
        dsr = [[Sampler instance] getOSInfo:NO];
        [dvController setXValsWithout:[dsr xVals]];
        [dvController setYValsWithout:[dsr yVals]];
        
        [self.navigationController pushViewController:dvController animated:YES];
        
        [[dvController appName] makeObjectsPerformSelector:@selector(setText:) withObject:@"Same Operating System"];
        UIImage *img = [UIImage newImageNotCached:@"icon57.png"];
        [[dvController appIcon] makeObjectsPerformSelector:@selector(setImage:) withObject:img];
        [img release];
        for (UIProgressView *pBar in [dvController appScore]) {
            [pBar setProgress:((UIProgressView *)[self.scoreSameOSProgBar objectAtIndex:1]).progress animated:NO];
        }
        
        [[dvController thisText] makeObjectsPerformSelector:@selector(setText:) withObject:@"Same OS"];
        [[dvController thatText] makeObjectsPerformSelector:@selector(setText:) withObject:@"Different OS"];
        
        [FlurryAnalytics logEvent:@"selectedSameOS"
                   withParameters:[NSDictionary dictionaryWithObjectsAndKeys:[[UIDevice currentDevice] systemVersion], @"OS Version", nil]];
    }
}

- (IBAction)getSameModelDetail:(id)sender
{
    DetailScreenReport *dsr = [[Sampler instance] getModelInfo:YES];
    if ([dsr xVals] == nil || [[dsr xVals] count] == 0) {
        UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Nothing to Report!" 
                                                        message:@"Please check back later; we should have results for your device soon." 
                                                       delegate:nil 
                                              cancelButtonTitle:@"OK"
                                              otherButtonTitles:nil];
        [alert show];
        [alert release];
    } else {
        DetailViewController *dvController = [self getDetailView];

        [dvController setXVals:[dsr xVals]];
        [dvController setYVals:[dsr yVals]];
        dsr = [[Sampler instance] getModelInfo:NO];
        [dvController setXValsWithout:[dsr xVals]];
        [dvController setYValsWithout:[dsr yVals]];
        
        [self.navigationController pushViewController:dvController animated:YES];
        
        [[dvController appName] makeObjectsPerformSelector:@selector(setText:) withObject:@"Same Device Model"];
        UIImage *img = [UIImage newImageNotCached:@"icon57.png"];
        [[dvController appIcon] makeObjectsPerformSelector:@selector(setImage:) withObject:img];
        [img release];
        for (UIProgressView *pBar in [dvController appScore]) {
            [pBar setProgress:((UIProgressView *)[self.scoreSameModelProgBar objectAtIndex:1]).progress animated:NO];
        }
        
        [[dvController thisText] makeObjectsPerformSelector:@selector(setText:) withObject:@"Same Model"];
        [[dvController thatText] makeObjectsPerformSelector:@selector(setText:) withObject:@"Different Model"];
        
        UIDeviceHardware *h =[[UIDeviceHardware alloc] init];
        [FlurryAnalytics logEvent:@"selectedSameModel"
                   withParameters:[NSDictionary dictionaryWithObjectsAndKeys:[h platformString], @"Model", nil]];
        [h release];
    }
}

- (IBAction)getSimilarAppsDetail:(id)sender
{
    DetailScreenReport *dsr = [[Sampler instance] getSimilarAppsInfo:YES];
    if ([dsr xVals] == nil || [[dsr xVals] count] == 0) {
        UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Nothing to Report!" 
                                                        message:@"Please check back later; we should have results for your device soon." 
                                                       delegate:nil 
                                              cancelButtonTitle:@"OK"
                                              otherButtonTitles:nil];
        [alert show];
        [alert release];
    } else {
        DetailViewController *dvController = [self getDetailView];
        
        [dvController setXVals:[dsr xVals]];
        [dvController setYVals:[dsr yVals]];
        dsr = [[Sampler instance] getSimilarAppsInfo:NO];
        [dvController setXValsWithout:[dsr xVals]];
        [dvController setYValsWithout:[dsr yVals]];
        
        [self.navigationController pushViewController:dvController animated:YES];
        
        [[dvController appName] makeObjectsPerformSelector:@selector(setText:) withObject:@"Similar Apps"];
        UIImage *img = [UIImage newImageNotCached:@"icon57.png"];
        [[dvController appIcon] makeObjectsPerformSelector:@selector(setImage:) withObject:img];
        [img release];
        for (UIProgressView *pBar in [dvController appScore]) {
            [pBar setProgress:((UIProgressView *)[self.scoreSimilarAppsProgBar objectAtIndex:1]).progress animated:NO];
        }
        
        [[dvController thisText] makeObjectsPerformSelector:@selector(setText:) withObject:@"Similar Apps"];
        [[dvController thatText] makeObjectsPerformSelector:@selector(setText:) withObject:@"Different Apps"];
        
        [FlurryAnalytics logEvent:@"selectedSimilarApps"];
    }
}

#pragma mark - View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view, typically from a nib.
    
    DLog(@"My UUID: %@", [[Globals instance] getUUID]);
    [[UIDevice currentDevice] beginGeneratingDeviceOrientationNotifications];
	[[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(orientationChanged:) name:UIDeviceOrientationDidChangeNotification object:nil];
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

    [self updateView];
    [self orientationChanged:nil];
    
    [self.navigationController setNavigationBarHidden:YES animated:YES];
}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    
    // loads data while showing busy indicator
    if (![self isFresh]) {
        [self loadDataWithHUD];
    } else {
        // For this screen, let's put sending samples/registrations here so that we don't conflict
        // with the report syncing (need to limit memory/CPU/thread usage so that we don't get killed).
        [[Sampler instance] checkConnectivityAndSendStoredDataToServer];
    }
    
    [self updateView];
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
    [[self jscore] makeObjectsPerformSelector:@selector(setText:) withObject:[[NSNumber numberWithInt:(int)(MIN( MAX([[Sampler instance] getJScore], -1.0), 1.0)*100)] stringValue]];
    
    // Last Updated
    NSTimeInterval howLong = [[NSDate date] timeIntervalSinceDate:[[Sampler instance] getLastReportUpdateTimestamp]];
    for (UILabel *lastUp in self.lastUpdated) {
        lastUp.text = [Utilities formatNSTimeIntervalAsUpdatedNSString:howLong];
    }
    
    // Change since last week
    [[self sinceLastWeekString] makeObjectsPerformSelector:@selector(setText:) withObject:[[[[Sampler instance] getChangeSinceLastWeek] objectAtIndex:0] stringByAppendingString:[@" (" stringByAppendingString:[[[[Sampler instance] getChangeSinceLastWeek] objectAtIndex:1] stringByAppendingString:@"%)"]]]];
    
    // Progress Bars
    for (UIProgressView *scoreBar in self.scoreSameOSProgBar) {
        [scoreBar setProgress:MIN(MAX([[[Sampler instance] getOSInfo:YES] score],0.0),1.0) animated:NO];
    }
    for (UIProgressView *scoreBar in self.scoreSameModelProgBar) {
        [scoreBar setProgress:MIN(MAX([[[Sampler instance] getModelInfo:YES] score],0.0),1.0) animated:NO];
    }
    for (UIProgressView *scoreBar in self.scoreSimilarAppsProgBar) {
        [scoreBar setProgress:MIN(MAX([[[Sampler instance] getSimilarAppsInfo:YES] score],0.0),1.0) animated:NO];
    }
    
    DLog(@"jscore: %f, updated: %f, os: %f, model: %f, apps: %f", (MIN( MAX([[Sampler instance] getJScore], -1.0), 1.0)*100), howLong, MIN(MAX([[[Sampler instance] getOSInfo:YES] score],0.0),1.0), [[[Sampler instance] getModelInfo:YES] score], [[[Sampler instance] getSimilarAppsInfo:YES] score]);
    [self.view setNeedsDisplay];
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
