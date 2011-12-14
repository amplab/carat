//
//  HogReportViewController.m
//  Carat
//
//  Created by Adam Oliner on 10/6/11.
//  Copyright 2011 UC Berkeley. All rights reserved.
//

#import "HogReportViewController.h"
#import "HogDetailViewController.h"
#import "ReportItemCell.h"
#import "FlurryAnalytics.h"

@implementation HogReportViewController

@synthesize dataTable = _hogTable;


// The designated initializer. Override to perform setup that is required before the view is loaded.
- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
	self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
	if (self) {
        self.title = @"Hog Report";
        self.tabBarItem.image = [UIImage imageNamed:@"hog"];
    }
    return self;
}

#pragma mark - table methods

- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section
{
    return @"Energy Hogs";
}

// loads the selected detail view
- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    ReportItemCell *selectedCell = (ReportItemCell *)[tableView cellForRowAtIndexPath:indexPath];
    [selectedCell setSelected:NO animated:YES];
    
    HogDetailViewController *dvController = [[[HogDetailViewController alloc] initWithNibName:@"HogDetailView" bundle:nil] autorelease];
    [self.navigationController pushViewController:dvController animated:YES];
    
    [[dvController appName] makeObjectsPerformSelector:@selector(setText:) withObject:selectedCell.appName.text];
    [[dvController appIcon] makeObjectsPerformSelector:@selector(setImage:) withObject:[UIImage imageNamed:[selectedCell.appName.text stringByAppendingString:@".png"]]];
    [[dvController appScore] makeObjectsPerformSelector:@selector(setProgress:) withObject:[listOfAppScores objectAtIndex:indexPath.row]];
    [FlurryAnalytics logEvent:@"selectedHogDetail"
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
    [listOfAppNames addObject:@"Camera+"];
    [listOfAppNames addObject:@"Fruit Ninja"];
    [listOfAppNames addObject:@"Skype"];
    [listOfAppNames addObject:@"Words With Friends HD"];
    [listOfAppNames addObject:@"Twitter"];
    [listOfAppNames addObject:@"Cut the Rope"];
    [listOfAppNames addObject:@"Angry Birds"];
    [listOfAppNames addObject:@"Shazam"];
    
    [listOfAppScores addObject:[NSNumber numberWithFloat:0.9f]];
    [listOfAppScores addObject:[NSNumber numberWithFloat:0.86f]];
    [listOfAppScores addObject:[NSNumber numberWithFloat:0.85f]];
    [listOfAppScores addObject:[NSNumber numberWithFloat:0.79f]];
    [listOfAppScores addObject:[NSNumber numberWithFloat:0.4f]];
    [listOfAppScores addObject:[NSNumber numberWithFloat:0.38f]];
    [listOfAppScores addObject:[NSNumber numberWithFloat:0.29f]];
    [listOfAppScores addObject:[NSNumber numberWithFloat:0.01f]];
    
    //Setup the navigation
    self.navigationItem.title = @"Energy Hogs";
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
