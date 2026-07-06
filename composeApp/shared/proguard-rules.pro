# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
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

-dontwarn org.slf4j.impl.StaticLoggerBinder

# Credential Manager Google auth uses Parcelable/reflection-heavy boundaries.
# Keep these surfaces stable so ID-token parsing and provider dispatch survive R8.
-keep class com.google.android.libraries.identity.googleid.** { *; }
-keep class androidx.credentials.** { *; }
-keep class androidx.credentials.playservices.** { *; }
-keep class com.google.android.gms.auth.api.identity.** { *; }
-keep class com.google.android.gms.common.api.** { *; }
-keep class com.google.android.gms.common.internal.safeparcel.** { *; }
-keepclassmembers class * extends com.google.android.gms.common.internal.safeparcel.AbstractSafeParcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}

-if class androidx.credentials.CredentialManager
-keep class androidx.credentials.playservices.** {
  *;
}
-dontobfuscate

# Fix for missing class kotlinx.datetime.Instant$Companion referenced from RevenueCat
-keep class kotlinx.datetime.** { *; }
-dontwarn kotlinx.datetime.**
