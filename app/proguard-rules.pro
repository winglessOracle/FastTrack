# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

##---------------Begin: General Rules ----------
# Keep source file names and line numbers for better crash logs
-keepattributes SourceFile,LineNumberTable

# Hide source file names in crash logs to avoid exposing file structure
-renamesourcefileattribute SourceFile

# Keep the application class and its methods
-keep class wesseling.io.fasttime.FastTrackApplication { *; }

# Keep all model classes and their fields
-keep class wesseling.io.fasttime.model.** { *; }

# Keep enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

##---------------Begin: Gson ----------
# Gson uses reflection to access fields, so we need to keep them
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**

# Keep Gson classes
-keep class com.google.gson.** { *; }

# Keep classes that will be serialized/deserialized by Gson
-keep class wesseling.io.fasttime.model.CompletedFast { *; }
-keep class wesseling.io.fasttime.model.FastingState { *; }
-keep class wesseling.io.fasttime.model.DateTimePreferences { *; }

# Prevent proguard from stripping interface information from TypeAdapter, TypeAdapterFactory,
# JsonSerializer, JsonDeserializer instances (so they can be used in @JsonAdapter)
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

##---------------Begin: Jetpack Compose ----------
# Keep Compose-related classes
-keep class androidx.compose.** { *; }
-keepclassmembers class androidx.compose.** { *; }

# Keep Compose runtime
-keep class androidx.compose.runtime.** { *; }
-keepclassmembers class androidx.compose.runtime.** { *; }

# Keep Compose UI
-keep class androidx.compose.ui.** { *; }
-keepclassmembers class androidx.compose.ui.** { *; }

# Keep Compose Material
-keep class androidx.compose.material.** { *; }
-keepclassmembers class androidx.compose.material.** { *; }

# Keep Compose Material3
-keep class androidx.compose.material3.** { *; }
-keepclassmembers class androidx.compose.material3.** { *; }

##---------------Begin: Accompanist ----------
# Keep Accompanist classes
-keep class com.google.accompanist.** { *; }
-keepclassmembers class com.google.accompanist.** { *; }

##---------------Begin: AndroidX ----------
# Keep AndroidX classes
-keep class androidx.core.** { *; }
-keep class androidx.appcompat.** { *; }
-keep class androidx.lifecycle.** { *; }
-keep class androidx.activity.** { *; }

##---------------Begin: Widget ----------
# Keep widget-related classes
-keep class wesseling.io.fasttime.widget.** { *; }

##---------------Begin: Kotlin ----------
# Keep Kotlin metadata
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keep,includedescriptorclasses class wesseling.io.fasttime.**$$serializer { *; }
-keepclassmembers class wesseling.io.fasttime.** {
    *** Companion;
}
-keepclasseswithmembers class wesseling.io.fasttime.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

##---------------Begin: Debugging ----------
# Uncomment these lines for debugging ProGuard issues
# -printseeds seeds.txt
# -printusage unused.txt
# -printmapping mapping.txt