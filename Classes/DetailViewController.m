//
//  DetailViewController.m
//  Carat
//
//  Created by Adam Oliner on 12/14/11.
//  Copyright (c) 2011 UC Berkeley. All rights reserved.
//

#import "DetailViewController.h"

@implementation DetailViewController

@synthesize navTitle;
@synthesize detailData;

@synthesize detailGraphView = _hogDetailGraphView;
@synthesize appName = _appName;
@synthesize appIcon = _appIcon;
@synthesize appScore = _appScore;
@synthesize thisText = _thisText;
@synthesize thatText = _thatText;
@synthesize portraitView, landscapeView;

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // custom init
    }
    
    return self;
}

- (void)didReceiveMemoryWarning
{
    // TODO release icons and whatnot; add checks to ensure this is loaded before displaying
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc that aren't in use.
}

#pragma mark - CPTPlotDataSource protocol

- (NSUInteger)numberOfRecordsForPlot:(CPTPlot *)plot
{
    if (self.detailData != nil) {
        return [[self.detailData xVals] count];
    } else return 0;
}

- (NSNumber *)numberForPlot:(CPTPlot *)plot 
                      field:(NSUInteger)fieldEnum 
                recordIndex:(NSUInteger)index
{
    // TODO
//    if (self.detailData != nil) {
//        if(fieldEnum == CPTScatterPlotFieldX) {
//            if(plot.identifier == @"This Plot")
//            { return [self.detailData xVals]; }
//            else if (plot.identifier == @"That Plot")
//            { return [NSNumber numberWithDouble:val+0.1]; }
//            else
//            { NSLog(@"Unknown plot identifier."); }
//        } else {
//            if(plot.identifier == @"This Plot")
//            { return [NSNumber numberWithDouble:val]; }
//            else if (plot.identifier == @"That Plot")
//            { return [NSNumber numberWithDouble:val+0.1]; }
//            else
//            { NSLog(@"Unknown plot identifier."); }
//    }
}

#pragma mark - View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
    
    self.navigationItem.title = self.navTitle;
    
    // graph setup
    for (CPTGraphHostingView *hostingView in self.detailGraphView) {
        float maxRate = 10.0f; // TODO get actual max rate
        
        CPTXYGraph *graph = [[CPTXYGraph alloc] initWithFrame:CGRectZero];
        hostingView.hostedGraph = graph;
        
        graph.paddingLeft = 0;
        graph.paddingTop = 0;
        graph.paddingRight = 0;
        graph.paddingBottom = 0;
        
        CPTXYPlotSpace *plotSpace = (CPTXYPlotSpace *)graph.defaultPlotSpace;
        plotSpace.xRange = [CPTPlotRange plotRangeWithLocation:CPTDecimalFromFloat(-1)
                                                        length:CPTDecimalFromFloat(maxRate+1)];
        plotSpace.yRange = [CPTPlotRange plotRangeWithLocation:CPTDecimalFromFloat(-0.1)
                                                        length:CPTDecimalFromFloat(1.15)];

        CPTXYAxisSet *axisSet = (CPTXYAxisSet *)graph.axisSet;
        
        CPTMutableLineStyle *axisLineStyle = [CPTMutableLineStyle lineStyle];
        
        CPTMutableLineStyle *thisLineStyle = [CPTMutableLineStyle lineStyle];
        thisLineStyle.lineColor = [CPTColor colorWithComponentRed:0 green:0.4 blue:0 alpha:1];
        thisLineStyle.lineWidth = 2.0f;
        
        CPTMutableLineStyle *thatLineStyle = [CPTMutableLineStyle lineStyle];
        thatLineStyle.lineColor = [CPTColor colorWithComponentRed:1 green:0.4 blue:0 alpha:1];
        thatLineStyle.lineWidth = 2.0f;
        
        CPTMutableTextStyle *axisTextStyle = [CPTMutableTextStyle textStyle];
        axisTextStyle.color = [CPTColor blackColor];
        
        // X-Axis
        axisSet.xAxis.majorIntervalLength = CPTDecimalFromFloat(maxRate/10.0f);
        axisSet.xAxis.minorTicksPerInterval = 2;
        axisSet.xAxis.majorTickLineStyle = axisLineStyle;
        axisSet.xAxis.minorTickLineStyle = axisLineStyle;
        axisSet.xAxis.axisLineStyle = axisLineStyle;
        axisSet.xAxis.minorTickLength = 5.0f;
        axisSet.xAxis.majorTickLength = 7.0f;
        axisSet.xAxis.labelOffset = 1.0f;
        axisSet.xAxis.titleTextStyle = axisTextStyle;
        axisSet.xAxis.title = @"Energy Rate";
        NSString *xAxisTitleLocation = [NSString stringWithFormat:@"%f", (maxRate/2)];
        axisSet.xAxis.titleLocation = [[NSDecimalNumber decimalNumberWithString:xAxisTitleLocation] decimalValue];;
        NSNumberFormatter* formatter = [[[NSNumberFormatter alloc] init] autorelease];
        [formatter setNumberStyle:NSNumberFormatterDecimalStyle];
        axisSet.xAxis.labelFormatter = formatter;
        
        // Y-Axis
        axisSet.yAxis.majorIntervalLength = [[NSDecimalNumber decimalNumberWithString:@"1"] decimalValue];
        axisSet.yAxis.minorTicksPerInterval = 10;
        axisSet.yAxis.majorTickLineStyle = axisLineStyle;
        axisSet.yAxis.minorTickLineStyle = axisLineStyle;
        axisSet.yAxis.axisLineStyle = axisLineStyle;
        axisSet.yAxis.minorTickLength = 5.0f;
        axisSet.yAxis.majorTickLength = 7.0f;
        axisSet.yAxis.labelOffset = 1.0f;
        
        CPTScatterPlot *thisPlot = [[[CPTScatterPlot alloc] init] autorelease];
        thisPlot.identifier = @"This Plot";
        thisPlot.dataLineStyle = thisLineStyle;
        thisPlot.dataSource = self;
        [graph addPlot:thisPlot];
        
        CPTScatterPlot *thatPlot = [[[CPTScatterPlot alloc] init] autorelease];
        thatPlot.identifier = @"That Plot";
        thatPlot.dataLineStyle = thatLineStyle;
        thatPlot.dataSource = self;
        [graph addPlot:thatPlot];

    }
    
    [[UIDevice currentDevice] beginGeneratingDeviceOrientationNotifications];
	[[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(orientationChanged:) name:UIDeviceOrientationDidChangeNotification object:nil];
    [[NSNotificationCenter defaultCenter] postNotificationName:UIDeviceOrientationDidChangeNotification object:nil];
    
    if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPhone) {
        if ([[UIApplication sharedApplication] statusBarOrientation] == UIInterfaceOrientationPortrait ||
            [[UIApplication sharedApplication] statusBarOrientation] == UIInterfaceOrientationPortraitUpsideDown)
        {
            self.view = self.portraitView;
        } else {
            self.view = self.landscapeView;
        }
    }
}

- (BOOL) shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)orientation {
    return YES;
}

- (void)viewDidUnload
{
    [thisText release];
    [self setThisText:nil];
    [thatText release];
    [self setThatText:nil];
    [appName release];
    [self setAppName:nil];
    [detailGraphView release];
    [self setDetailGraphView:nil];
    [appIcon release];
    [self setAppIcon:nil];
    [appScore release];
    [self setAppScore:nil];
    [portraitView release];
    [self setPortraitView:nil];
    [landscapeView release];
    [self setLandscapeView:nil];
    
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    
    if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPhone) {
        if ([[UIApplication sharedApplication] statusBarOrientation] == UIInterfaceOrientationPortrait ||
            [[UIApplication sharedApplication] statusBarOrientation] == UIInterfaceOrientationPortraitUpsideDown)
        {
            self.view = self.portraitView;
        } else {
            self.view = self.landscapeView;
        }
    }
}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
}

- (void) orientationChanged:(id)object
{  
	UIDeviceOrientation interfaceOrientation = [[object object] orientation];
	
    if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad) {
        self.view = self.landscapeView;
    } else if (interfaceOrientation == UIInterfaceOrientationPortrait || interfaceOrientation == UIInterfaceOrientationPortraitUpsideDown) 
	{
		self.view = self.portraitView;
	} 
	else 
	{
		self.view = self.landscapeView;
	}
}

- (void)dealloc {
    [thisText release];
    [thatText release];
    [appName release];
    [detailGraphView release];
    [appIcon release];
    [appScore release];
    [portraitView release];
    [landscapeView release];
    [super dealloc];
}

@end
