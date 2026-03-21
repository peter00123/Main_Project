# proguard-rules.pro
# ProGuard rules for Atezhare release builds.

# Keep Retrofit and OkHttp models — see model/Models.kt and network/ApiService.kt
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }
-keep class okhttp3.** { *; }
-keep class com.atezhare.model.** { *; }

# Keep Gson serialization — used by GsonConverterFactory in network/RetrofitClient.kt
-keep class com.google.gson.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep ZXing — used in ui/send/SendActivity and utils/QrUtils.kt
-keep class com.google.zxing.** { *; }
-keep class com.journeyapps.barcodescanner.** { *; }

# Keep CameraX — used in ui/send/SendActivity
-keep class androidx.camera.** { *; }

# Keep Parcelable — used in model/LocalFile
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}
