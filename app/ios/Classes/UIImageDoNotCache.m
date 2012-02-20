//
//  UIImage+UIImageNoCache.m
//  Carat
//
//  Created by Adam Oliner on 2/2/12.
//  Copyright (c) 2012 UC Berkeley. All rights reserved.
//

#import "UIImageDoNotCache.h"

@implementation UIImage (DoNotCache)

+ (UIImage *)newImageNotCached:(NSString *)filename {
    NSString *imageFile = [[NSString alloc] initWithFormat:@"%@/%@", [[NSBundle mainBundle] resourcePath], filename];
    UIImage *image = [[UIImage alloc] initWithContentsOfFile:imageFile];
    [imageFile release];
    return image;
}

@end