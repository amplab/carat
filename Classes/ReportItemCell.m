//
//  ReportItemCell.m
//  Carat
//
//  Created by Adam Oliner on 12/1/11.
//  Copyright (c) 2011 UC Berkeley. All rights reserved.
//

#import "ReportItemCell.h"

@implementation ReportItemCell

@synthesize appName;
@synthesize appIcon;
@synthesize appScore;
@synthesize appIconURL;


- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
    if (self) {
        // Initialization code
    }
    return self;
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated
{
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

- (void) cache:(JMImageCache *)c didDownloadImage:(UIImage *)i forURL:(NSString *)url 
{
	//DLog(@"%s didDownloadImage for URL = %@", __PRETTY_FUNCTION__, url);
	if([url isEqualToString:appIconURL]) {
		self.appIcon.image = i;
		[self setNeedsLayout];
	}
}

@end
