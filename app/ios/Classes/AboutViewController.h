//
//  AboutViewController.h
//  Carat
//
//  Created by Adam Oliner on 11/29/11.
//  Copyright (c) 2011 UC Berkeley. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface AboutViewController : UIViewController <UIWebViewDelegate> {
    NSArray *aboutWebView;
}

@property (retain, nonatomic) IBOutletCollection(UIWebView) NSArray *aboutWebView;


@end
