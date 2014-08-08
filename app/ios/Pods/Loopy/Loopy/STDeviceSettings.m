//
//  STDeviceSettings.m
//  Loopy
//
//  Created by David Jedeikin on 4/15/14.
//  Copyright (c) 2014 ShareThis. All rights reserved.
//

#import "STDeviceSettings.h"
#import "STIdentifierFactory.h"
#import "STReachability.h"
#import "STDevice.h"
#import "STApp.h"
#import "STGeo.h"

//set to 1 to use IDFA, 0 to use IDFV and set IDFA String to "UNAVAILABLE"
#define SHOULD_USE_IDFA 1

@implementation STDeviceSettings

NSString *const DEVICE_DATA_FILENAME = @"STDeviceData.plist";
NSString *const DEVICE_ID_KEY = @"DeviceID";

@synthesize locationManager;
@synthesize carrierName;
@synthesize osVersion;
@synthesize deviceModel;
@synthesize md5id;
@synthesize idfa;
@synthesize idfv;
@synthesize currentLocation;

- (id)initWithLocationsDisabled:(BOOL)locationServicesDisabled
                 identifierType:(STIdentifierType)identifierType {
    self = [super init];
    
    if(self) {
        //device information cached for sharing and other operations
        if(!locationServicesDisabled) {
            self.locationManager = [[CLLocationManager alloc] init];
            self.locationManager.desiredAccuracy = kCLLocationAccuracyHundredMeters;
            self.locationManager.delegate = self;
            [self.locationManager startUpdatingLocation];
        }
        CTTelephonyNetworkInfo *networkInfo = [[CTTelephonyNetworkInfo alloc] init];
        CTCarrier *carrier = [networkInfo subscriberCellularProvider];
        UIDevice *device = [UIDevice currentDevice];
        self.carrierName = [carrier carrierName] != nil ? [carrier carrierName] : @"none";
        self.deviceModel = machineName();
        self.osVersion = device.systemVersion;
        
        //identifier settings come from object built by factory
        STIdentifierFactory *identifierFactory = (STIdentifierFactory *)[STIdentifierFactory instance];
        STIdentifier *identifier = [identifierFactory identifierForKey:identifierType];
        self.idfv = identifier.idfv;
        self.idfa = identifier.idfa;
        self.md5id = identifier.md5id;
    }
    return self;
}

//required subset of endpoint calls
- (STDevice *)device {
    CLLocationCoordinate2D coordinate;
    STReachability *reachability = [STReachability reachabilityForInternetConnection];
    NetworkStatus netStatus = [reachability currentReachabilityStatus];
    NSString *wifiStatus = netStatus == ReachableViaWiFi ? @"on" : @"off";
    //set to UNAVAILABLE if IDFA isn't allowed
    NSString *idStr = self.idfa ? [self.idfa UUIDString] : @"UNAVAILABLE";
    NSString *idfvStr = [self.idfv UUIDString];
    
    STDevice *device = [[STDevice alloc] init];
    device.id = idStr;
    device.idv = idfvStr;
    device.model = self.deviceModel;
    device.os = @"ios";
    device.osv = self.osVersion;
    device.carrier = self.carrierName;
    device.wifi = wifiStatus;
    
    STGeo *geo = nil;
    if(self.currentLocation) {
        coordinate = self.currentLocation.coordinate;
    }
    //location management disabled; simply set to 0,0
    else {
        coordinate = CLLocationCoordinate2DMake(0.0, 0.0);
    }
    geo = [[STGeo alloc] init];
    geo.lat = [NSNumber numberWithDouble:coordinate.latitude];
    geo.lon = [NSNumber numberWithDouble:coordinate.longitude];
    device.geo = geo;
    
    return device;
}

//required subset of endpoint calls
- (STApp *)app {
    STApp *app = [[STApp alloc] init];
    NSBundle *bundle = [NSBundle mainBundle];
    NSDictionary *info = [bundle infoDictionary];
    NSString *appID = [info valueForKey:@"CFBundleIdentifier"];
    NSString *appName = [info valueForKey:@"CFBundleName"];
    NSString *appVersion = [info valueForKey:@"CFBundleVersion"];
    
    app.id = appID;
    app.name = appName;
    app.version = appVersion;
    
    return app;
}

#pragma mark - Location And Device Information

//location update
- (void)locationManager:(CLLocationManager *)manager didUpdateLocations:(NSArray *)locations {
    if(locations.lastObject) {
        self.currentLocation = (CLLocation *)locations.lastObject;
    }
}

//convenience method to return "real" device name
//per http://stackoverflow.com/questions/11197509/ios-iphone-get-device-model-and-make
NSString *machineName() {
    struct utsname systemInfo;
    uname(&systemInfo);
    
    return [NSString stringWithCString:systemInfo.machine
                              encoding:NSUTF8StringEncoding];
}

//convenience method to return MD5 String
//per http://www.makebetterthings.com/iphone/how-to-get-md5-and-sha1-in-objective-c-ios-sdk/
- (NSString *)md5FromString:(NSString *)input {
    const char *cStr = [input UTF8String];
    unsigned char digest[16];
    CC_MD5( cStr, strlen(cStr), digest ); // This is the md5 call
    
    NSMutableString *output = [NSMutableString stringWithCapacity:CC_MD5_DIGEST_LENGTH * 2];
    
    for(int i = 0; i < CC_MD5_DIGEST_LENGTH; i++) {
        [output appendFormat:@"%02x", digest[i]];
    }
    
    return output;
}

@end
