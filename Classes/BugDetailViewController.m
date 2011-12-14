//
//  BugDetailViewController.m
//  Carat
//
//  Created by Adam Oliner on 12/1/11.
//  Copyright (c) 2011 UC Berkeley. All rights reserved.
//

#import "BugDetailViewController.h"
#import "CorePlot-CocoaTouch.h"

@implementation BugDetailViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        self.navTitle = @"Bug Detail";
    }
    
    return self;
}

@end
