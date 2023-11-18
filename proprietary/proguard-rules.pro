-dontwarn java.lang.invoke.StringConcatFactory
-dontwarn com.google.android.material.R$attr
-dontwarn dev.lucasnlm.antimine.i18n.R$string


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
