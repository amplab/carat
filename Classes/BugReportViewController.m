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
#import "CoreDataManager.h"

@implementation BugReportViewController

// The designated initializer. Override to perform setup that is required before the view is loaded.
- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil {
	self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
	if (self) {
        self.title = @"Bug Report";
        self.tabBarItem.image = [UIImage imageNamed:@"bug"];
        self.detailViewName = @"BugDetailView";
        self.tableTitle = @"Energy Bugs";
        self.thisText = @"(Running Here)";
        self.thatText = @"(Running Elsewhere)";
    }
    return self;
}

- (DetailViewController *)getDetailView
{
    return [[[BugDetailViewController alloc] initWithNibName:@"DetailView" bundle:nil] autorelease];
}

#pragma mark - View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];
	// Do any additional setup after loading the view, typically from a nib.

    [self setReport:[[CoreDataManager instance] getBugs:NO]];
    
//    //Initialize the arrays.
//    listOfAppNames = [[NSMutableArray alloc] init];
//    listOfAppScores = [[NSMutableArray alloc] init];
//    
//    //Add items
//    [listOfAppNames addObject:@"Pandora Radio"];
//    [listOfAppNames addObject:@"Facebook"];
//    [listOfAppNames addObject:@"Paper Toss"];
//    [listOfAppNames addObject:@"Shazam"];
//    [listOfAppNames addObject:@"Angry Birds"];
//    
//    [listOfAppScores addObject:[NSNumber numberWithFloat:0.95f]];
//    [listOfAppScores addObject:[NSNumber numberWithFloat:0.93f]];
//    [listOfAppScores addObject:[NSNumber numberWithFloat:0.47f]];
//    [listOfAppScores addObject:[NSNumber numberWithFloat:0.29f]];
//    [listOfAppScores addObject:[NSNumber numberWithFloat:0.1f]];
}

@end
