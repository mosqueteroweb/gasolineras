# ProGuard and R8 rules for Gass (Gasolineras España)

# Keep line numbers and source file names for crash reports
-keepattributes SourceFile,LineNumberTable

# Keep annotations and reflection signatures
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod

# Retrofit & OkHttp
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**
-dontwarn okhttp3.**

# Gson & Data Transfer Objects (DTOs)
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.gasolineras.app.data.api.** { *; }
-keep class com.gasolineras.app.domain.model.** { *; }

# Keep Enum values() and valueOf() methods
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# OpenStreetMap / OSMDroid (uses reflection for map overlays and views)
-keep class org.osmdroid.** { *; }
-dontwarn org.osmdroid.**

# Google Play Services Location
-keep class com.google.android.gms.location.** { *; }
-dontwarn com.google.android.gms.location.**
