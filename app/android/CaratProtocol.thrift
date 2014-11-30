namespace java edu.berkeley.cs.amplab.carat.thrift

// 
// The registration message.
//
struct Registration {
	1: required string uuId;		// The UUID generated using CFUUIDCreate(). on Android, ANDROID_ID.
	// ANDROID_ID may change if device is factory reset or a different mod is installed. 
	// On phones, the IMEI could be used, but that is sensitive information.
	// Build.Serial could be used, but that is not available before Android 2.3 on phones.
	// For tablets, Build.Serial should always be available.
	// WLAN MAC address could be used, but that may be changed by rooting and using macchanger etc.
	2: optional double timestamp;		// Timestamp for this registration message.
	3: optional string platformId;		// Platform ID, eg. iPhone3,1 or Galaxy Nexus
	4: optional string systemVersion;	// iOS version, eg. 3.1.3 or 4.0.2
	5: optional string systemDistribution; // CyanogenMod, MIUI, etc.
	6: optional string kernelVersion; // 2.6.32-cyanogenmod-... etc.
}

//
// Running processes.
//
struct ProcessInfo {
	1: optional i32 pId;
	2: optional string pName;
	3: optional string applicationLabel; // Human readable application name
	4: optional bool isSystemApp; // If the app is a system app or update to a system app.
	5: optional string importance; // foreground, visible, background, service, empty
	6: optional string versionName; // Version of app, human-readable.
	7: optional i32 versionCode; // Version of app, android version code.
	8: optional list<string> appSignatures; // Signatures of the app from PackageInfo.signatures
	9: optional string installationPkg; // Package that installed this one, e.g. com.android.vending, com.google.play, or com.amazon.venezia.
}

//
// The process info list.
//
typedef list<ProcessInfo> ProcessInfoList

//
// Network details on Android.
//
struct NetworkDetails {
	// Android-only: network settings
	1: optional string networkType;	// Currently wifi,mobile,wimax,or unknown
	2: optional string mobileNetworkType; // GPRS, EDGE, UMTS, ...
	
	// Android-only: Mobile data settings
	3: optional string mobileDataStatus; // connecting, connected, disconnected, suspended
	4: optional string mobileDataActivity; // none,in,out,inout,dormant
	5: optional bool roamingEnabled; // true if currently roaming in a foreign mobile network.
	
	// Android-only: Wifi settings
	6: optional string wifiStatus;	 // disabled, disabling, enabled, enabling, unknown
	7: optional i32 wifiSignalStrength;	// as given by getRssi() on Android
	8: optional i32 wifiLinkSpeed;	    // link speed in Mbps
}

//
// Battery details on Android.
//
struct BatteryDetails {
	// Android-only: battery status information
	1: optional string batteryCharger; // currently ac, usb, or unplugged
	2: optional string batteryHealth; // currently Unknown,Unspecified failure,Dead,Cold,Overheat,Over voltage, or Good
	3: optional double batteryVoltage; // voltage in Volts.
	4: optional double batteryTemperature; // temperature in C.
	5: optional string batteryTechnology; // battery technology.
	6: optional double batteryCapacity; // Capacity in mAh from Android Power Profile.
}

//
// CPU details on Android.
//
struct CpuStatus {
	// Android-only: CPU
    1: optional double cpuUsage; // cpu usage fraction (0-1)
	2: optional double uptime; // uptime in seconds
    // These may change
//	3: optional double cpuTime; // CPU usage in seconds since reboot
//	4: optional double idleTime; // idle time in seconds since reboot

}

//
// Call info on Android.
//
struct CallInfo {
    1: optional double incomingCallTime; // incoming call time sum since boot
    2: optional double outgoingCallTime; // outgoing call time sum since boot
    3: optional double nonCallTime; // non-call time sum since boot
    4: optional string callStatus; // idle,offhook, or ringing
}

//
// Feature list, used to retrieve hog or bug report.
//
struct Feature {
	1: optional string key;
	2: optional string value;
}

//
// Sample
//
struct Sample {
	1: required string uuId;		// The ID for this device.
	2: optional double timestamp;		// Timestamp for this sample.
	3: optional ProcessInfoList piList;	// List of processes running.
	4: optional string batteryState;	// State of the battery. ie. charging, discharging, etc.
	5: optional double batteryLevel;	// Level of the battery.
	6: optional i32 memoryWired;		// Total wired memory.
	7: optional i32 memoryActive;		// Total active memory.
	8: optional i32 memoryInactive;		// Total inactive memory.
	9: optional i32 memoryFree;		// Total free memory.
	10: optional i32 memoryUser;		// Total user memory.
	11: optional string triggeredBy;	// Trigger reason.
	12: optional string networkStatus;	// Reachability status.
	13: optional double distanceTraveled;	// If locationchange triggers, then this will have a value.
	// Android-only: brightness
	14: optional i32 screenBrightness;	// Brightness value, 0-255
	// Android-only: network status
	15: optional NetworkDetails networkDetails; // Network status struct, with info on the active network, mobile,  and wifi
	16: optional BatteryDetails batteryDetails; // Battery status struct, with battery health, charger, voltage, temperature, etc.
	17: optional CpuStatus cpuStatus; // CPU information, such as cpu usage percentage.
	//Android only: enabled location providers (such as GPS)
	18: optional list<string> locationProviders;	// Enabled location providers
	19: optional CallInfo callInfo; // call ratios and information.
	20: optional i32 screenOn; // Android Only: Screen on == 1, off == 0
	21: optional string timeZone; // Device timezone abbreviation
	22: optional i32 unknownSources; // Android Only: Unknown source app installation on == 1, off == 0
	23: optional i32 developerMode; // Android Only: Developer mode on == 1, off == 0
	24: optional list<Feature> extra; // Extra features for extensibility.
}

//
// Fields for the detailed screen report. 
//
struct DetailScreenReport {
	1: optional double score;
	2: optional list<double> xVals;
	3: optional list<double> yVals;
	4: optional double expectedValue;
	// 95% confidence error value
	5: optional double error;
	6: optional double errorWithout;
	// Number of samples used for expectedValue and error
	7: optional double samples;
	8: optional double samplesWithout;
}

//
// Main report.
//
struct Reports {
	1: optional double jScore;
	2: optional DetailScreenReport os;
	3: optional DetailScreenReport osWithout;
	4: optional DetailScreenReport model;
	5: optional DetailScreenReport modelWithout;
	6: optional DetailScreenReport similarApps;
	7: optional DetailScreenReport similarAppsWithout;
	8: optional double changeSinceLastWeek;
	9: optional double changeSinceLastWeekPercentage;
	10: optional DetailScreenReport jScoreWith;
	11: optional DetailScreenReport jScoreWithout;
}

//
// Struct with info on hog or bug with percentages 
//
struct HogsBugs {
	1: optional string appName;		// Application name.
	2: optional double wDistance;		// Wasserstein distance.
	3: optional list<double> xVals;		// This is the x-axis values for PDF in the detailed view for this app.
	4: optional list<double> yVals;		// This is the y-axis values for PDF in the detailed view for this app.
	5: optional list<double> xValsWithout;	
	6: optional list<double> yValsWithout;
	7: optional double expectedValue;
	8: optional double expectedValueWithout; 
	9: optional double error;
	10: optional double errorWithout;
	// Number of samples used for expectedValue and error
	11: optional double samples;
    12: optional double samplesWithout;
	// Android-only
	13: optional string appLabel; // Human-readable application label on Android
	14: optional string appPriority; // Priority of app. See ProcessList
}

typedef list<HogsBugs> HogsBugsList

//
// Hog report
//
struct HogBugReport {
	1: required string uuId;		// ID for the device for which this report is intended.
	2: optional HogsBugsList hbList;	// List of hogs or bugs.
}


typedef list<Feature> FeatureList

//
// The CARAT service. 
//
service CaratService {
	oneway void registerMe(1:Registration registration);
	bool uploadSample(1:Sample sample);
	Reports getReports(1: string uuId, 2: FeatureList features);
	HogBugReport getHogOrBugReport(1: string uuId, 2: FeatureList features)
	HogBugReport getQuickHogsAndMaybeRegister(1:Registration registration, 2:list<string> processList)
}

