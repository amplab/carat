//
//  InstructionViewController.m
//  Carat
//
//  Created by Adam Oliner on 2/9/12.
//  Copyright (c) 2012 UC Berkeley. All rights reserved.
//

#import "InstructionViewController.h"
#import "Sampler.h"
#import "Utilities.h"

@implementation InstructionViewController

@synthesize theHTML = _theHTML;
@synthesize actionType = _actionType;

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil actionType:(ActionType)action
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
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
    // Releases the view if it doesn't have a superview.
    [super didReceiveMemoryWarning];
    
    // Release any cached data, images, etc that aren't in use.
    [theHTML release];
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

/*
// Implement loadView to create a view hierarchy programmatically, without using a nib.
- (void)loadView
{
}
*/


- (void)viewDidLoad
{
    [super viewDidLoad];
 
    self.navigationItem.title = @"Action Instructions";
    
    // TODO load the appropriate HTML into theHTML based on actionType
    switch (self.actionType) {
        case ActionTypeKillApp:
            [self setTheHTML:@"Unrecognized Action Type!"];
            break;
            
        case ActionTypeRestartApp:
            [self setTheHTML:@"Unrecognized Action Type!"];
            break;
            
        case ActionTypeUpgradeOS:
            [self setTheHTML:@"Unrecognized Action Type!"];
            break;
            
        case ActionTypeDimScreen:
            [self setTheHTML:@"Unrecognized Action Type!"];
            break;
            
        case ActionTypeSpreadTheWord:
            DLog(@"Should not be loading InstructionView when ActionType is SpreadTheWord!");
            [self setTheHTML:@"How did you get here?"];
            break;
            
        default:
            [self setTheHTML:@"Unrecognized Action Type!"];
            break;
    }
}

- (void)viewDidUnload
{
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

- (void)viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];
    
    [[Sampler instance] checkConnectivityAndSendStoredDataToServer];
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    // Return YES for supported orientations
    return YES;
}


@end
