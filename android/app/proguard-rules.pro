# Proguard rules for Snap Android app

# Keep main application class
-keep class com.snap.SnapApplication { *; }
-keep class com.snap.MainActivity { *; }

# Keep all data models
-keep class com.snap.data.models.** { *; }
-keep class com.snap.data.models.**$* { *; }

# Keep ViewModels
-keep class com.snap.ui.viewmodel.** { *; }
-keep class com.snap.ui.viewmodel.**$* { *; }

# Keep Retrofit service
-keep class com.snap.data.api.SnapApiService { *; }
-keep class com.snap.data.api.** { *; }

# Keep Repository
-keep class com.snap.data.repository.** { *; }
-keep class com.snap.data.repository.**$* { *; }

# Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}

# OkHttp
-keep class okhttp3.** { *; }
-keep class okhttp3.internal.platform.** { *; }
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Kotlin Serialization
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class kotlinx.serialization.** { *; }

# Keep data classes (for serialization)
-keep class com.snap.data.models.** { *; }
-keepclassmembers class com.snap.data.models.** {
    <init>(...);
    *** get*();
    void set*(...);
}

# Coroutines
-keep class kotlinx.coroutines.** { *; }
-keep class kotlin.coroutines.** { *; }

# Lifecycle & Compose
-keep class androidx.lifecycle.** { *; }
-keep class androidx.compose.** { *; }

# Room
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase

# JSON serializable objects must be kept
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}

# Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep custom application classes
-keep class * extends android.app.Activity
-keep class * extends android.content.BroadcastReceiver
-keep class * extends android.content.ContentProvider
-keep class * extends android.app.Service

# Keep view constructors for inflation
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

# Keep enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Parcelable implementations
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep BuildConfig and R classes
-keep class **.BuildConfig { *; }
-keep class **.R$* { *; }
