//
//  AboutViewController.m
//  Carat
//
//  Created by Adam Oliner on 11/29/11.
//  Copyright (c) 2011 UC Berkeley. All rights reserved.
//

#import "AboutViewController.h"

@implementation AboutViewController
@synthesize aboutWebView;
@synthesize theHTML = _theHTML;
@synthesize portraitView, landscapeView;

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
	self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
	if (self) {
        self.title = @"About";
        self.tabBarItem.image = [UIImage imageNamed:@"about"];
    }
    return self;
}

- (void)didReceiveMemoryWarning
{
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc that aren't in use.
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
    // Do any additional setup after loading the view from its nib.
    self.theHTML = @"<html> \
    <head> \
    </head> \
    <body> \
    <h3>Carat: collaborative detection of energy bugs</h3> \
    <p>Carat is a research project from the AMP Lab at UC Berkeley that aims to perform collaborative detection of energy bugs.<p> \
    <p><b><i>Bug</i></b> means an app that is using a lot of energy on a small number of devices, including yours. Restarting a buggy app may improve battery life.<p> \
    <p><b><i>Hog</i></b> means an app that is correlated with higher energy use, across many devices. Closing this app may improve battery life.<p> \
    <p>Carat collects and reports generic usage statistics about your phone that allow it to do its job; this process involves absolutely no personally identifying information. Period.<p> \
    <p>Carat is under development, so please check back often to see new data and new analyses. The more you use it, the better it will get. You can read more about the research on the <a href=\"http://carat.cs.bereley.edu\">Carat Project page</a>.<p> \
    <p>You can also <a href=\"mailto:oliner+carat@eecs.berkeley.edu\">contact us directly</a>.<p> \
    </body> \
    </html>";
    for (UIWebView *wv in self.aboutWebView) {
        [wv loadHTMLString:self.theHTML baseURL:nil];
    }
    
    [[UIDevice currentDevice] beginGeneratingDeviceOrientationNotifications];
	[[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(orientationChanged:) name:@"UIDeviceOrientationDidChangeNotification" object:nil];
}

- (void)viewDidUnload
{
    [aboutWebView release];
    [self setAboutWebView:nil];
    [theHTML release];
    [self setTheHTML:nil];
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

- (void) orientationChanged:(id)object
{  
	UIDeviceOrientation interfaceOrientation = [[object object] orientation];
	
    if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPad) {
        self.view = self.portraitView;
    } else if (interfaceOrientation == UIInterfaceOrientationPortrait || interfaceOrientation == UIInterfaceOrientationPortraitUpsideDown) 
	{
		self.view = self.portraitView;
	} 
	else 
	{
		self.view = self.landscapeView;
	}
}

- (BOOL) shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)orientation {
    return YES;
}

- (void)dealloc {
    [aboutWebView release];
    [theHTML release];
    [super dealloc];
}
@end
