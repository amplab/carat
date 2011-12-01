//
//  HogReportViewController.m
//  Carat
//
//  Created by Adam Oliner on 10/6/11.
//  Copyright 2011 UC Berkeley. All rights reserved.
//

#import "HogReportViewController.h"
#import "ReportItemCell.h"

@implementation HogReportViewController

@synthesize hogTable = _hogTable;
@synthesize lastUpdatedString = _lastUpdatedString;



// The designated initializer. Override to perform setup that is required before the view is loaded.
- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
	self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
	if (self) {
        self.title = @"Hog Report";
        self.tabBarItem.image = [UIImage imageNamed:@"hog"];
    }
    return self;
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    // Release any cached data, images, etc that aren't in use.
}

#pragma mark - table methods

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
    return [listOfAppNames count];
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    
    static NSString *CellIdentifier = @"ReportViewCell";
    
    ReportItemCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    if (cell == nil) {
        NSArray *topLevelObjects = [[NSBundle mainBundle] loadNibNamed:@"ReportItemCell" owner:nil options:nil];
        for (id currentObject in topLevelObjects) {
            if ([currentObject isKindOfClass:[UITableViewCell class]]) {
                cell = (ReportItemCell *)currentObject;
                break;
            }
        }
    }
    
    // Set up the cell...
    NSString *appName = [listOfAppNames objectAtIndex:indexPath.row];
    cell.appName.text = appName;
    NSString *iconPath = [appName stringByAppendingString:@".jpeg"];
    
    //cell.appIcon.image = [UIImage imageWithContentsOfFile:[appName stringByAppendingString:@".jpeg"]];
    cell.appIcon.image = [UIImage imageNamed:iconPath];
    cell.appScore.progress = [[listOfAppScores objectAtIndex:indexPath.row] floatValue];
    return cell;
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
    [listOfAppNames addObject:@"Words With Friends"];
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
    
    //Set the title
    self.navigationItem.title = @"Energy Hogs";
    
}

- (void)viewDidUnload
{
    [hogTable release];
    hogTable = nil;
    [self setHogTable:nil];
    [lastUpdatedString release];
    lastUpdatedString = nil;
    [self setLastUpdatedString:nil];
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
}

- (void)viewWillDisappear:(BOOL)animated
{
	[super viewWillDisappear:animated];
}

- (void)viewDidDisappear:(BOOL)animated
{
	[super viewDidDisappear:animated];
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    // Return YES for supported orientations
    if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPhone) {
        return (interfaceOrientation != UIInterfaceOrientationPortraitUpsideDown);
    } else {
        return YES;
    }
}


- (void)dealloc {
    [hogTable release];
    [lastUpdatedString release];
    [listOfAppNames release];
    [listOfAppScores release];
    [super dealloc];
}
@end
