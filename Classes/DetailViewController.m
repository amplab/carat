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

#pragma mark - Data management

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
    return 100;
}

- (NSNumber *)numberForPlot:(CPTPlot *)plot 
                      field:(NSUInteger)fieldEnum 
                recordIndex:(NSUInteger)index
{
    double val = (((double)index)/100.0);
    
    if(fieldEnum == CPTScatterPlotFieldX) {
        return [NSNumber numberWithDouble:val*10];
    } else { 
        if(plot.identifier == @"This Plot")
        { return [NSNumber numberWithDouble:val]; }
        else if (plot.identifier == @"That Plot")
        { return [NSNumber numberWithDouble:val+0.1]; }
        else
        { NSLog(@"Unknown plot identifier."); }
    }
}

#pragma mark - View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];
    [self setFirstAppearance:YES];
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
    [self hudWasHidden:HUD];
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

    // loads data while showing busy indicator
    if ([self firstAppearance]) {
        [self loadDetailDataWithHUD];
        [self setFirstAppearance:NO];
        [self.view setNeedsDisplay];
    }
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
