# Proguard rules for Snap Android app

# ==================== SNAP APP CLASSES ====================
-keep class com.snap.SnapApplication { *; }
-keep class com.snap.MainActivity { *; }

# Keep all data models
-keep class com.snap.data.models.** { *; }
-keep class com.snap.data.models.**$* { *; }
-keep class com.snap.data.database.entity.** { *; }

# Keep ViewModels
-keep class com.snap.ui.viewmodel.** { *; }
-keep class com.snap.ui.viewmodel.**$* { *; }

# Keep Retrofit service
-keep class com.snap.data.api.SnapApiService { *; }
-keep class com.snap.data.api.** { *; }

# Keep Repository
-keep class com.snap.data.repository.** { *; }
-keep class com.snap.data.repository.**$* { *; }

# Keep Utils
-keep class com.snap.util.** { *; }

# ==================== RETROFIT ====================
-keepattributes Signature
-keepattributes *Annotation*
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response

# ==================== OKHTTP ====================
-keep class okhttp3.** { *; }
-keep class okhttp3.internal.platform.** { *; }
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
-dontwarn okhttp3.**
-dontwarn okio.**

# ==================== KOTLIN SERIALIZATION ====================
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class kotlinx.serialization.** { *; }
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Keep data classes (for serialization)
-keep class com.snap.data.models.** { *; }
-keepclassmembers class com.snap.data.models.** {
    <init>(...);
    *** get*();
    void set*(...);
}

# ==================== COROUTINES ====================
-keep class kotlinx.coroutines.** { *; }
-keep class kotlin.coroutines.** { *; }
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

# ==================== LIFECYCLE & COMPOSE ====================
-keep class androidx.lifecycle.** { *; }
-keep class androidx.compose.** { *; }
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.material3.** { *; }
-dontwarn androidx.compose.**

# ==================== ROOM DATABASE ====================
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# ==================== HILT ====================
-keep class dagger.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
-keep class dagger.hilt.android.internal.** { *; }
-dontwarn dagger.hilt.**

# ==================== DATASTORE ====================
-keep class androidx.datastore.** { *; }
-keep class androidx.datastore.preferences.** { *; }

# ==================== JSON SERIALIZABLE ====================
-keepclassmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}

# ==================== REMOVE LOGGING ====================
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# ==================== NATIVE METHODS ====================
-keepclasseswithmembernames class * {
    native <methods>;
}

# ==================== ANDROID COMPONENTS ====================
-keep class * extends android.app.Activity
-keep class * extends android.content.BroadcastReceiver
-keep class * extends android.content.ContentProvider
-keep class * extends android.app.Service
-keep class * extends android.appwidget.AppWidgetProvider

# ==================== VIEW CONSTRUCTORS ====================
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

# ==================== PARCELABLE ====================
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}

# ==================== OPTIMIZATION ====================
-optimizationpasses 5
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

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
