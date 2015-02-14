//
//  SettingsViewController.m
//  Carat
//
//  Created by Muhammad Haris on 06/02/15.
//  Copyright (c) 2015 UC Berkeley. All rights reserved.
//

#import "SettingsViewController.h"
#import "AboutViewController.h"
#import "CaratConstants.h"
#import <Socialize/Socialize.h>
#import "Utilities.h"
#import "CoreDataManager.h"
#import "UIDeviceHardware.h"
#import "Flurry.h"

typedef NS_ENUM(NSUInteger, SettingsCellID) {
	kSettingsCellWifiSwitch = 0,
	kSettingsCellFeeback = 1,
	kSettingsCellAbout = 2
};

@interface SettingsViewController ()<UITableViewDataSource, UITableViewDelegate>
	@property (nonatomic, retain) IBOutlet UITableView* tableView;
@end

@implementation SettingsViewController

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
	self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
	if (self) {
		self.title = @"Settings";
		self.tabBarItem.image = [UIImage imageNamed:@"settings"];
	}
	return self;
}
- (void)viewDidLoad {
    [super viewDidLoad];

    // Do any additional setup after loading the view from its nib.
}

-(void) viewWillAppear:(BOOL)animated{

	[self.navigationController setNavigationBarHidden:YES animated:YES];
	[super viewWillAppear:YES];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
	return 3;
}

- (NSString *)tableView:(UITableView *)tableView titleForHeaderInSection:(NSInteger)section
{
	return self.title;
}

- (UIView *)tableView:(UITableView *)tableView viewForHeaderInSection:(NSInteger)section
{
	UIView *hView = [[[UIView alloc] initWithFrame: CGRectZero] autorelease];
	hView.backgroundColor = [UIColor clearColor];

	UILabel *hLabel=[[[UILabel alloc] initWithFrame: CGRectMake(10, 30, tableView.bounds.size.width, 20)] autorelease];

	hLabel.backgroundColor = [UIColor clearColor];
	hLabel.shadowColor = [UIColor whiteColor];
	hLabel.shadowOffset = CGSizeMake(0.5,1);
	hLabel.textColor = [UIColor blackColor];
	hLabel.font = [UIFont boldSystemFontOfSize:15];
	hLabel.text = self.title;

	[hView addSubview:hLabel];

	return hView;
}

-(CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath{
	return 60.0f;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {

	static NSString *MyIdentifier = @"SettingsItemCell";
	UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:MyIdentifier];
	if (cell == nil) {
		cell = [[UITableViewCell alloc] initWithStyle:UITableViewCellStyleDefault  reuseIdentifier:MyIdentifier];
	}
	cell.textLabel.textColor = [tableView separatorColor];
	switch (indexPath.row) {
  case kSettingsCellWifiSwitch:
			cell.textLabel.text = @"Use Wifi Only";
			//add a switch
			UISwitch *wifiSwitch = [[[UISwitch alloc] initWithFrame:CGRectZero] autorelease];
			wifiSwitch.onTintColor = [tableView separatorColor];
			cell.selectionStyle = UITableViewCellSelectionStyleNone;
			cell.accessoryView = wifiSwitch;
			[wifiSwitch setOn:isUsingWifiOnly animated:NO];
			[wifiSwitch addTarget:self action:@selector(wifiSwitchToggled:) forControlEvents:UIControlEventValueChanged];

			break;
  case kSettingsCellFeeback:
			cell.textLabel.text = @"Feedback";
			break;
  case kSettingsCellAbout:
			cell.textLabel.text = @"About";
			break;
  default:
			break;
	}

	return cell;
}

// loads the selected detail view
- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
	switch (indexPath.row) {
  case kSettingsCellFeeback:
			[self _reportFeedback];
			break;
  case kSettingsCellAbout:
			[self _presentAboutViewController];
			break;
  default:
			break;
	}

}
- (void) wifiSwitchToggled:(id)sender {
	UISwitch* switchControl = sender;
	BOOL useWifiOnly = switchControl.on ? YES: NO;
	[[NSUserDefaults standardUserDefaults] setBool:useWifiOnly forKey:kUseWifiOnly];
	NSLog( @"The switch is %@", switchControl.on ? @"ON" : @"OFF" );
}

-(void) _presentAboutViewController{
	AboutViewController *aboutView = [[[AboutViewController alloc] initWithNibName:@"AboutView" bundle:nil] autorelease];
	[self.navigationController pushViewController:aboutView animated:YES];
}

-(void) _reportFeedback{
	id<SZEntity> entity = [SZEntity entityWithKey:@"http://carat.cs.berkeley.edu" name:@"Carat"];

	SZShareOptions *options = [SZShareUtils userShareOptions];
	options.willShowEmailComposerBlock = ^(SZEmailShareData *emailData) {
		emailData.subject = @"Battery Diagnosis with Carat";
		emailData.recepients = [NSArray arrayWithObject:@"Carat Team <carat@cs.helsinki.fi>"];
		//        NSString *appURL = [emailData.propagationInfo objectForKey:@"http://bit.ly/xurpWS"];
		//        NSString *entityURL = [emailData.propagationInfo objectForKey:@"entity_url"];
		//        id<SZEntity> entity = emailData.share.entity;
		NSDictionary *memoryInfo = [Utilities getMemoryInfo];

		NSString* memoryUsed = @"Not available";
		NSString* memoryActive = @"Not available";

		if (memoryInfo) {
			float frac_used = [memoryInfo[kMemoryUsed] floatValue];
			float frac_active = [memoryInfo[kMemoryActive] floatValue];
			memoryUsed = [NSString stringWithFormat:@"%.02f%%",frac_used*100];
			memoryActive = [NSString stringWithFormat:@"%.02f%%",frac_active*100];
		}
		float Jscore = (MIN( MAX([[CoreDataManager instance] getJScore], -1.0), 1.0)*100);
		Jscore = ceil(14.5);
		NSString *JscoreStr = @"N/A";
		if(Jscore > 0)
			JscoreStr = [NSString stringWithFormat:@"%.0f", Jscore];

		// Device info
		UIDeviceHardware *h =[[[UIDeviceHardware alloc] init] autorelease];

		NSString *messageBody = [NSString stringWithFormat:
								 @"Carat ID: %s\n JScore: %@\n OS Version: %@\n Device Model: %@\n Memory Used: %@\n Memory Active: %@", [[[Globals instance] getUUID] UTF8String], JscoreStr, [UIDevice currentDevice].systemVersion,[h platformString], memoryUsed, memoryActive];

		emailData.messageBody = messageBody;
	};

	[SZShareUtils shareViaEmailWithViewController:self options:options entity:entity success:^(id<SocializeShare> share) {
		DLog(@"success reporting feedback");
		[self.tableView reloadData];
	} failure:^(NSError *error) {
		[self.tableView reloadData];
		DLog(@"failed reporting feedback");
	}];
	[Flurry logEvent:@"reportFeedback"];
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
