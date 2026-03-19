# =============================================================================
# FILE: app/proguard-rules.pro
# =============================================================================
# INDEX OF CONTENTS:
#   1. Hilt / Dagger rules
#   2. Kotlin Coroutines rules
#   3. Parcelable rules
#   4. Navigation Component rules
#   5. General Android rules
#
# OBJECTIVE:
#   ProGuard/R8 rules to prevent obfuscation of classes that are
#   referenced by name at runtime (reflection, DI, serialisation).
#   Without these rules, release builds would crash due to missing classes.
# =============================================================================

# ── Hilt / Dagger ─────────────────────────────────────────────────────────────
-keep class dagger.hilt.** { *; }
-keep @dagger.hilt.android.HiltAndroidApp class * { *; }
-keep @dagger.hilt.android.AndroidEntryPoint class * { *; }
-keepclasseswithmembers class * {
    @dagger.hilt.android.lifecycle.HiltViewModel <init>(...);
}

# ── Kotlin Coroutines ─────────────────────────────────────────────────────────
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** { volatile <fields>; }

# ── Parcelable ────────────────────────────────────────────────────────────────
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}
-keepclassmembers class * implements android.os.Parcelable {
    static ** CREATOR;
}

# ── Navigation Component ──────────────────────────────────────────────────────
-keepnames class androidx.navigation.fragment.NavHostFragment

# ── Data models (Parcelize) ───────────────────────────────────────────────────
-keep class com.nearbyshare.data.models.** { *; }

# ── ViewBinding ───────────────────────────────────────────────────────────────
-keep class * implements androidx.viewbinding.ViewBinding {
    public static ** inflate(...);
    public static ** bind(android.view.View);
}

# ── General Android ───────────────────────────────────────────────────────────
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
