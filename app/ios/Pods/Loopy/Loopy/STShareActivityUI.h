//
//  STShare.h
//  Loopy
//
//  Created by David Jedeikin on 10/23/13.
//  Copyright (c) 2013 ShareThis. All rights reserved.
//

#import "STAPIClient.h"
#import <Foundation/Foundation.h>
#import <Social/Social.h>

@interface STShareActivityUI : NSObject

@property (nonatomic, strong) UIViewController *parentController;
@property (nonatomic, strong) STAPIClient *apiClient;

- (id)initWithParent:(UIViewController *)parent apiClient:(STAPIClient *)client;
- (NSArray *)getDefaultActivities:(NSArray *)activityItems;
- (UIActivityViewController *)newActivityViewController:(NSArray *)shareItems withActivities:(NSArray *)activities;
- (SLComposeViewController *)newActivityShareController:(id)activityObj;
- (void)showActivityViewDialog:(UIActivityViewController *)activityController completion:(void (^)(void))completion;
- (void)handleShowActivityShare:(NSNotification *)notification;
- (void)handleShareComplete:(NSNotification *)notification;
@end
