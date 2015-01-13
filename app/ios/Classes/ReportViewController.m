//
//  ReportViewController.m
//  Carat
//
//  Created by Adam Oliner on 12/14/11.
//  Copyright (c) 2011 UC Berkeley. All rights reserved.
//

#import "ReportViewController.h"
#import "ReportItemCell.h"
#import "Utilities.h"
#import "DetailViewController.h"
#import "HiddenAppsViewController.h"
#import "Flurry.h"
#import "UIImageDoNotCache.h"

@implementation ReportViewController

@synthesize detailViewName;
@synthesize tableTitle;
@synthesize thisText;
@synthesize thatText;

@synthesize report;

@synthesize dataTable = _dataTable;

// The designated initializer. Override to perform setup that is required before the view is loaded.
- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
	self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
	if (self) {
        // custom code (overridden by subclass)
    }
    return self;
}

- (void)didReceiveMemoryWarning
{
    DLog(@"Memory warning.");
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
}

// overridden by subclasses
- (DetailViewController *)getDetailView
{
    return nil;
}

// overridden by subclasses
- (void)loadDataWithHUD:(id)obj { }
- (void)updateView {
	NSLog(@"");
}
- (void)reloadReport { }


#pragma mark - MBProgressHUDDelegate method

- (void)hudWasHidden:(MBProgressHUD *)hud
{
    // Remove HUD from screen when the HUD was hidded
    [HUD removeFromSuperview];
    [HUD release];
	HUD = nil;
}


#pragma mark - button actions

- (IBAction)showHiddenAppsPressed:(id)sender {
    HiddenAppsViewController *haView = [[[HiddenAppsViewController alloc] initWithNibName:@"HiddenAppsView" bundle:nil] autorelease];
    [self.navigationController pushViewController:haView animated:YES];
    
    [Flurry logEvent:@"selectedShowHiddenApps"];
}


#pragma mark - table methods

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    if (report != nil && [report hbListIsSet]) {
        return [[report hbList] count];
    } else return 0;
}

- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section
{
    return self.tableTitle;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    
    static NSString *CellIdentifier = @"ReportItemCell";
    
    ReportItemCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    if (cell == nil) {
        NSArray *topLevelObjects = [[NSBundle mainBundle] loadNibNamed:@"ReportItemCell" owner:nil options:nil];
        for (id currentObject in topLevelObjects) {
            if ([currentObject isKindOfClass:[ReportItemCell class]]) {
                cell = (ReportItemCell *)currentObject;
                break;
            }
        }
    }
    
    // Set up the cell...
    HogsBugs *hb = [[report hbList] objectAtIndex:indexPath.row];
    NSString *appName = [hb appName];
    cell.appName.text = appName;
    
    NSString *imageURL = [[@"https://s3.amazonaws.com/carat.icons/" 
                           stringByAppendingString:appName] 
                          stringByAppendingString:@".jpg"];
    
    [cell.appIcon setImageWithURL:[NSURL URLWithString:imageURL]
                 placeholderImage:[UIImage imageNamed:@"icon57.png"]];
    
    double benefit = (100/[hb expectedValueWithout] - 100/[hb expectedValue]);
    double benefit_max = (100/([hb expectedValueWithout]-[hb errorWithout]) - 100/([hb expectedValue]+[hb error]));
    double error = benefit_max-benefit;
    
    cell.appImpact.text =
    [NSString stringWithFormat:@"%@ ± %@", [Utilities doubleAsTimeNSString:benefit], [Utilities doubleAsTimeNSString:error]];
    return cell;
}

- (NSString *)tableView:(UITableView *)tableView titleForFooterInSection:(NSInteger)section
{
    NSString *tmpStatus = [[CoreDataManager instance] getReportUpdateStatus];
    if (tmpStatus == nil) {
        return [Utilities formatNSTimeIntervalAsUpdatedNSString:[[NSDate date] timeIntervalSinceDate:[[CoreDataManager instance] getLastReportUpdateTimestamp]]];
    } else {
        return tmpStatus;
    }
}

// loads the selected detail view
- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    ReportItemCell *selectedCell = (ReportItemCell *)[tableView cellForRowAtIndexPath:indexPath];
    [selectedCell setSelected:NO animated:YES];
    
    DetailViewController *dvController = [self getDetailView];
    HogsBugs *hb = [[self.report hbList] objectAtIndex:indexPath.row];
    
    NSInteger benefit = (int) (100/[hb expectedValueWithout] - 100/[hb expectedValue]);
    NSInteger benefit_max = (int) (100/([hb expectedValueWithout]-[hb errorWithout]) - 100/([hb expectedValue]+[hb error]));
    NSInteger error = (int) (benefit_max-benefit);
    
    [self.navigationController pushViewController:dvController animated:YES];
    // Force view to load. Without this, IBOutletCollections will have zero elements at this point.
    [dvController loadView];
    
    [[dvController appName] makeObjectsPerformSelector:@selector(setText:) withObject:selectedCell.appName.text];
    [[dvController appImpact] makeObjectsPerformSelector:@selector(setText:) withObject:[NSString stringWithFormat:@"%@ ± %@", [Utilities doubleAsTimeNSString:benefit], [Utilities doubleAsTimeNSString:error]]];

    NSString *imageURL = [[@"https://s3.amazonaws.com/carat.icons/"
                           stringByAppendingString:selectedCell.appName.text]
                          stringByAppendingString:@".jpg"];
    DLog(imageURL);
    
    for (UIImageView *appimg in dvController.appIcon) {
        [appimg setImageWithURL:[NSURL URLWithString:imageURL]
               placeholderImage:[UIImage imageNamed:@"icon57.png"]];
    }
    
    [[dvController samplesWith] makeObjectsPerformSelector:@selector(setText:) withObject:[[NSNumber numberWithDouble:[hb samples]] stringValue]];
    [[dvController samplesWithout] makeObjectsPerformSelector:@selector(setText:) withObject:[[NSNumber numberWithDouble:[hb samplesWithout]] stringValue]];
    
    [Flurry logEvent:[@"selected" stringByAppendingString:self.detailViewName]
               withParameters:[NSDictionary dictionaryWithObjectsAndKeys:selectedCell.appName.text, @"App Name", nil]];
}

- (BOOL)tableView:(UITableView *)tableView canEditRowAtIndexPath:(NSIndexPath *)indexPath {
    return YES;
}

- (IBAction)hideAppsPressed:(id)sender {
    BOOL editing = ![self.dataTable isEditing];
    [super setEditing:editing animated:YES];
    [self.dataTable setEditing:editing animated:YES];
    if (editing) {
        [(UIButton *)sender setTitle:@"Done" forState:UIControlStateNormal];
        [(UIButton *)sender setTitleColor:[UIColor colorWithRed:1.0 green:0.4 blue:0 alpha:1.0] forState:UIControlStateNormal];
    } else {
        [(UIButton *)sender setTitle:@"Hide Apps" forState:UIControlStateNormal];
        [(UIButton *)sender setTitleColor:[UIColor colorWithRed:0.0 green:0.4 blue:0.0 alpha:1.0] forState:UIControlStateNormal];
    }
}

- (UITableViewCellEditingStyle)tableView:(UITableView *)tableView editingStyleForRowAtIndexPath:(NSIndexPath *)indexPath {
    return UITableViewCellEditingStyleDelete;
}

- (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath {
    // If row is deleted, remove it from the list.
    if (editingStyle == UITableViewCellEditingStyleDelete) {
        // Delete the row from the data source
        [[Globals instance] hideApp:((ReportItemCell *)[tableView cellForRowAtIndexPath:indexPath]).appName.text];
        [self reloadReport]; // reloads hogs/bugs underlying data to exclude new app
        // Animate the deletion
        [self.dataTable deleteRowsAtIndexPaths:[NSArray arrayWithObject:indexPath] withRowAnimation:UITableViewRowAnimationFade];
    }
}

#pragma mark - View lifecycle

// overridden by subclasses
- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view, typically from a nib.

    //Setup the navigation
    self.navigationItem.title = self.tableTitle;;
}

// overridden by subclasses
- (void)viewWillAppear:(BOOL)animated
{
	[self.navigationController setNavigationBarHidden:YES animated:YES];
	CGRect tableViewFrame = self.dataTable.frame;
	self.dataTable.frame = CGRectMake(0, 0, tableViewFrame.size.width, tableViewFrame.size.height);
    [super viewWillAppear:animated];
    [[CoreDataManager instance] checkConnectivityAndSendStoredDataToServer];
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
}

- (void)viewDidDisappear:(BOOL)animated
{
	[super viewDidDisappear:animated];
}

- (BOOL) shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)orientation {
    return YES;
}

- (void)viewDidUnload
{
    [dataTable release];
    [self setDataTable:nil];
    [report release];
    [self setReport:nil];
    
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

- (void)dealloc {
    [detailViewName release];
    [tableTitle release];
    [thisText release];
    [thatText release];
    [report release];
    [dataTable release];
    [super dealloc];
}

@end
