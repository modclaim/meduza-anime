# Meduza Anime Production Proguard Rules

# Preserve debugging information in stack traces
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod,Exceptions

# 1. Meduza Anime Models and Architecture
-keep class uz.meduza.anime.data.models.** { *; }
-keep class uz.meduza.anime.data.local.entities.** { *; }
-keep class uz.meduza.anime.data.local.dao.** { *; }
-keep class uz.meduza.anime.core.network.** { *; }
-keep class uz.meduza.anime.core.session.** { *; }
-keep class uz.meduza.anime.core.crash.** { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# 2. Gson Rules
-keepclassmembers enum * { *; }
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
-dontwarn com.google.gson.**
-keep class com.google.gson.** { *; }

# 3. Retrofit & OkHttp
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# 4. Room Database
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
-keep class androidx.room.** { *; }

# 5. Media3 / ExoPlayer
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**

# 6. Coil Image Loading
-keep class coil3.** { *; }
-dontwarn coil3.**

# 7. Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-dontwarn kotlinx.coroutines.**

# 8. Timber Logging
-dontwarn timber.log.**
-keep class timber.log.** { *; }
