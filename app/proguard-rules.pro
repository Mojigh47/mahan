# Room database entities and DAOs
-keep class moji.deliverytracker.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *

# Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Shimmer
-keep class com.facebook.shimmer.** { *; }

# Security-sensitive classes
-keep class moji.deliverytracker.SecurityHelper { *; }
-keep class moji.deliverytracker.MoneyCalculator { *; }
-keep class moji.deliverytracker.ReportCalculator { *; }
-keep class moji.deliverytracker.Validator { *; }

# Remove logging in production
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Optimization
-optimizationpasses 5
-dontusemixedcaseclassnames
-verbose

# Preserve line numbers for crash reporting
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
