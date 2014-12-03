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
    id callbackDelegate;
    SEL callbackSelector;
}

@property (retain, nonatomic) IBOutletCollection(UIWebView) NSArray *consentWebView;

@property (nonatomic, retain) id callbackDelegate;

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil callbackTo:(id)delegate withSelector:(SEL)selector;

@end
