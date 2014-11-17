# Carat Project: Android and iOS Applications

For details, visit http://carat.cs.berkeley.edu/

## Build instructions for Android

1. Download Eclipse: http://www.eclipse.org/downloads/
2. Install Google's ADT: http://developer.android.com/sdk/installing/installing-adt.html
3. Make sure you have the latest Android SDKs and tools installed.
4. For building the Android app, first import the companion "Support" library into your IDE (we will use this project as a library project). If you use Eclipse, follow these stpes:

5. Select File > Import.
6. From the Android category, select "Existing Android Code Into Workspace" and click Next.
7. Browse to the root directory of Carat project, select the directory "support-appcompat", click OK, and then click finish to complete the import process. Now you should see the support library project in your project navigatation pane, in the left side.

8. Then, import the main Carat Android project. After that, you should add the above library project to the Carat project:
9. Right click on the main project (Carat) > properties > Android > in the library section (in the bottom), click Add > select the support library project > click OK.
10. Click the green play button with the Carat project selected to run on a device or an emulator.

## Build instructions for iOS

1. Install ruby gems: https://rubygems.org/pages/download
2. Install CocoaPods 0.33.1: http://rubygems.org/gems/cocoapods/versions/0.33.1
3. Make sure you have the latest Xcode.
4. Go to the app/ios folder in a terminal and run `pod install`.
5. Make sure the `SZIdentifierUtils.m ` and `.h` files still have `#define SHOULD_USE_IDFA 0`
6. Open the resulting Carat.xcodeworkspace in Xcode.
7. Build and run on an emulator or your device.

