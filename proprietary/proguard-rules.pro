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

# The native PGS library wraps the Java PGS SDK using reflection.
-dontobfuscate
-keeppackagenames

# Needed for callbacks.
-keepclasseswithmembernames,includedescriptorclasses class * {
    native <methods>;
}

# Needed for helper libraries.
-keep class com.google.example.games.juihelper.** {
  public protected *;
}
-keep class com.sample.helper.** {
  public protected *;
}

# Needed for GoogleApiClient and auth stuff.
-keep class com.google.android.gms.common.api.** {
  public protected *;
}

# Keep all of the "nearby" library, which is needed by the native PGS library
# at runtime (though deprecated).
-keep class com.google.android.gms.nearby.** {
  public protected *;
}

# Keep all of the public PGS APIs.
-keep class com.google.android.gms.games.** {
  public protected *;
}
