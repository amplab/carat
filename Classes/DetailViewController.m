//
//  DetailViewController.m
//  Carat
//
//  Created by Adam Oliner on 12/14/11.
//  Copyright (c) 2011 UC Berkeley. All rights reserved.
//

#import "DetailViewController.h"
#import "Utilities.h"

@implementation DetailViewController

@synthesize navTitle;

@synthesize xVals, yVals, xValsWithout, yValsWithout;

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
    if (self.xVals != nil) {
        if(plot.identifier == @"This Plot")
        { return [self.xVals count]; }
        else if (plot.identifier == @"That Plot")
        { return [self.xValsWithout count]; }
        else return 0;
    } else return 0;
}

- (NSNumber *)numberForPlot:(CPTPlot *)plot 
                      field:(NSUInteger)fieldEnum 
                recordIndex:(NSUInteger)index
{
    if (self.xVals != nil) {
        if(fieldEnum == CPTScatterPlotFieldX) {
            if(plot.identifier == @"This Plot")
            { return [self.xVals objectAtIndex:index]; }
            else if (plot.identifier == @"That Plot")
            { return [self.xValsWithout objectAtIndex:index]; }
            else
            {
                DLog(@"Unknown plot identifier.");
                return [NSNumber numberWithInteger:index];
            }
        } else {
            if(plot.identifier == @"This Plot")
            { return [self.yVals objectAtIndex:index]; }
            else if (plot.identifier == @"That Plot")
            { return [self.yValsWithout objectAtIndex:index]; }
            else
            {
                DLog(@"Unknown plot identifier.");
                return [NSNumber numberWithInteger:index];
            }
        }
    } else {
        DLog(@"Plot data is nil.");
        return [NSNumber numberWithInteger:index];
    }
}

#pragma mark - View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
    
    self.navigationItem.title = self.navTitle;
    
    // graph setup
    for (CPTGraphHostingView *hostingView in self.detailGraphView) {
        float maxRate;
        float maxProb;
        if (self.xVals != nil && [self.xVals count] > 0 && self.xValsWithout != nil && [self.xValsWithout count] > 0) {
            NSNumber *m1 = [self.xVals valueForKeyPath:@"@max.floatValue"];
            NSNumber *m2 = [self.xValsWithout valueForKeyPath:@"@max.floatValue"];
            maxRate = MAX([m1 floatValue], [m2 floatValue]);
        } else {
            DLog(@"xVals(Without) was nil or zero-length.");
            maxRate = 10.0f;
        }
        if (self.yVals != nil && [self.yVals count] > 0 && self.yValsWithout != nil && [self.yValsWithout count] > 0) {
            NSNumber *m1 = [self.yVals valueForKeyPath:@"@max.floatValue"];
            NSNumber *m2 = [self.yValsWithout valueForKeyPath:@"@max.floatValue"];
            maxProb = MAX([m1 floatValue], [m2 floatValue]);
        } else {
            DLog(@"yVals(Without) was nil or zero-length.");
            maxProb = 1.0f;
        }
        
        CPTXYGraph *graph = [[CPTXYGraph alloc] initWithFrame:CGRectZero];
        hostingView.hostedGraph = graph;
        
        graph.paddingLeft = 0;
        graph.paddingTop = 0;
        graph.paddingRight = 0;
        graph.paddingBottom = 0;
        
        CPTXYPlotSpace *plotSpace = (CPTXYPlotSpace *)graph.defaultPlotSpace;
        plotSpace.xRange = [CPTPlotRange plotRangeWithLocation:CPTDecimalFromFloat(-(maxRate*0.1f))
                                                        length:CPTDecimalFromFloat(maxRate+(maxRate*0.1f))];
        plotSpace.yRange = [CPTPlotRange plotRangeWithLocation:CPTDecimalFromFloat(-(maxProb*0.1f))
                                                        length:CPTDecimalFromFloat(maxProb+(maxProb*0.15f))];
        
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
        axisTextStyle.fontSize = 9.0;
        
        // X-Axis
        axisSet.xAxis.majorIntervalLength = CPTDecimalFromFloat(maxRate/3.0f);
        axisSet.xAxis.minorTicksPerInterval = 1;
        axisSet.xAxis.majorTickLineStyle = axisLineStyle;
        axisSet.xAxis.minorTickLineStyle = axisLineStyle;
        axisSet.xAxis.axisLineStyle = axisLineStyle;
        axisSet.xAxis.minorTickLength = 4.0f;
        axisSet.xAxis.majorTickLength = 6.0f;
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
        axisSet.yAxis.minorTickLength = 4.0f;
        axisSet.yAxis.majorTickLength = 6.0f;
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
    [self setXVals:nil];
    [self setYVals:nil];
    [self setXValsWithout:nil];
    [self setYValsWithout:nil];
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
    
    [[Sampler instance] checkConnectivityAndSendStoredDataToServer];
}

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
}

- (void) orientationChanged:(id)object
{  
    if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad) {
        self.view = self.portraitView;
    } else if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPhone) {
        if ([[UIApplication sharedApplication] statusBarOrientation] == UIInterfaceOrientationPortrait ||
            [[UIApplication sharedApplication] statusBarOrientation] == UIInterfaceOrientationPortraitUpsideDown)
        {
            self.view = self.portraitView;
        } else {
            self.view = self.landscapeView;
        }
    }
}

- (void)dealloc {
    [navTitle release];
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
