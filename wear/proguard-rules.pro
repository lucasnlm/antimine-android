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

-keepattributes *Annotation*
-keepattributes Signature
-dontwarn com.squareup.**
-keep class com.squareup.** { *; }
-keep class com.squareup.moshi.** { *; }

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
-keepattributes Signature
-keep class com.google.android.gms.** { *; }
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

# Kryo
-dontwarn sun.reflect.**
-dontwarn java.beans.**
-dontwarn sun.nio.ch.**
-dontwarn sun.misc.**

# Firebase
-keep class com.google.android.gms.** { *; }
-keep class com.google.firebase.** { *; }

# LibGDX
-keep class com.badlogic.gdx.scenes.** { *; }

-dontwarn com.badlogic.gdx.backends.android.AndroidFragmentApplication

# Required if using Gdx-Controllers extension
-keep class com.badlogic.gdx.controllers.android.AndroidControllers

# Required if using Box2D extension
-keepclassmembers class com.badlogic.gdx.physics.box2d.World {
   boolean contactFilter(long, long);
   void    beginContact(long);
   void    endContact(long);
   void    preSolve(long, long);
   void    postSolve(long, long);
   boolean reportFixture(long);
   float   reportRayFixture(long, float, float, float, float, float);
}

-dontwarn javax.annotation.processing.AbstractProcessor
-dontwarn javax.annotation.processing.SupportedOptions
-dontwarn java.lang.invoke.StringConcatFactory
-dontwarn com.google.android.material.R$attr
-dontwarn dev.lucasnlm.antimine.i18n.R$string

