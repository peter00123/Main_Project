# QRShare ProGuard rules

# Keep Gson model classes
-keepclassmembers class com.qrshare.network.** { *; }
-keepclassmembers class com.qrshare.sharing.** { *; }

# ZXing
-keep class com.google.zxing.** { *; }

# ML Kit
-keep class com.google.mlkit.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
