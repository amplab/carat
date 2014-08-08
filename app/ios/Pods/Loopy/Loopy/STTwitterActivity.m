//
//  STTwitterActivity.m
//  Loopy
//
//  Created by David Jedeikin on 10/17/13.
//  Copyright (c) 2013 ShareThis. All rights reserved.
//

#import "STTwitterActivity.h"
#import "STConstants.h"
#import <Social/Social.h>

@implementation STTwitterActivity

@synthesize shareItems;

- (NSString *)activityTitle {
    return @"Twitter";
}

- (NSString *)activityType {
    return SLServiceTypeTwitter;
}

- (UIImage *)activityImage {
    UIImage *image = nil;
    BOOL isIPhone = [[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPhone;
    BOOL isIOS7 = YES;
    
    if (floor(NSFoundationVersionNumber) <= NSFoundationVersionNumber_iOS_6_1) {
        isIOS7 = NO;
    }
    
    if((isIOS7 && isIPhone) || (!isIOS7 && !isIPhone)) {
        image = [UIImage imageNamed:@"TwitterLogo60x60.png"];
    }
    else if(!isIOS7 && isIPhone) {
        image = [UIImage imageNamed:@"TwitterLogo43x43.png"];
    }
    else if(isIOS7 && !isIPhone) {
        image = [UIImage imageNamed:@"TwitterLogo76x76.png"];
    }
    
    return image;
}

- (BOOL)canPerformWithActivityItems:(NSArray *)activityItems {
    return YES;
}

//Notification of intent to share
- (void)prepareWithActivityItems:(NSArray *)activityItems {
    self.shareItems = activityItems;
    [[NSNotificationCenter defaultCenter] postNotificationName:BeginShareNotification object:self];
}

@end
