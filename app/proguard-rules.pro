# ==============================================================================
# ChouTuan Android Release R8 / ProGuard Rules
# ==============================================================================

-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*
-keepattributes Signature,InnerClasses,EnclosingMethod

# ------------------------------------------------------------------------------
# Domain & Data Serialization Models (防止代码压缩后数据类被混淆裁剪)
# ------------------------------------------------------------------------------
-keep class com.example.myapplication.domain.model.** { *; }
-keep class com.example.myapplication.data.** { *; }

# Gson / Kotlin Serialization
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# ------------------------------------------------------------------------------
# Retrofit2
# ------------------------------------------------------------------------------
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# ------------------------------------------------------------------------------
# OkHttp3 & Okio
# ------------------------------------------------------------------------------
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
-keep class okhttp3.** { *; }

# ------------------------------------------------------------------------------
# Room (Entity / DAO / Database)
# ------------------------------------------------------------------------------
-dontwarn androidx.room.paging.**
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keepclassmembers class * {
    @androidx.room.* <fields>;
    @androidx.room.* <methods>;
}
