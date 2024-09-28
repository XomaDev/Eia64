# Keep your main package classes to ensure essential code is not removed
-keep class space.themelon.eia64.** { *; }

# Keep code related to reflection if used
-keepattributes Signature,InnerClasses,EnclosingMethod

# Do not warn about missing Kotlin classes
-dontwarn kotlin.**
