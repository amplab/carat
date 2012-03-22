//
//  ConsentViewController.h
//  Carat
//
//  Created by Adam Oliner on 3/21/12.
//  Copyright (c) 2012 UC Berkeley. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface ConsentViewController : UIViewController <UIWebViewDelegate> {
    NSArray *consentWebView;
    IBOutlet UIView *portraitView;
	IBOutlet UIView *landscapeView;
    id callbackDelegate;
    SEL callbackSelector;
}

@property (retain, nonatomic) IBOutletCollection(UIWebView) NSArray *consentWebView;
@property (nonatomic, retain) IBOutlet UIView *portraitView;
@property (nonatomic, retain) IBOutlet UIView *landscapeView;

@property (nonatomic, retain) id callbackDelegate;

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil callbackTo:(id)delegate withSelector:(SEL)selector;

@end
