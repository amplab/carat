#### For building the Android app, (in adition to the main project) import the following library projects into your IDE:

* **Support library**: official library by Google for providing new Android features (such as action bar, navigation drawer, etc) to older APIs (API > 7).
  * Directory path: app/support-appcompat

> The Android Support Library package is a set of code libraries that provide backward-compatible versions of Android framework APIs as well as features that are only available through the library APIs.
 

* **Holograph**: a seamless native library for creating charts and graphs
  * Directory path: app/libs/HoloGraphLibrary

 
* **Android-Support-PreferenceFragment**: a backport of the PreferenceFragment class. Provides the preference fragment feature for API < 11 (Android < 3.0). A FragmentPreference is the recommended way for implementing a fragment for adjusting settings or user preferences. See the comments in CaratSettingsFragment.java.
  * Directory path: app/libs/android-support-v4-preferencefragment

 
* **Switch Widget Backport**: provides the switch widget (toggle button) for Android < 3.0
  * Directory path: app/libs/android-switch-backport


If you use Eclipse, follow these stpes to import a library project:

1. Select File > *Import*.
2. From the *Android* category, select *Existing Android Code Into Workspace* and click Next.
3. Select the library project's directory, click OK, and then click finish to complete the import process. Now you should see the imported library project in your project navigatation pane, in the left side.

Eclipse may complain about not being able to find the dependecies for Carat project, this is a bug in Eclipse (not fixed even in Eclipse Mars). The solution is to go to the project properties dialog of Carat project, go to the *Android* section, remove all of the library project dependencies, and re-add them.


For more details, visit http://carat.cs.berkeley.edu/