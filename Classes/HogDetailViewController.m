//
//  HogDetailViewController.m
//  Carat
//
//  Created by Adam Oliner on 12/1/11.
//  Copyright (c) 2011 UC Berkeley. All rights reserved.
//

#import "HogDetailViewController.h"
#import "CorePlot-CocoaTouch.h"

@implementation HogDetailViewController

@synthesize detailGraphView = _hogDetailGraphView;
@synthesize wassersteinDistance = _wassersteinDistance;
@synthesize appName = _appName;
@synthesize appIcon = _appIcon;
@synthesize appScore = _appScore;
@synthesize numSamplesWith = _numSamplesWith;
@synthesize numSamplesWithout = _numSamplesWithout;
@synthesize portraitView, landscapeView;

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        self.navTitle = @"Hog Detail";
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

#pragma mark - View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];
    [self setFirstAppearance:YES];
    // Do any additional setup after loading the view from its nib.
    
    self.navigationItem.title = self.navTitle;
    
    // graph setup
    for (CPTGraphHostingView *hostingView in self.detailGraphView) {
        CPTXYGraph *graph = [[CPTXYGraph alloc] initWithFrame:CGRectZero];
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
    
    [[UIDevice currentDevice] beginGeneratingDeviceOrientationNotifications];
	[[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(orientationChanged:) name:UIDeviceOrientationDidChangeNotification object:nil];
    [[NSNotificationCenter defaultCenter] postNotificationName:UIDeviceOrientationDidChangeNotification object:nil];
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

- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
    
    if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPhone) {
        if ([[UIApplication sharedApplication] statusBarOrientation] == UIInterfaceOrientationPortrait ||
            [[UIApplication sharedApplication] statusBarOrientation] == UIInterfaceOrientationPortraitUpsideDown)
        {
            self.view = self.portraitView;
        } else {
            self.view = self.landscapeView;
        }
    }
    
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
    [numSamplesWith release];
    [numSamplesWithout release];
    [wassersteinDistance release];
    [appName release];
    [detailGraphView release];
    [appIcon release];
    [appScore release];
    [portraitView release];
    [landscapeView release];
    [super dealloc];
}

@end
