//
//  BugReportViewController.m
//  Carat
//
//  Created by Adam Oliner on 10/6/11.
//  Copyright 2011 UC Berkeley. All rights reserved.
//

#import "BugReportViewController.h"
#import "BugDetailViewController.h"
#import "ReportItemCell.h"
#import "FlurryAnalytics.h"

@implementation BugReportViewController

@synthesize dataTable = _bugTable;


// The designated initializer. Override to perform setup that is required before the view is loaded.
- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
	self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
	if (self) {
        self.title = @"Bug Report";
        self.tabBarItem.image = [UIImage imageNamed:@"bug"];
    }
    return self;
}

#pragma mark - table methods

- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section
{
    return @"Energy Bugs";
}

// loads the selected detail view
- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    ReportItemCell *selectedCell = (ReportItemCell *)[tableView cellForRowAtIndexPath:indexPath];
    [selectedCell setSelected:NO animated:YES];
    
    BugDetailViewController *dvController = [[[BugDetailViewController alloc] initWithNibName:@"BugDetailView" bundle:nil] autorelease];
    [self.navigationController pushViewController:dvController animated:YES];
    
    [[dvController appName] makeObjectsPerformSelector:@selector(setText:) withObject:selectedCell.appName.text];
    [[dvController appIcon] makeObjectsPerformSelector:@selector(setImage:) withObject:[UIImage imageNamed:[selectedCell.appName.text stringByAppendingString:@".png"]]];
    [[dvController appScore] makeObjectsPerformSelector:@selector(setProgress:) withObject:[listOfAppScores objectAtIndex:indexPath.row]];
    [FlurryAnalytics logEvent:@"selectedBugDetail"
               withParameters:[NSDictionary dictionaryWithObjectsAndKeys:selectedCell.appName.text, @"App Name", nil]];
}

#pragma mark - View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view, typically from a nib.
    
    // TODO: remove DUMMY DATA
    //Initialize the arrays.
    listOfAppNames = [[NSMutableArray alloc] init];
    listOfAppScores = [[NSMutableArray alloc] init];
    
    //Add items
    [listOfAppNames addObject:@"Pandora Radio"];
    [listOfAppNames addObject:@"Facebook"];
    [listOfAppNames addObject:@"Paper Toss"];
    [listOfAppNames addObject:@"Shazam"];
    [listOfAppNames addObject:@"Angry Birds"];
    
    [listOfAppScores addObject:[NSNumber numberWithFloat:0.95f]];
    [listOfAppScores addObject:[NSNumber numberWithFloat:0.93f]];
    [listOfAppScores addObject:[NSNumber numberWithFloat:0.47f]];
    [listOfAppScores addObject:[NSNumber numberWithFloat:0.29f]];
    [listOfAppScores addObject:[NSNumber numberWithFloat:0.1f]];
    
    //Set the title
    self.navigationItem.title = @"Energy Bugs";
}

- (void)viewDidUnload
{
    [dataTable release];
    [self setDataTable:nil];
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

- (void)dealloc {
    [dataTable release];
    [super dealloc];
}
@end
