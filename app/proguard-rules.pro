# Project specific ProGuard rules

-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Moshi
-keep class com.squareup.moshi.**
-keep class kotlin.** { *; }

