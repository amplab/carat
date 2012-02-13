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

@implementation ActionViewController

@synthesize actionStrings, actionValues;

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
    return [actionStrings count];
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
    cell.actionString.text = [actionStrings objectAtIndex:indexPath.row];
    if ([[actionValues objectAtIndex:indexPath.row] doubleValue] <= 0) {
        cell.actionValue.text = @"+100 karma!";
    } else {
        cell.actionValue.text = [Utilities formatNSTimeIntervalAsNSString:[[actionValues objectAtIndex:indexPath.row] doubleValue]];
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
//    ReportItemCell *selectedCell = (ReportItemCell *)[tableView cellForRowAtIndexPath:indexPath];
//    [selectedCell setSelected:NO animated:YES];
//    
//    DetailViewController *dvController = [self getDetailView];
//    HogsBugs *hb = [[self.report hbList] objectAtIndex:indexPath.row];
//    [dvController setXVals:[hb xVals]];
//    [dvController setYVals:[hb yVals]];
//    [dvController setXValsWithout:[hb xValsWithout]];
//    [dvController setYValsWithout:[hb yValsWithout]];
//    [self.navigationController pushViewController:dvController animated:YES];
//    
//    [[dvController appName] makeObjectsPerformSelector:@selector(setText:) withObject:selectedCell.appName.text];
//    
//    UIImage *img = [UIImage newImageNotCached:[selectedCell.appName.text stringByAppendingString:@".png"]];
//    if (img == nil) {
//        img = [UIImage newImageNotCached:@"icon57.png"];
//    }
//    [[dvController appIcon] makeObjectsPerformSelector:@selector(setImage:) withObject:img];
//    [img release];
//    
//    for (UIProgressView *pBar in [dvController appScore]) {
//        [pBar setProgress:selectedCell.appScore.progress animated:NO];
//    }
//    
//    [[dvController thisText] makeObjectsPerformSelector:@selector(setText:) withObject:self.thisText];
//    [[dvController thatText] makeObjectsPerformSelector:@selector(setText:) withObject:self.thatText];
//    
//    [FlurryAnalytics logEvent:[@"selected" stringByAppendingString:self.detailViewName]
//               withParameters:[NSDictionary dictionaryWithObjectsAndKeys:selectedCell.appName.text, @"App Name", nil]];
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
    
    
    // TODO Remove dummy data
    
    //Initialize the arrays.
    actionStrings = [[NSMutableArray alloc] init];
    actionValues = [[NSMutableArray alloc] init];

    //Add items
    [actionStrings addObject:@"Kill Pandora Radio"];
    [actionStrings addObject:@"Restart Facebook"];
    [actionStrings addObject:@"Upgrade to iOS 5.0.1"];
    [actionStrings addObject:@"Dim the screen"];
    [actionStrings addObject:@"Turn off WiFi"];
    [actionStrings addObject:@"Help Spread the Word!"];
    
    [actionValues addObject:[NSNumber numberWithInt:154400]];
    [actionValues addObject:[NSNumber numberWithInt:7990]];
    [actionValues addObject:[NSNumber numberWithInt:3583]];
    [actionValues addObject:[NSNumber numberWithInt:1020]];
    [actionValues addObject:[NSNumber numberWithInt:650]];
    [actionValues addObject:[NSNumber numberWithInt:-1]];
}

- (void)viewDidUnload
{
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
    // TODO
    
    [self.view setNeedsDisplay];
}

- (void)dealloc {
    [dataTable release];
    [super dealloc];
}
@end
