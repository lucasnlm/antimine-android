-keepattributes Exceptions, InnerClasses

-keep class com.crashlytics.** { *; }
-dontwarn com.crashlytics.**

-keep class dev.lucasnlm.antimine.common.level.database.*
-keep class dev.lucasnlm.antimine.common.level.database.** { *; }
-keep class dev.lucasnlm.antimine.common.level.database.converters.** { *; }
-keep class dev.lucasnlm.antimine.common.level.database.dao.** { *; }
-keep class androidx.room.** { *; }

-keep class * extends androidx.fragment.app.Fragment{}
-keepnames class * extends android.os.Parcelable
-keepnames class * extends java.io.Serializable

-keep class * implements android.os.Parcelable {
   public static final android.os.Parcelable$Creator *;
}

-keepclassmembers @com.squareup.moshi.JsonClass class * extends java.lang.Enum {
    <fields>;
    **[] values();
}

-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# For Google Play Services
-keep class io.grpc.** {*;}
-keep public class com.google.android.gms.ads.** {
    public *;
}

-keep public class com.google.ads.** {
    public *;
}


# Material
-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**
-dontnote com.google.android.material.**