# Project specific ProGuard rules

# Crashlytics 2.+
-keep class com.crashlytics.** { *; }
-keep class com.crashlytics.android.**
-keepattributes SourceFile, LineNumberTable, *Annotation*

# For Fabric to properly de-obfuscate your crash reports, you need to remove this line from your ProGuard config:
-printmapping mapping.txt

# support design
-dontwarn android.support.design.**
-keep class android.support.design.** { *; }
-keep interface android.support.design.** { *; }
-keep public class android.support.design.R$* { *; }

# support v7
-keep public class android.support.v7.widget.** { *; }
-keep public class android.support.v7.internal.widget.** { *; }
-keep public class android.support.v7.internal.view.menu.** { *; }

-keep public class * extends android.support.v4.view.ActionProvider {
    public <init>(android.content.Context);
}

# support v4
-keep class android.support.v4.** { *; }
-keep interface android.support.v4.** { *; }

# build config
-keep class com.example.BuildConfig { *; }

# firebase
-keep class com.firebase.** { *; }

# google gms
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# amplitude
-keep class com.google.android.gms.ads.** { *; }
-dontwarn okio.**

# recycler view
-keep public class * extends android.support.v7.widget.RecyclerView$LayoutManager {
    public <init>(...);
}