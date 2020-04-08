# Project specific ProGuard rules

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

# recycler view
-keep public class * extends android.support.v7.widget.RecyclerView$LayoutManager {
    public <init>(...);
}