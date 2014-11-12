//
//  InstructionViewController.m
//  Carat
//
//  Created by Adam Oliner on 2/9/12.
//  Copyright (c) 2012 UC Berkeley. All rights reserved.
//

#import "InstructionViewController.h"
#import "CoreDataManager.h"
#import "Utilities.h"

@implementation InstructionViewController

@synthesize actionType = _actionType;
@synthesize webView = _webView;

- (id)initWithNibName:(NSString *)nibNameOrNil actionType:(ActionType)action
{
    self = [super initWithNibName:nibNameOrNil bundle:nil];
    if (self) {
        [self setActionType:action];
    }
    return self;
}

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
    DLog(@"Memory warning.");
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
}

#pragma mark - UIWebView

- (BOOL)webView:(UIWebView *)inWeb shouldStartLoadWithRequest:(NSURLRequest *)inRequest navigationType:(UIWebViewNavigationType)inType {
    if ( inType == UIWebViewNavigationTypeLinkClicked ) {
        [[UIApplication sharedApplication] openURL:[inRequest URL]];
        return NO;
    }
    
    return YES;
}

- (void)webView:(UIWebView *)wv didFailLoadWithError:(NSError *)error {
    // Ignore NSURLErrorDomain error -999.
    if (error.code == NSURLErrorCancelled) return;
    
    // Ignore "Fame Load Interrupted" errors. Seen after app store links.
    if (error.code == 102 && [error.domain isEqual:@"WebKitErrorDomain"]) return;
    
    // Normal error handling…
}

#pragma mark - View lifecycle

- (void)viewDidLoad
{
    [super viewDidLoad];
 
    self.navigationItem.title = @"Action Instructions";
    
    switch (self.actionType) {
        case ActionTypeKillApp:
            DLog(@"Loading Kill App instructions");
            [self.webView loadRequest:[NSURLRequest requestWithURL:[NSURL URLWithString:@"killapp.html" relativeToURL:[NSURL fileURLWithPath:[[NSBundle mainBundle] bundlePath]]]]];
            break;
            
        case ActionTypeRestartApp:
            DLog(@"Loading Restart App instructions");
            [self.webView loadRequest:[NSURLRequest requestWithURL:[NSURL URLWithString:@"killapp.html" relativeToURL:[NSURL fileURLWithPath:[[NSBundle mainBundle] bundlePath]]]]];
            break;
            
        case ActionTypeUpgradeOS:
            DLog(@"Loading Upgrade OS instructions");
            [self.webView loadRequest:[NSURLRequest requestWithURL:[NSURL URLWithString:@"upgradeos.html" relativeToURL:[NSURL fileURLWithPath:[[NSBundle mainBundle] bundlePath]]]]];
            break;
            
        case ActionTypeActiveBatteryLifeInfo:
            DLog(@"Loading Active Battery Life info");
            self.navigationItem.title = @"Active Battery Life Info";
            [self.webView loadRequest:[NSURLRequest requestWithURL:[NSURL URLWithString:@"activebatterylife.html" relativeToURL:[NSURL fileURLWithPath:[[NSBundle mainBundle] bundlePath]]]]];
            break;
            
        case ActionTypeJScoreInfo:
            DLog(@"Loading J-Score info");
            self.navigationItem.title = @"J-Score Info";
            [self.webView loadRequest:[NSURLRequest requestWithURL:[NSURL URLWithString:@"jscoreinfo.html" relativeToURL:[NSURL fileURLWithPath:[[NSBundle mainBundle] bundlePath]]]]];
            break;
            
        case ActionTypeMemoryInfo:
            DLog(@"Loading Memory info");
            self.navigationItem.title = @"Memory Info";
            [self.webView loadRequest:[NSURLRequest requestWithURL:[NSURL URLWithString:@"memoryinfo.html" relativeToURL:[NSURL fileURLWithPath:[[NSBundle mainBundle] bundlePath]]]]];
            break;

        case ActionTypeDetailInfo:
            DLog(@"Loading Detail info");
            self.navigationItem.title = @"Distribution Info";
            [self.webView loadRequest:[NSURLRequest requestWithURL:[NSURL URLWithString:@"detailinfo.html" relativeToURL:[NSURL fileURLWithPath:[[NSBundle mainBundle] bundlePath]]]]];
            break;
            
        case ActionTypeDimScreen:
            DLog(@"These instructions not yet implemented.");
            [webView loadRequest:[NSURLRequest requestWithURL:[NSURL fileURLWithPath:[[NSBundle mainBundle] pathForResource:@"about" ofType:@"html"] isDirectory:NO]]];
            break;
            
        case ActionTypeSpreadTheWord:
            DLog(@"Should not be loading InstructionView when ActionType is SpreadTheWord!");
            [webView loadRequest:[NSURLRequest requestWithURL:[NSURL fileURLWithPath:[[NSBundle mainBundle] pathForResource:@"about" ofType:@"html"] isDirectory:NO]]];
            break;

        case ActionTypeCollectData:
            DLog(@"Loading Data Collection info");
            self.navigationItem.title = @"Data Collection Info";
            [self.webView loadRequest:[NSURLRequest requestWithURL:[NSURL URLWithString:@"collectdata.html" relativeToURL:[NSURL fileURLWithPath:[[NSBundle mainBundle] bundlePath]]]]];
            break;
            
        default:
            DLog(@"Unrecognized Action Type!");
            [webView loadRequest:[NSURLRequest requestWithURL:[NSURL fileURLWithPath:[[NSBundle mainBundle] pathForResource:@"about" ofType:@"html"] isDirectory:NO]]];
            break;
    }
    
    // iOS 7+ fix for tabbar overlapping bottom of view
    if ([self respondsToSelector:@selector(setEdgesForExtendedLayout:)]) {
        [self setEdgesForExtendedLayout:UIRectEdgeNone];
        self.extendedLayoutIncludesOpaqueBars = NO;
        self.automaticallyAdjustsScrollViewInsets = NO;
    }
}

- (void)viewDidUnload
{
    [webView release];
    [self setWebView:nil];
    
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    
    [[CoreDataManager instance] checkConnectivityAndSendStoredDataToServer];
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    // Return YES for supported orientations
    return YES;
}

- (void)dealloc {
    [webView release];
    [super dealloc];
}

@end
