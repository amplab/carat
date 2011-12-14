//
//  ReportViewController.m
//  Carat
//
//  Created by Adam Oliner on 12/14/11.
//  Copyright (c) 2011 UC Berkeley. All rights reserved.
//

#import "ReportViewController.h"
#import "ReportItemCell.h"
#import "CorePlot-CocoaTouch.h"
#import "Utilities.h"

@implementation ReportViewController

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
    cell.appIcon.image = [UIImage imageNamed:[appName stringByAppendingString:@".png"]];
    cell.appScore.progress = [[listOfAppScores objectAtIndex:indexPath.row] floatValue];
    return cell;
}

- (NSString *)tableView:(UITableView *)tableView titleForFooterInSection:(NSInteger)section
{
    NSDate *lastUpdated = [NSDate dateWithTimeIntervalSinceNow:-100000]; // TODO
    NSDate *now = [NSDate date];
    NSTimeInterval howLong = [now timeIntervalSinceDate:lastUpdated];
    return [Utilities formatNSTimeIntervalAsNSString:howLong];
}

#pragma mark - View lifecycle

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    
    [self.navigationController setNavigationBarHidden:YES animated:YES];
}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
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

- (void)dealloc {
    [listOfAppNames release];
    [listOfAppScores release];
    [super dealloc];
}

@end
