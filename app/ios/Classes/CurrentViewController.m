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
#import "CoreDataManager.h"
#import "CommunicationManager.h"
#import "UIDeviceHardware.h"
#import "Globals.h"
#import "UIImageDoNotCache.h"
#import "InstructionViewController.h"
#import "ProcessListViewController.h"

@implementation CurrentViewController

@synthesize jscore = _jscore;
@synthesize expectedLife = _expectedLife;
@synthesize lastUpdated = _lastUpdated;
//@synthesize sinceLastWeekString = _sinceLastWeekString;
@synthesize osVersion = _osVersion;
@synthesize deviceModel = _deviceModel;
@synthesize memUsed = _memUsed;
@synthesize memActive = _memActive;
@synthesize portraitView, landscapeView;

// The designated initializer. Override to perform setup that is required before the view is loaded.
- (id) initWithNibName: (NSString *) nibNameOrNil 
                bundle: (NSBundle *)nibBundleOrNil 
{
	self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
	if (self) {
        self.title = @"My Device";
        self.tabBarItem.image = [UIImage imageNamed:@"32-iphone"];
        self->MAX_LIFE = 1209600;
    }
    
    return self;
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Release any cached data, images, etc that aren't in use.
}

- (void)loadDataWithHUD:(id)obj
{
    if ([[CoreDataManager instance] getReportUpdateStatus] == nil) {
        // *probably* no update in progress, reload table data while locking out view
        HUD = [[MBProgressHUD alloc] initWithView:self.tabBarController.view];
        [self.tabBarController.view addSubview:HUD];
        
        HUD.dimBackground = YES;
        
        // Register for HUD callbacks so we can remove it from the window at the right time
        HUD.delegate = self;
        HUD.labelText = @"Updating Device Data";
        
        [HUD showWhileExecuting:@selector(updateView) onTarget:self withObject:nil animated:YES];
    }
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

- (IBAction)getProcessList:(id)sender
{
    ProcessListViewController *plvController = [[ProcessListViewController alloc] initWithNibName:@"ProcessListView" bundle:nil];
    [self.navigationController pushViewController:plvController animated:YES];
    [plvController release];
    [FlurryAnalytics logEvent:@"selectedProcessList"];
}

- (IBAction)getJScoreInfoScreen:(id)sender
{
    InstructionViewController *ivController = [[InstructionViewController alloc] initWithNibName:@"InstructionView" actionType:ActionTypeJScoreInfo];
    [self.navigationController pushViewController:ivController animated:YES];
    [ivController release];
    [FlurryAnalytics logEvent:@"selectedJScoreInfo"];
}

- (IBAction)getMemoryInfo:(id)sender
{
    InstructionViewController *ivController = [[InstructionViewController alloc] initWithNibName:@"InstructionView" actionType:ActionTypeMemoryInfo];
    [self.navigationController pushViewController:ivController animated:YES];
    [ivController release];
    [FlurryAnalytics logEvent:@"selectedMemoryInfo"];
}

- (DetailViewController *)getDetailView
{
    DetailViewController *detailView = [[[DetailViewController alloc] initWithNibName:@"DetailView" bundle:nil] autorelease];
    detailView.navTitle = @"Category Detail";
    return detailView;
}

- (IBAction)getSameOSDetail:(id)sender
{
    DetailScreenReport *dsr = [[CoreDataManager instance] getOSInfo:YES];
    if (dsr == nil || [dsr xVals] == nil || [[dsr xVals] count] == 0) {
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
        
        dsr = [[CoreDataManager instance] getOSInfo:NO];
        [dvController setXValsWithout:[dsr xVals]];
        [dvController setYValsWithout:[dsr yVals]];
        
        [self.navigationController pushViewController:dvController animated:YES];
        
        [[dvController appName] makeObjectsPerformSelector:@selector(setText:) withObject:@"Same Operating System"];
        UIImage *img = [UIImage newImageNotCached:@"icon57.png"];
        [[dvController appIcon] makeObjectsPerformSelector:@selector(setImage:) withObject:img];
        [img release];
        for (UIProgressView *pBar in [dvController appScore]) {
            [pBar setProgress:MIN(MAX([[[CoreDataManager instance] getOSInfo:YES] score],0.0),1.0) animated:NO];
        }
        
        [[dvController thisText] makeObjectsPerformSelector:@selector(setText:) withObject:@"Same OS"];
        [[dvController thatText] makeObjectsPerformSelector:@selector(setText:) withObject:@"Different OS"];
        
        [FlurryAnalytics logEvent:@"selectedSameOS"
                   withParameters:[NSDictionary dictionaryWithObjectsAndKeys:[[UIDevice currentDevice] systemVersion], @"OS Version", nil]];
    }
}

- (IBAction)getSameModelDetail:(id)sender
{
    DetailScreenReport *dsr = [[CoreDataManager instance] getModelInfo:YES];
    if (dsr == nil || [dsr xVals] == nil || [[dsr xVals] count] == 0) {
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
        dsr = [[CoreDataManager instance] getModelInfo:NO];
        [dvController setXValsWithout:[dsr xVals]];
        [dvController setYValsWithout:[dsr yVals]];
        
        [self.navigationController pushViewController:dvController animated:YES];
        
        [[dvController appName] makeObjectsPerformSelector:@selector(setText:) withObject:@"Same Device Model"];
        UIImage *img = [UIImage newImageNotCached:@"icon57.png"];
        [[dvController appIcon] makeObjectsPerformSelector:@selector(setImage:) withObject:img];
        [img release];
        for (UIProgressView *pBar in [dvController appScore]) {
            [pBar setProgress:MIN(MAX([[[CoreDataManager instance] getModelInfo:YES] score],0.0),1.0) animated:NO];
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
    DetailScreenReport *dsr = [[CoreDataManager instance] getSimilarAppsInfo:YES];
    if (dsr == nil || [dsr xVals] == nil || [[dsr xVals] count] == 0) {
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
        dsr = [[CoreDataManager instance] getSimilarAppsInfo:NO];
        [dvController setXValsWithout:[dsr xVals]];
        [dvController setYValsWithout:[dsr yVals]];
        
        [self.navigationController pushViewController:dvController animated:YES];
        
        [[dvController appName] makeObjectsPerformSelector:@selector(setText:) withObject:@"Similar Apps"];
        UIImage *img = [UIImage newImageNotCached:@"icon57.png"];
        [[dvController appIcon] makeObjectsPerformSelector:@selector(setImage:) withObject:img];
        [img release];
        for (UIProgressView *pBar in [dvController appScore]) {
            [pBar setProgress:MIN(MAX([[[CoreDataManager instance] getSimilarAppsInfo:YES] score],0.0),1.0) animated:NO];
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
    [jscore release];
    [self setJscore:nil];
    [lastUpdated release];
    [self setLastUpdated:nil];
    [expectedLife release];
    [self setExpectedLife:nil];
    [portraitView release];
    [self setPortraitView:nil];
    [landscapeView release];
    [self setLandscapeView:nil];
    [memUsed release];
    [self setMemUsed:nil];
    [memActive release];
    [self setMemActive:nil];
    
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
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(loadDataWithHUD:) 
                                                 name:@"CCDMReportUpdateStatusNotification"
                                               object:nil];
}

- (void)viewWillDisappear:(BOOL)animated
{
	[super viewWillDisappear:animated];
    
    [[NSNotificationCenter defaultCenter] removeObserver:self
                                                    name:@"CCDMReportUpdateStatusNotification" object:nil];
    [self.navigationController setNavigationBarHidden:NO animated:YES];
}

- (void)viewDidDisappear:(BOOL)animated
{
	[super viewDidDisappear:animated];
}

- (void)updateView
{
    // J-Score
    [[self jscore] makeObjectsPerformSelector:@selector(setText:) withObject:[[NSNumber numberWithInt:(int)(MIN( MAX([[CoreDataManager instance] getJScore], -1.0), 1.0)*100)] stringValue]];
    
    // Expected Battery Life
    NSTimeInterval eb; // expected life in seconds
    double jev = [[[CoreDataManager instance] getJScoreInfo:YES] expectedValue];
    if (jev > 0) eb = MIN(MAX_LIFE,100/jev);
    else eb = MAX_LIFE;
    for (UILabel *el in self.expectedLife) {
        el.text = [Utilities formatNSTimeIntervalAsNSString:eb];
    }
    
    // Last Updated
    NSTimeInterval howLong = [[NSDate date] timeIntervalSinceDate:[[CoreDataManager instance] getLastReportUpdateTimestamp]];
    for (UILabel *lastUp in self.lastUpdated) {
        lastUp.text = [Utilities formatNSTimeIntervalAsUpdatedNSString:howLong];
    }
    
    // Change since last week
//    [[self sinceLastWeekString] makeObjectsPerformSelector:@selector(setText:) withObject:[[[[CoreDataManager instance] getChangeSinceLastWeek] objectAtIndex:0] stringByAppendingString:[@" (" stringByAppendingString:[[[[CoreDataManager instance] getChangeSinceLastWeek] objectAtIndex:1] stringByAppendingString:@"%)"]]]];
    
    DLog(@"jscore: %f, updated: %f, os: %f, model: %f, apps: %f", (MIN( MAX([[CoreDataManager instance] getJScore], -1.0), 1.0)*100), howLong, MIN(MAX([[[CoreDataManager instance] getOSInfo:YES] score],0.0),1.0), [[[CoreDataManager instance] getModelInfo:YES] score], [[[CoreDataManager instance] getSimilarAppsInfo:YES] score]);
    
    //  Memory info.
    mach_msg_type_number_t count = HOST_VM_INFO_COUNT;
    vm_statistics_data_t vmstat;
    if (host_statistics(mach_host_self(), HOST_VM_INFO, (host_info_t)&vmstat, &count) == KERN_SUCCESS)
    {
        int active = vmstat.active_count;
        int free = vmstat.free_count;
        int used = vmstat.wire_count+active+vmstat.inactive_count;
        float frac_used = ((float)(used) / (float)(used+free));
        float frac_active = ((float)(active) / (float)(used));
        DLog(@"Active memory: %f, Used memory: %f", frac_active, frac_used);
        for (UIProgressView *memUsedProg in self.memUsed) {
            memUsedProg.progress = frac_used;
        }
        for (UIProgressView *memActiveProg in self.memActive) {
            memActiveProg.progress = frac_active;
        }
    }

    // Device info
    UIDeviceHardware *h =[[UIDeviceHardware alloc] init];
    for (UILabel *mod in self.deviceModel) {
        mod.text = [h platformString];
    }
    [h release];
    
    for (UILabel *os in self.osVersion) {
        os.text = [UIDevice currentDevice].systemVersion;
    }
    
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
    [jscore release];
    [lastUpdated release];
    [expectedLife release];
    [portraitView release];
    [landscapeView release];
    [memUsed release];
    [memActive release];
    [super dealloc];
}

@end
