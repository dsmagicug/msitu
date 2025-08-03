# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# React Native
-keep class com.facebook.react.** { *; }
-keep class com.facebook.hermes.** { *; }
-keep class com.facebook.jni.** { *; }

# Don't warn about missing classes - these are referenced but not needed at runtime
-dontwarn javax.measure.**
-dontwarn javax.measure.spi.**
-dontwarn tech.units.indriya.**
-dontwarn org.slf4j.**
-dontwarn org.slf4j.impl.**

# Keep react-native-maps classes
-keep class com.rnmaps.maps.** { *; }
-keep class com.airbnb.android.react.maps.** { *; }
-keep class com.google.android.gms.maps.** { *; }

# Keep coordinate validation and map-related classes
-keep class com.msitu.** { *; }
-keep class * implements com.facebook.react.bridge.JavaScriptModule { *; }
-keep class * implements com.facebook.react.bridge.NativeModule { *; }

# Keep React Native Reanimated classes
-keep class com.swmansion.reanimated.** { *; }
-keep class com.facebook.react.bridge.ReadableNativeMap { *; }
-keep class com.facebook.react.bridge.WritableNativeMap { *; }

# Keep coordinate validation functions
-keepclassmembers class * {
    @com.facebook.react.bridge.ReactMethod *;
}

# Keep coordinate validation and utility functions
# Note: Wildcard patterns are not valid in ProGuard rules

# Keep React Native bridge classes
-keep class com.facebook.react.bridge.** { *; }
-keep class com.facebook.react.uimanager.** { *; }

# Keep native methods
-keepclassmembers class * {
    native <methods>;
}

# Keep React Native modules
-keep class com.facebook.react.bridge.** { *; }
-keep class com.facebook.react.uimanager.** { *; }
-keep class com.facebook.react.views.** { *; }

# Keep SQLite
-keep class org.sqlite.** { *; }
-keep class org.sqlite.database.** { *; }

# Keep Bluetooth
-keep class com.facebook.react.bridge.** { *; }
-keep class com.facebook.react.modules.bluetooth.** { *; }

# Keep Maps
-keep class com.facebook.react.views.map.** { *; }

# Remove unused code
-dontwarn org.sqlite.**
-dontwarn com.facebook.react.**
-dontwarn com.facebook.hermes.**

# Optimize
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification

# Remove logging in release
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
}
