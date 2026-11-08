# Proguard / R8 rules for Antigravity Android

# Keep JavascriptInterface methods accessible from WebView
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep JavaScript bridge class
-keep class com.google.antigravity.mobile.bridge.** { *; }

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep data models
-keep class com.google.antigravity.mobile.data.model.** { *; }

# WorkManager
-keep class androidx.work.impl.workers.** { *; }
-keep class com.google.antigravity.mobile.worker.** { *; }

# OkHttp rules
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
