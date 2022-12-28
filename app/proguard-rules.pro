-keepattributes Exceptions, InnerClasses

-keep class com.crashlytics.** { *; }
-dontwarn com.crashlytics.**

-keep class dev.lucasnlm.antimine.common.level.database.*
-keep class dev.lucasnlm.antimine.common.level.database.** { *; }
-keep class dev.lucasnlm.antimine.common.level.database.converters.** { *; }
-keep class dev.lucasnlm.antimine.common.level.database.dao.** { *; }
-keep class androidx.room.** { *; }

-keepclassmembers @com.squareup.moshi.JsonClass class * extends java.lang.Enum {
    <fields>;
    **[] values();
}
