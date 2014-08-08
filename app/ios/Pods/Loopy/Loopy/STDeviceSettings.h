//
//  STDeviceSettings.h
//  Loopy
//
//  Created by David Jedeikin on 4/15/14.
//  Copyright (c) 2014 ShareThis. All rights reserved.
//

#import "STDevice.h"
#import "STApp.h"
#import "STIdentifier.h"
#import <Foundation/Foundation.h>
#import <CoreLocation/CoreLocation.h>
#import <CoreTelephony/CTCarrier.h>
#import <CoreTelephony/CTTelephonyNetworkInfo.h>
#import <AdSupport/ASIdentifierManager.h>
#import <sys/utsname.h>

@interface STDeviceSettings : NSObject<CLLocationManagerDelegate>

extern NSString *const DEVICE_DATA_FILENAME;
extern NSString *const DEVICE_ID_KEY;

@property (nonatomic, strong) NSString *md5id;
@property (nonatomic, strong) NSUUID *idfa;
@property (nonatomic, strong) NSUUID *idfv;
@property (nonatomic, strong) CLLocation *currentLocation;
@property (nonatomic, strong) CLLocationManager *locationManager;
@property (nonatomic, strong) NSString *carrierName;
@property (nonatomic, strong) NSString *osVersion;
@property (nonatomic, strong) NSString *deviceModel;

- (id)initWithLocationsDisabled:(BOOL)locationServicesDisabled identifierType:(STIdentifierType)type;
- (STDevice *)device;
- (STApp *)app;
- (NSString *)md5FromString:(NSString *)input;

@end
