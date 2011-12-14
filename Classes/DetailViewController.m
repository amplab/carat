//
//  DetailViewController.m
//  Carat
//
//  Created by Adam Oliner on 12/14/11.
//  Copyright (c) 2011 UC Berkeley. All rights reserved.
//

#import "DetailViewController.h"

@implementation DetailViewController

@synthesize firstAppearance = _firstAppearance;
@synthesize navTitle;

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // custom init
    }
    
    return self;
}

#pragma mark - Data management

- (void)loadDetailDataWithHUD
{
    HUD = [[MBProgressHUD alloc] initWithView:self.navigationController.tabBarController.view];
	[self.navigationController.tabBarController.view addSubview:HUD];
	
	HUD.dimBackground = YES;
	
	// Regiser for HUD callbacks so we can remove it from the window at the right time
    HUD.delegate = self;
    HUD.labelText = @"Loading";
	
    [HUD showWhileExecuting:@selector(loadDetailData) onTarget:self withObject:nil animated:YES];
}

- (BOOL)isFresh
{
    return YES; // TODO will check current date against date in CoreData
}

#pragma mark - MBProgressHUDDelegate method

- (void)hudWasHidden:(MBProgressHUD *)hud
{
    // Remove HUD from screen when the HUD was hidded
    [HUD removeFromSuperview];
    [HUD release];
	HUD = nil;
}

#pragma mark - CPTPlotDataSource protocol

- (NSUInteger)numberOfRecordsForPlot:(CPTPlot *)plot
{
    return 51;
}

- (NSNumber *)numberForPlot:(CPTPlot *)plot 
                      field:(NSUInteger)fieldEnum 
                recordIndex:(NSUInteger)index
{
    double val = (index/5.0)-5;
    
    if(fieldEnum == CPTScatterPlotFieldX)
    { return [NSNumber numberWithDouble:val]; }
    else
    { 
        if(plot.identifier == @"X Squared Plot")
        { return [NSNumber numberWithDouble:val*val]; }
        else
        { return [NSNumber numberWithDouble:1/val]; }
    }
}

#pragma mark - View lifecycle

- (BOOL) shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)orientation {
    return YES;
}

@end
