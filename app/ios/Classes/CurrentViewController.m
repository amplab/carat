//
//  CurrentViewController.m
//  Carat
//
//  Created by Adam Oliner on 10/6/11.
//  Copyright 2011 UC Berkeley. All rights reserved.
//

#import "CurrentViewController.h"
#import "Utilities.h"
#import "DetailViewController.h"
#import "Flurry.h"
#import "CoreDataManager.h"
#import "CommunicationManager.h"
#import "UIDeviceHardware.h"
#import "Globals.h"
#import "UIImageDoNotCache.h"
#import "InstructionViewController.h"
#import "ProcessListViewController.h"
#import "CaratConstants.h"

@implementation CurrentViewController



@synthesize jscore = _jscore;
@synthesize expectedLife = _expectedLife;
@synthesize lastUpdated = _lastUpdated;
@synthesize osVersion = _osVersion;
@synthesize deviceModel = _deviceModel;
@synthesize memUsed = _memUsed;
@synthesize memActive = _memActive;
@synthesize uuid = _uuid;

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
    DLog(@"Memory warning.");
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
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
    [Flurry logEvent:@"selectedProcessList"];
}

- (IBAction)getActiveBatteryLifeInfoScreen:(id)sender {
    InstructionViewController *ivController = [[InstructionViewController alloc] initWithNibName:@"InstructionView" actionType:ActionTypeActiveBatteryLifeInfo];
    [self.navigationController pushViewController:ivController animated:YES];
    [ivController release];
    [Flurry logEvent:@"selectedActiveBatteryLifeInfo"];
}

- (IBAction)getJScoreInfoScreen:(id)sender
{
    InstructionViewController *ivController = [[InstructionViewController alloc] initWithNibName:@"InstructionView" actionType:ActionTypeJScoreInfo];
    [self.navigationController pushViewController:ivController animated:YES];
    [ivController release];
    [Flurry logEvent:@"selectedJScoreInfo"];
}

- (IBAction)getMemoryInfo:(id)sender
{
    InstructionViewController *ivController = [[InstructionViewController alloc] initWithNibName:@"InstructionView" actionType:ActionTypeMemoryInfo];
    [self.navigationController pushViewController:ivController animated:YES];
    [ivController release];
    [Flurry logEvent:@"selectedMemoryInfo"];
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
    if (dsr == nil || ![dsr expectedValueIsSet] || [dsr expectedValue] <= 0 || ![dsr errorIsSet] || [dsr error] <= 0) {
        UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Nothing to Report!" 
                                                        message:@"Please check back later; we should have results for your device soon." 
                                                       delegate:nil 
                                              cancelButtonTitle:@"OK"
                                              otherButtonTitles:nil];
        [alert show];
        [alert release];
    } else {
        DetailViewController *dvController = [self getDetailView];

        double expectedValueWithout = [[[CoreDataManager instance] getOSInfo:NO] expectedValue];
        
        NSInteger benefit = (int) (100/expectedValueWithout - 100/[dsr expectedValue]);
        NSInteger benefit_max = (int) (100/(expectedValueWithout-[dsr errorWithout]) - 100/([dsr expectedValue]+[dsr error]));
        NSInteger error = (int) (benefit_max-benefit);
        
        [self.navigationController pushViewController:dvController animated:YES];
        
        [dvController loadView];
        [[dvController appName] makeObjectsPerformSelector:@selector(setText:) withObject:@"Same Operating System"];
        [[dvController appImpact] makeObjectsPerformSelector:@selector(setText:) withObject:[[Utilities formatNSTimeIntervalAsNSString:[[NSNumber numberWithInt:benefit] doubleValue]] stringByAppendingString:[@" ± " stringByAppendingString:[Utilities formatNSTimeIntervalAsNSString:[[NSNumber numberWithInt:error] doubleValue]]]]];
        UIImage *img = [UIImage newImageNotCached:@"icon57.png"];
        [[dvController appIcon] makeObjectsPerformSelector:@selector(setImage:) withObject:img];
        [img release];
        
        [[dvController samplesWith] makeObjectsPerformSelector:@selector(setText:) withObject:[[NSNumber numberWithDouble:[dsr samples]] stringValue]];
        [[dvController samplesWithout] makeObjectsPerformSelector:@selector(setText:) withObject:[[NSNumber numberWithDouble:[dsr samplesWithout]] stringValue]];
        
        [Flurry logEvent:@"selectedSameOS"
                   withParameters:[NSDictionary dictionaryWithObjectsAndKeys:[[UIDevice currentDevice] systemVersion], @"OS Version", nil]];
    }
}

- (IBAction)getSameModelDetail:(id)sender
{
    DetailScreenReport *dsr = [[CoreDataManager instance] getModelInfo:YES];
    if (dsr == nil || ![dsr expectedValueIsSet] || [dsr expectedValue] <= 0 || ![dsr errorIsSet] || [dsr error] <= 0) {
        UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Nothing to Report!" 
                                                        message:@"Please check back later; we should have results for your device soon." 
                                                       delegate:nil 
                                              cancelButtonTitle:@"OK"
                                              otherButtonTitles:nil];
        [alert show];
        [alert release];
    } else {
        DetailViewController *dvController = [self getDetailView];

        double expectedValueWithout = [[[CoreDataManager instance] getModelInfo:NO] expectedValue];
        
        NSInteger benefit = (int) (100/expectedValueWithout - 100/[dsr expectedValue]);
        NSInteger benefit_max = (int) (100/(expectedValueWithout-[dsr errorWithout]) - 100/([dsr expectedValue]+[dsr error]));
        NSInteger error = (int) (benefit_max-benefit);
        
        [self.navigationController pushViewController:dvController animated:YES];
        [dvController loadView];

        [[dvController appName] makeObjectsPerformSelector:@selector(setText:) withObject:@"Same Device Model"];
        [[dvController appImpact] makeObjectsPerformSelector:@selector(setText:) withObject:[[Utilities formatNSTimeIntervalAsNSString:[[NSNumber numberWithInt:benefit] doubleValue]] stringByAppendingString:[@" ± " stringByAppendingString:[Utilities formatNSTimeIntervalAsNSString:[[NSNumber numberWithInt:error] doubleValue]]]]];
        UIImage *img = [UIImage newImageNotCached:@"icon57.png"];
        [[dvController appIcon] makeObjectsPerformSelector:@selector(setImage:) withObject:img];
        [img release];
        
        [[dvController samplesWith] makeObjectsPerformSelector:@selector(setText:) withObject:[[NSNumber numberWithDouble:[dsr samples]] stringValue]];
        [[dvController samplesWithout] makeObjectsPerformSelector:@selector(setText:) withObject:[[NSNumber numberWithDouble:[dsr samplesWithout]] stringValue]];
        
        UIDeviceHardware *h =[[UIDeviceHardware alloc] init];
        [Flurry logEvent:@"selectedSameModel"
                   withParameters:[NSDictionary dictionaryWithObjectsAndKeys:[h platformString], @"Model", nil]];
        [h release];
    }
}

#pragma mark - View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];
   
// Do any additional setup after loading the view, typically from a nib.
    DLog(@"My UUID: %@", [[Globals instance] getUUID]);
}

-(void)viewWillLayoutSubviews
{
	self.view.frame = CGRectMake(0, 0, self.view.frame.size.width, self.view.frame.size.height);

	CGSize scrollSize = [Utilities orientationIndependentScreenSize];
	BOOL isOlderDevice = [Utilities isOlderHeightDevice];

	if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPhone)
	{
		if (isOlderDevice)
			scrollSize.height = scrollSize.height - self.tabBarController.tabBar.frame.size.height + 20;
		else if(!isOlderDevice && UIDeviceOrientationIsLandscape([UIApplication sharedApplication].statusBarOrientation))
			scrollSize.height = scrollSize.height - self.tabBarController.tabBar.frame.size.height - 60;
		else
			scrollSize.height = scrollSize.height - self.tabBarController.tabBar.frame.size.height - 20;

		self.scrollView.contentSize = scrollSize;
		CGSize contentsize = self.scrollView.contentSize;
		CGRect frame = self.uhAmpLogo.frame;
		frame.origin.y = contentsize.height - frame.size.height;
		self.uhAmpLogo.frame = frame;
	}
	else if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad) {

		if (UIDeviceOrientationIsLandscape([UIApplication sharedApplication].statusBarOrientation))
			scrollSize.height = scrollSize.width  - self.tabBarController.tabBar.frame.size.height - 20;
		else
			scrollSize.height = scrollSize.height  - self.tabBarController.tabBar.frame.size.height - 20;

		self.scrollView.contentSize = scrollSize;
		CGSize contentsize = self.scrollView.contentSize;
		CGRect frame = self.uhAmpLogo.frame;
		frame.origin.y = contentsize.height - frame.size.height;
		self.uhAmpLogo.frame = frame;
	}

	self.navigationController.navigationBar.hidden = YES;
	[super viewWillLayoutSubviews];
}

- (void)viewDidUnload
{
    [jscore release];
    [self setJscore:nil];
    [lastUpdated release];
    [self setLastUpdated:nil];
    [expectedLife release];
    [self setExpectedLife:nil];
    [memUsed release];
    [self setMemUsed:nil];
    [memActive release];
    [self setMemActive:nil];
    [uuid release];
    [self setUuid:nil];
    
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    [self updateView];
	[[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(sampleCountUpdated:) name:kSamplesSentCountUpdateNotification object:nil];
}

-(void)sampleCountUpdated:(NSNotification*)notification{
	// Last Updated
	NSTimeInterval howLong = [[NSDate date] timeIntervalSinceDate:[[CoreDataManager instance] getLastReportUpdateTimestamp]];
	for (UILabel *lastUp in self.lastUpdated) {
		lastUp.text = [Utilities formatNSTimeIntervalAsUpdatedNSString:howLong];
	}
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
    if ([[[[self jscore] objectAtIndex:0] text] isEqualToString:@"0"]) {
        [[self jscore] makeObjectsPerformSelector:@selector(setText:) withObject:@"N/A"];
    }
    
    // UUID
    [[self uuid] makeObjectsPerformSelector:@selector(setText:) withObject:[[Globals instance] getUUID]];
    
    // Expected Battery Life
    NSTimeInterval eb; // expected life in seconds
    double jev = [[[CoreDataManager instance] getJScoreInfo:YES] expectedValue];
    if (jev > 0) eb = MIN(MAX_LIFE,100/jev);
    else eb = MAX_LIFE;
    for (UILabel *el in self.expectedLife) {
        el.text = [[Utilities formatNSTimeIntervalAsNSString:eb] stringByTrimmingCharactersInSet:
                   [NSCharacterSet whitespaceAndNewlineCharacterSet]];
    }
    
    // Last Updated
    NSTimeInterval howLong = [[NSDate date] timeIntervalSinceDate:[[CoreDataManager instance] getLastReportUpdateTimestamp]];
    for (UILabel *lastUp in self.lastUpdated) {
        lastUp.text = [Utilities formatNSTimeIntervalAsUpdatedNSString:howLong];
    }
    
    // Change since last week
//    [[self sinceLastWeekString] makeObjectsPerformSelector:@selector(setText:) withObject:[[[[CoreDataManager instance] getChangeSinceLastWeek] objectAtIndex:0] stringByAppendingString:[@" (" stringByAppendingString:[[[[CoreDataManager instance] getChangeSinceLastWeek] objectAtIndex:1] stringByAppendingString:@"%)"]]]];
    
    DLog(@"uuid: %s, jscore: %f, updated: %f, os: %f, model: %f, apps: %f", [[[Globals instance] getUUID] UTF8String], (MIN( MAX([[CoreDataManager instance] getJScore], -1.0), 1.0)*100), howLong, MIN(MAX([[[CoreDataManager instance] getOSInfo:YES] score],0.0),1.0), [[[CoreDataManager instance] getModelInfo:YES] score], [[[CoreDataManager instance] getSimilarAppsInfo:YES] score]);
    
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
        for (UILabel *memUsedProg in self.memUsed) {
            memUsedProg.text = [NSString stringWithFormat:@"%.02f%%",frac_used*100];
        }
        for (UILabel *memActiveProg in self.memActive) {
            memActiveProg.text = [NSString stringWithFormat:@"%.02f%%",frac_active*100];
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

- (BOOL) shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)orientation {
    return YES;
}

- (void)dealloc {
    [jscore release];
    [lastUpdated release];
    [expectedLife release];
    [memUsed release];
    [memActive release];
    [super dealloc];
}

@end
