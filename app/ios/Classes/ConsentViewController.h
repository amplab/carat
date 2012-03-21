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
}

@property (retain, nonatomic) IBOutletCollection(UIWebView) NSArray *consentWebView;
@property (nonatomic, retain) IBOutlet UIView *portraitView;
@property (nonatomic, retain) IBOutlet UIView *landscapeView;

@end
