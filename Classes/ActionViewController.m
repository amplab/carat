//
//  ActionViewController.m
//  Carat
//
//  Created by Adam Oliner on 2/7/12.
//  Copyright (c) 2012 UC Berkeley. All rights reserved.
//

#import "ActionViewController.h"
#import "Sampler.h"
#import "Utilities.h"
#import "ActionItemCell.h"
#import "UIImageDoNotCache.h"
#import "InstructionViewController.h"
#import "FlurryAnalytics.h"
#import "ActionObject.h"

@implementation ActionViewController

@synthesize actionList, actionTable;

@synthesize dataTable;

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        self.title = @"Actions";
        self.tabBarItem.image = [UIImage imageNamed:@"53-house"];
    }
    return self;
}

- (void)didReceiveMemoryWarning
{
    // Releases the view if it doesn't have a superview.
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


#pragma mark - table methods

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return [actionList count];
}

- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section
{
    return @"To improve battery life...";
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    static NSString *CellIdentifier = @"ActionItemCell";
    
    ActionItemCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    if (cell == nil) {
        NSArray *topLevelObjects = [[NSBundle mainBundle] loadNibNamed:@"ActionItemCell" owner:nil options:nil];
        for (id currentObject in topLevelObjects) {
            if ([currentObject isKindOfClass:[UITableViewCell class]]) {
                cell = (ActionItemCell *)currentObject;
                break;
            }
        }
    }
    
    // Set up the cell...
    ActionObject *act = [self.actionList objectAtIndex:indexPath.row];
    cell.actionString.text = act.actionText;
    if (act.actionBenefit <= 0) { // already filtered out benefits < 60 seconds
        cell.actionValue.text = @"+100 karma!";
        cell.actionType = ActionTypeSpreadTheWord;
    } else {
        cell.actionValue.text = [Utilities formatNSTimeIntervalAsNSString:[[NSNumber numberWithInt:act.actionBenefit] doubleValue]];
        cell.actionType = act.actionType;
    }
    
    return cell;
}

- (NSString *)tableView:(UITableView *)tableView titleForFooterInSection:(NSInteger)section
{
    NSDate *lastUpdated = [[Sampler instance] getLastReportUpdateTimestamp];
    NSDate *now = [NSDate date];
    NSTimeInterval howLong = [now timeIntervalSinceDate:lastUpdated];
    return [Utilities formatNSTimeIntervalAsUpdatedNSString:howLong];
}

// loads the selected detail view
- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    ActionItemCell *selectedCell = (ActionItemCell *)[tableView cellForRowAtIndexPath:indexPath];
    [selectedCell setSelected:NO animated:YES];
    
    if (selectedCell.actionType == ActionTypeSpreadTheWord) {
        [self shareHandler];
    } else {
        InstructionViewController *ivController = [[InstructionViewController alloc] initWithNibName:@"InstructionView" actionType:selectedCell.actionType];
        [self.navigationController pushViewController:ivController animated:YES];
        [ivController release];
        [FlurryAnalytics logEvent:@"selectedInstructionView"];
    }
}

#pragma mark - share handler

- (void)shareHandler {
    UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Temporarily Disabled" 
                                                    message:@"This feature is disabled while Carat is in beta." 
                                                   delegate:nil 
                                          cancelButtonTitle:@"OK"
                                          otherButtonTitles:nil];
    [alert show];
    [alert release];
    
    [FlurryAnalytics logEvent:@"selectedSpreadTheWord"];
    
    // TODO reactivated before submitting to Apple
    //	// Create the item to share (in this example, a url)
    //	NSURL *url = [NSURL URLWithString:@"http://carat.cs.berkeley.edu"];
    //	SHKItem *item = [SHKItem URL:url title:@"Learn about your mobile device's battery usage. For science! (Seriously.)"];
    //    
    //	// Get the ShareKit action sheet
    //	SHKActionSheet *actionSheet = [SHKActionSheet actionSheetForItem:item];
    //    
    //	// Display the action sheet
    //	[actionSheet showFromTabBar:self.tabBarController.tabBar];
}


#pragma mark - View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
    
    [self updateView];
}

- (void)viewDidUnload
{
    [HUD release];
    [actionList release];
    [self setActionList:nil];
    [actionTable release];
    [self setActionTable:nil];
    [dataTable release];
    [self setDataTable:nil];
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    
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

- (BOOL) shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)orientation {
    return YES;
}

- (void)updateView {
    [self setActionList:[[[NSMutableArray alloc] init] autorelease]];
    
    ActionObject *tmpAction;
    
    // get Hogs, filter negative actionBenefits, fill mutable array
    NSArray *tmp = [[Sampler instance] getHogs].hbList;
    if (tmp != nil) {
        for (HogsBugs *hb in tmp) {
            if ([hb appName] == nil ||
                [hb expectedValue] <= 0 ||
                [hb expectedValueWithout] <= 0) continue;
            
            NSInteger benefit = (int) (10000/[hb expectedValue] - 10000/[hb expectedValueWithout]);
            if (benefit <= 60) continue;
            
            tmpAction = [[ActionObject alloc] init];
            [tmpAction setActionText:[@"Kill " stringByAppendingString:[hb appName]]];
            [tmpAction setActionType:ActionTypeKillApp];
            [tmpAction setActionBenefit:benefit];
            [self.actionList addObject:tmpAction];
            [tmpAction release];
        }
    }
    
    // get Bugs, add to array
    tmp = [[Sampler instance] getBugs].hbList;
    if (tmp != nil) {
        for (HogsBugs *hb in tmp) {
            if ([hb appName] == nil ||
                [hb expectedValue] <= 0 ||
                [hb expectedValueWithout] <= 0) continue;
            
            NSInteger benefit = (int) (10000/[hb expectedValue] - 10000/[hb expectedValueWithout]);
            if (benefit <= 60) continue;
            
            tmpAction = [[ActionObject alloc] init];
            [tmpAction setActionText:[@"Restart " stringByAppendingString:[hb appName]]];
            [tmpAction setActionType:ActionTypeRestartApp];
            [tmpAction setActionBenefit:benefit];
            [self.actionList addObject:tmpAction];
            [tmpAction release];
        }
    }
    
    // get OS
    DetailScreenReport *dscWith = [[[Sampler instance] getOSInfo:YES] retain];
    DetailScreenReport *dscWithout = [[[Sampler instance] getOSInfo:NO] retain];
    
    if (dscWith != nil && dscWithout != nil) {
        if (dscWith.expectedValue > 0 &&
            dscWithout.expectedValue > 0) {
            NSInteger benefit = (int) (10000/dscWith.expectedValue - 10000/dscWithout.expectedValue);
            if (benefit > 60) {
                tmpAction = [[ActionObject alloc] init];
                [tmpAction setActionText:@"Upgrade the Operating System"];
                [tmpAction setActionType:ActionTypeUpgradeOS];
                [tmpAction setActionBenefit:benefit];
                [self.actionList addObject:tmpAction];
                [tmpAction release];
            }
        }
    }
    
    [dscWith release];
    [dscWithout release];

    // sharing Action
    tmpAction = [[ActionObject alloc] init];
    [tmpAction setActionText:@"Help Spread the Word!"];
    [tmpAction setActionType:ActionTypeSpreadTheWord];
    [tmpAction setActionBenefit:-1];
    [self.actionList addObject:tmpAction];
    [tmpAction release];
    
    //the "key" is the *name* of the @property as a string.  So you can also sort by @"label" if you'd like
    [self.actionList sortUsingDescriptors:[NSArray arrayWithObject:[NSSortDescriptor sortDescriptorWithKey:@"actionBenefit" ascending:NO]]];
    
    [self.actionTable reloadData];
    [self.view setNeedsDisplay];
}

- (void)dealloc {
    [HUD release];
    [actionList release];
    [actionTable release];
    [dataTable release];
    [super dealloc];
}
@end






