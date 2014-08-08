//
//  NSMutableURLRequest+Parameters.m
//
//  Created by Jon Crosby on 10/19/07.
//  Copyright 2007 Kaboomerang LLC. All rights reserved.
//
//  Permission is hereby granted, free of charge, to any person obtaining a copy
//  of this software and associated documentation files (the "Software"), to deal
//  in the Software without restriction, including without limitation the rights
//  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
//  copies of the Software, and to permit persons to whom the Software is
//  furnished to do so, subject to the following conditions:
//
//  The above copyright notice and this permission notice shall be included in
//  all copies or substantial portions of the Software.
//
//  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
//  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
//  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
//  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
//  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
//  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
//  THE SOFTWARE.


#import "NSMutableURLRequest+Parameters.h"
#import <objc/runtime.h>

static NSString *OAParametersKey = @"OAParametersKey";

@interface NSString (UUID)
+ (NSString*)stringWithUUID;
@end

@implementation NSString (UUID)

+ (NSString*)stringWithUUID {
    CFUUIDRef	uuidObj = CFUUIDCreate(nil);
    NSString	*uuidString = (NSString*)CFUUIDCreateString(nil, uuidObj);
    CFRelease(uuidObj);
    
    return [uuidString autorelease];
}

@end

@implementation NSMutableURLRequest (OAParameterAdditions)

- (NSArray *)OAParameters
{
    return objc_getAssociatedObject(self, OAParametersKey);
}

- (void)setOAParameters:(NSArray *)parameters {
    [self setOAParameters:parameters multipart:NO];
}

- (void)setOAParameters:(NSArray *)parameters multipart:(BOOL)multipart
{
    if (multipart) {
        
        NSString *boundary = [NSString stringWithUUID];
        NSString *contentType = [NSString stringWithFormat:@"multipart/form-data; boundary=%@", boundary];
        [self setValue:contentType forHTTPHeaderField:@"Content-Type"];
        
        NSMutableData *postData = [NSMutableData data];
        for (OARequestParameter *requestParameter in parameters) {
            NSMutableDictionary *subHeaders = [requestParameter.multipartHeaders copy];
            if (subHeaders == nil) {
                subHeaders = [[NSMutableDictionary alloc] init];
            }
            
            NSMutableString *contentDisposition = [subHeaders objectForKey:@"Content-Disposition"];
            if (contentDisposition == nil) {
                contentDisposition = [NSMutableString stringWithFormat:@"form-data; name=\"%@\"", requestParameter.name];
                if (requestParameter.multipartFilename != nil) {
                    [contentDisposition appendFormat:@"; filename=\"%@\"", requestParameter.multipartFilename];
                }
                [subHeaders setObject:contentDisposition forKey:@"Content-Disposition"];
            }
            
            if ([subHeaders objectForKey:@"Content-Type"] == nil && ([requestParameter.multipartFilename length] > 0 || [requestParameter.value isKindOfClass:[NSData class]])) {
                [subHeaders setObject:@"application/octet-stream" forKey:@"Content-Type"];
            }
            
            [postData appendData:[[NSString stringWithFormat:@"--%@\r\n", boundary] dataUsingEncoding:NSUTF8StringEncoding]];
            
            [subHeaders enumerateKeysAndObjectsUsingBlock:^(id key, id obj, BOOL *stop) {
                [postData appendData:[[NSString stringWithFormat:@"%@: %@\r\n", key, obj] dataUsingEncoding:NSUTF8StringEncoding]];
            }];
            [postData appendData:[@"\r\n" dataUsingEncoding:NSUTF8StringEncoding]];
            
            NSData *data;
            if ([requestParameter.value isKindOfClass:[NSData class]]) {
                data = requestParameter.value;
            } else {
                data = [[NSString stringWithFormat:@"%@\r\n", requestParameter.value] dataUsingEncoding:NSUTF8StringEncoding];
            }
            [postData appendData:data];
        }
        [postData appendData:[[NSString stringWithFormat:@"--%@--\r\n", boundary] dataUsingEncoding:NSUTF8StringEncoding]];
        
        [self setHTTPBody:postData];
        
    } else {
        
        NSMutableString *encodedParameterPairs = [NSMutableString stringWithCapacity:256];
        
        int position = 1;
        for (OARequestParameter *requestParameter in parameters)
        {
            [encodedParameterPairs appendString:[requestParameter URLEncodedNameValuePair]];
            if (position < [parameters count])
                [encodedParameterPairs appendString:@"&"];
            
            position++;
        }
        
        if ([[self HTTPMethod] isEqualToString:@"GET"] || [[self HTTPMethod] isEqualToString:@"DELETE"]) {
            [self setURL:[NSURL URLWithString:[NSString stringWithFormat:@"%@?%@", [[self URL] URLStringWithoutQuery], encodedParameterPairs]]];
        } else {
            // POST, PUT
            NSData *postData = [encodedParameterPairs dataUsingEncoding:NSASCIIStringEncoding allowLossyConversion:YES];
            [self setHTTPBody:postData];
            [self setValue:[NSString stringWithFormat:@"%d", [postData length]] forHTTPHeaderField:@"Content-Length"];
            [self setValue:@"application/x-www-form-urlencoded" forHTTPHeaderField:@"Content-Type"];
        }

    }
    
    objc_setAssociatedObject(self, OAParametersKey, parameters, OBJC_ASSOCIATION_RETAIN_NONATOMIC);
}

@end
