# Carat Project: Android and iOS Applications

For details, visit http://carat.cs.berkeley.edu/

## Build instructions for Android

1. Download Eclipse: http://www.eclipse.org/downloads/
2. Install Google's ADT: http://developer.android.com/sdk/installing/installing-adt.html
3. Make sure you have the latest Android SDKs and tools installed.
4. Import Carat project into your IDE.
5. Import the following library projects (dependecies) into your IDE: 

* **Support library**: official library by Google for providing new Android features (such as action bar, navigation drawer, etc) to older APIs (API > 7).

    > The Android Support Library package is a set of code libraries that provide backward-compatible versions of Android framework APIs as well as features that are only available through the library APIs.
 
   * Directory path: app/libs/Android/support-appcompat

* **MPAndroidChart**: a comprehensive and customizable native library for creating charts and graphs
  * Directory path: app/libs/Android/MPAndroidChart

 
* **Android-Support-PreferenceFragment**: a backport of the PreferenceFragment class. Provides the preference fragment feature for API < 11 (Android < 3.0). A FragmentPreference is the recommended way for implementing a fragment for adjusting settings or user preferences. See the comments in CaratSettingsFragment.java.
  * Directory path: app/libs/Android/android-support-v4-preferencefragment

 
* **Switch Widget Backport**: provides the switch widget (toggle button) for Android < 3.0
  * Directory path: app/libs/Android/android-switch-backport


If you use Eclipse, follow these stpes to import a library project:

1. Select File > *Import*.
2. From the *Android* category, select *Existing Android Code Into Workspace* and click Next.
3. Select the library project's directory, click OK, and then click finish to complete the import process. Now you should see the imported library project in your project navigatation pane, in the left side.

Eclipse may complain about not being able to find the dependecies for Carat project, this is a bug in Eclipse (not fixed even in Eclipse Mars). The solution is to go to the project properties dialog of Carat project, go to the *Android* section, remove all of the library project dependencies, and re-add them.


## Build instructions for iOS

1. Install ruby gems: https://rubygems.org/pages/download
2. Install CocoaPods 0.33.1: http://rubygems.org/gems/cocoapods/versions/0.33.1
3. Make sure you have the latest Xcode.
4. Go to the app/ios folder in a terminal and run `pod install`.
5. Make sure the `SZIdentifierUtils.m ` and `.h` files still have `#define SHOULD_USE_IDFA 0`
6. Open the resulting Carat.xcodeworkspace in Xcode.
7. Build and run on an emulator or your device.
