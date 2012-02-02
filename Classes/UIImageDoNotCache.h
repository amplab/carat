//
//  UIImage+UIImageNoCache.h
//  Carat
//
//  Created by Adam Oliner on 2/2/12.
//  Copyright (c) 2012 UC Berkeley. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface UIImage (DoNotCache)

+ (UIImage *)newImageNotCached:(NSString *)filename;

@end