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

#pragma mark - table methods

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return [actionStrings count];
}

- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section
{
    return @"Action List";
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
    cell.actionValue.text = [Utilities formatNSTimeIntervalAsNSString:[[actionValues objectAtIndex:indexPath.row] doubleValue]];
    
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
        [actionStrings addObject:@"Restart Paper Toss"];
        [actionStrings addObject:@"Kill Shazam"];
        [actionStrings addObject:@"Kill Angry Birds"];
        
        [actionValues addObject:[NSNumber numberWithInt:154400]];
        [actionValues addObject:[NSNumber numberWithInt:7990]];
        [actionValues addObject:[NSNumber numberWithInt:3583]];
        [actionValues addObject:[NSNumber numberWithInt:1020]];
        [actionValues addObject:[NSNumber numberWithInt:650]];
}

- (void)viewDidUnload
{
    [self setDataTable:nil];
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
    [dataTable release];
    [super dealloc];
}
@end
