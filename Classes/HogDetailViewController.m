//
//  HogDetailViewController.m
//  Carat
//
//  Created by Adam Oliner on 12/1/11.
//  Copyright (c) 2011 UC Berkeley. All rights reserved.
//

#import "HogDetailViewController.h"
#import "CorePlot-CocoaTouch.h"
#import "MBProgressHUD.h"

@implementation HogDetailViewController

@synthesize hogDetailGraphView = _hogDetailGraphView;
@synthesize wassersteinDistance = _wassersteinDistance;
@synthesize appName = _appName;
@synthesize appIcon = _appIcon;
@synthesize appScore = _appScore;
@synthesize numSamplesWith = _numSamplesWith;
@synthesize numSamplesWithout = _numSamplesWithout;
@synthesize firstAppearance = _firstAppearance;

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
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

- (void)loadDetailData
{
    // TODO finish
    // display waiting indicator
    sleep(1);
    // check local cache, use if fresh
    
    // attempt to refresh cache over network
    // [(HogDetailViewController *)vc setWasUpdated:NO/YES];
    
    // if stale data found, display brief hud error and show
    
    // finally, if all else fails, show without the graph
    
    
    if ([self isFresh]) {
        // The checkmark image is based on the work by http://www.pixelpressicons.com, http://creativecommons.org/licenses/by/2.5/ca/
        HUD.customView = [[[UIImageView alloc] initWithImage:[UIImage imageNamed:@"37x-Checkmark.png"]] autorelease];
        HUD.mode = MBProgressHUDModeCustomView;
        HUD.labelText = @"Completed";
        sleep(1);
    } else {
        HUD.customView = [[[UIImageView alloc] initWithImage:[UIImage imageNamed:@"37x-X.png"]] autorelease];
        HUD.mode = MBProgressHUDModeCustomView;
        HUD.labelText = @"Failed";
        HUD.detailsLabelText = @"(showing stale data)";
        sleep(2);
    }
}

- (BOOL)isFresh
{
    return NO; // TODO will check current date against date in CoreData
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

- (void)viewDidLoad
{
    [super viewDidLoad];
    [self setFirstAppearance:YES];
    // Do any additional setup after loading the view from its nib.
    
    self.navigationItem.title = @"Hog Detail";
    
    // graph setup
    graph = [[CPTXYGraph alloc] initWithFrame:CGRectZero];
    CPTGraphHostingView *hostingView = (CPTGraphHostingView *)self.hogDetailGraphView;
    hostingView.hostedGraph = graph;
    graph.paddingLeft = 0;
    graph.paddingTop = 0;
    graph.paddingRight = 0;
    graph.paddingBottom = 0;
    
    CPTXYPlotSpace *plotSpace = (CPTXYPlotSpace *)graph.defaultPlotSpace;
    plotSpace.xRange = [CPTPlotRange plotRangeWithLocation:CPTDecimalFromFloat(-6)
                                                    length:CPTDecimalFromFloat(12)];
    plotSpace.yRange = [CPTPlotRange plotRangeWithLocation:CPTDecimalFromFloat(-5)
                                                    length:CPTDecimalFromFloat(30)];
    
    CPTXYAxisSet *axisSet = (CPTXYAxisSet *)graph.axisSet;
    
    CPTLineStyle *lineStyle = [CPTLineStyle lineStyle];
//    lineStyle.lineColor = [CPTColor blackColor];
//    lineStyle.lineWidth = 2.0f;
    
    axisSet.xAxis.majorIntervalLength = [[NSDecimalNumber decimalNumberWithString:@"5"] decimalValue];
    axisSet.xAxis.minorTicksPerInterval = 4;
    axisSet.xAxis.majorTickLineStyle = lineStyle;
    axisSet.xAxis.minorTickLineStyle = lineStyle;
    axisSet.xAxis.axisLineStyle = lineStyle;
    axisSet.xAxis.minorTickLength = 5.0f;
    axisSet.xAxis.majorTickLength = 7.0f;
    
    axisSet.yAxis.majorIntervalLength = [[NSDecimalNumber decimalNumberWithString:@"5"] decimalValue];
    axisSet.yAxis.minorTicksPerInterval = 4;
    axisSet.yAxis.majorTickLineStyle = lineStyle;
    axisSet.yAxis.minorTickLineStyle = lineStyle;
    axisSet.yAxis.axisLineStyle = lineStyle;
    axisSet.yAxis.minorTickLength = 5.0f;
    axisSet.yAxis.majorTickLength = 7.0f;
    
    CPTScatterPlot *xSquaredPlot = [[[CPTScatterPlot alloc] init] autorelease];
    xSquaredPlot.identifier = @"X Squared Plot";
//    xSquaredPlot.dataLineStyle.lineWidth = 1.0f;
//    xSquaredPlot.dataLineStyle.lineColor = [CPTColor redColor];
    xSquaredPlot.dataSource = self;
    [graph addPlot:xSquaredPlot];
    
    CPTPlotSymbol *greenCirclePlotSymbol = [CPTPlotSymbol ellipsePlotSymbol];
    greenCirclePlotSymbol.fill = [CPTFill fillWithColor:[CPTColor greenColor]];
    greenCirclePlotSymbol.size = CGSizeMake(2.0, 2.0);
    xSquaredPlot.plotSymbol = greenCirclePlotSymbol;  
    
    CPTScatterPlot *xInversePlot = [[[CPTScatterPlot alloc] init] autorelease];
    xInversePlot.identifier = @"X Inverse Plot";
//    xInversePlot.dataLineStyle.lineWidth = 1.0f;
//    xInversePlot.dataLineStyle.lineColor = [CPTColor blueColor];
    xInversePlot.dataSource = self;
    [graph addPlot:xInversePlot];

}

- (void)viewDidUnload
{
    [numSamplesWithout release];
    [self setNumSamplesWithout:nil];
    [numSamplesWith release];
    [self setNumSamplesWith:nil];
    [wassersteinDistance release];
    [self setWassersteinDistance:nil];
    [appName release];
    [self setAppName:nil];
    [hogDetailGraphView release];
    [self setHogDetailGraphView:nil];
    [appIcon release];
    [self setAppIcon:nil];
    [appScore release];
    [self setAppScore:nil];
    [self hudWasHidden:HUD];
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    // loads data while showing busy indicator
    if ([self firstAppearance]) {
        [self loadDetailDataWithHUD];
        [self setFirstAppearance:NO];
        [self.view setNeedsDisplay];
    }
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    // Return YES for supported orientations
    return (interfaceOrientation == UIInterfaceOrientationPortrait);
}

- (void)dealloc {
    [numSamplesWith release];
    [numSamplesWithout release];
    [wassersteinDistance release];
    [appName release];
    [hogDetailGraphView release];
    [appIcon release];
    [appScore release];
    [super dealloc];
}

@end
