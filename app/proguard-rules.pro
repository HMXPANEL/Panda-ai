# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep Room entities and DAOs
-keep class com.example.data.** { *; }

# Keep ViewModel and StateFlow
-keep class com.example.ui.PandaViewModel { *; }

# Keep SystemIntegrations
-keep class com.example.ui.SystemIntegrations { *; }

# Keep VoiceRecognizer
-keep class com.example.ui.VoiceRecognizer { *; }

# Keep AccessibilityService
-keep class com.example.ui.PandaAccessibilityService { *; }

# Keep Compose generated classes
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }

# Keep Kotlinx Serialization
-keep class kotlinx.serialization.** { *; }
-keep class com.example.data.** { *; }

# Keep OkHttp and Moshi
-keep class okhttp3.** { *; }
-keep class com.squareup.moshi.** { *; }

# Keep Coroutines
-keep class kotlinx.coroutines.** { *; }

# Preserve line numbers for debugging
-keepattributes SourceFile,LineNumberTable

# Keep Parcelable implementations
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep Enum values
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
