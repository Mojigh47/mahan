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

# Security-sensitive classes (must not be obfuscated)
-keep class moji.deliverytracker.SecurityHelper { *; }
-keep class moji.deliverytracker.MoneyCalculator { *; }
-keep class moji.deliverytracker.ReportCalculator { *; }
-keep class moji.deliverytracker.Validator { *; }
-keep class moji.deliverytracker.BaseAuthActivity { *; }
-keep class moji.deliverytracker.LauncherActivity { *; }
-keep class moji.deliverytracker.EncryptedBackupHelper { *; }
-keep class moji.deliverytracker.TransactionalSettlementHelper { *; }
-keep class moji.deliverytracker.SecureBackupHelper { *; }

# Remove logging in production (aggressive)
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
}

# Remove debug assertions
-assumenosideeffects class java.lang.Throwable {
    public void printStackTrace();
}

# Aggressive obfuscation for sensitive data
-obfuscationdictionary obfuscation-dictionary.txt
-packageobfuscationdictionary obfuscation-dictionary.txt
-classObfuscationDictionary obfuscation-dictionary.txt

# Aggressive optimization for production
-optimizationpasses 7
-optimizeaggressively
-dontusemixedcaseclassnames
-verbose

# Remove unused code and resources
-dontshrink
-dontoptimize
-dontpreverify

# Inline small methods
-allowaccessmodification
-mergeinterfacesaggressively

# Preserve line numbers for crash reporting (production)
-keepattributes SourceFile,LineNumberTable,Exceptions,InnerClasses,EnclosingMethod,Signature
-renamesourcefileattribute SourceFile

# Keep all custom exceptions
-keep public class * extends java.lang.Exception
-keep public class * extends java.lang.Throwable

# Keep all model classes
-keep class moji.deliverytracker.** { *; }

# Prevent removal of critical methods
-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclasseswithmembernames class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
