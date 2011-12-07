//
//  BugDetailViewController.m
//  Carat
//
//  Created by Adam Oliner on 12/1/11.
//  Copyright (c) 2011 UC Berkeley. All rights reserved.
//

#import "BugDetailViewController.h"
#import "CorePlot-CocoaTouch.h"

@implementation BugDetailViewController

@synthesize bugDetailGraphView;
@synthesize wassersteinDistance;
@synthesize appName;
@synthesize appIcon;
@synthesize appScore;
@synthesize numSamplesWith;
@synthesize numSamplesWithout;

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
    // Do any additional setup after loading the view from its nib.
    
    self.navigationItem.title = @"Bug Detail";
    
    // graph setup
    graph = [[CPTXYGraph alloc] initWithFrame:CGRectZero];
    CPTGraphHostingView *hostingView = (CPTGraphHostingView *)self.bugDetailGraphView;
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
    [bugDetailGraphView release];
    [self setBugDetailGraphView:nil];
    [appIcon release];
    [self setAppIcon:nil];
    [appScore release];
    [self setAppScore:nil];
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
    [numSamplesWith release];
    [numSamplesWithout release];
    [wassersteinDistance release];
    [appName release];
    [bugDetailGraphView release];
    [appIcon release];
    [appScore release];
    [super dealloc];
}

@end
