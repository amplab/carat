//
//  ContainerViewController.m
//  Carat
//
//  Created by Muhammad Haris on 25/12/14.
//  Copyright (c) 2014 UC Berkeley. All rights reserved.
//

#import "ContainerViewController.h"
#import "CaratConstants.h"
#import "CoreDataManager.h"
#import "Utilities.h"

@interface ContainerViewController ()

@end

@implementation ContainerViewController

- (void)viewDidLoad {
	
    [super viewDidLoad];
	[[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(samplesSentCountUpdated:) name:kSamplesSentCountUpdateNotification object:nil];

	[[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(pageTitleUpdated:) name:kPageTitleUpdateNotification object:nil];

	[[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(updatedXAgo:) name:kUpdatedXAgoUpdateNotification object:nil];

	self.samplesSentLabel.text = [NSString stringWithFormat:@"Samples Sent: %li", (long)[[CoreDataManager instance] getSampleSent]];
    // Do any additional setup after loading the view from its nib.
}

-(void) pageTitleUpdated:(NSNotification*) notification{
	NSDictionary* userInfo = notification.userInfo;
	self.pageTitle.text = userInfo[kPageTitle];
}

-(void) updatedXAgo:(NSNotification*) notification{
	NSDictionary* userInfo = notification.userInfo;
	self.updatedXAgo.text = userInfo[kUpdatedXAgo];
}

-(void) samplesSentCountUpdated:(NSNotification*) notification{
	NSDictionary* userInfo = notification.userInfo;
	NSInteger samplesSentCount = userInfo[kSamplesSent];
	NSLog(@"Sample count updated, current count: %i", samplesSentCount);
    self.samplesSentLabel.text = [NSString stringWithFormat:@"Samples Sent: %i", [[CoreDataManager instance] getSampleSent]];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

-(void) viewDidAppear:(BOOL)animated
{
	[super viewDidAppear:animated];
	[self.view bringSubviewToFront:self.topBar];
}

-(void) dealloc{

	[[NSNotificationCenter defaultCenter] removeObserver:self];
	[super dealloc];
}
/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end
