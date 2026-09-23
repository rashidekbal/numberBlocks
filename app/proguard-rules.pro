# ==============================================================================
# Number Blocks - Production ProGuard & R8 Configuration
# ==============================================================================

# ------------------------------------------------------------------------------
# 1. Optimization & Attributes (Preserve line numbers for Crashlytics)
# ------------------------------------------------------------------------------
-keepattributes SourceFile,LineNumberTable
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes EnclosingMethod,InnerClasses
-dontwarn javax.annotation.**

# ------------------------------------------------------------------------------
# 2. Gson Serialization & Game State Engine Models
# ------------------------------------------------------------------------------
# Preserve all Gson annotations and reflection support
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
    @com.google.gson.annotations.Expose <fields>;
}
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Game State Models serialized to/from JSON in PreferencesManager
-keep class com.redcodersgroup.numberblocks.engine.** { *; }
-keepclassmembers class com.redcodersgroup.numberblocks.engine.** { *; }

# Game & Profile Data Models
-keep class com.redcodersgroup.numberblocks.games.LeaderboardEntry { *; }
-keep class com.redcodersgroup.numberblocks.profile.AvatarManager$* { *; }
-keep class com.redcodersgroup.numberblocks.theme.Theme { *; }

# ------------------------------------------------------------------------------
# 3. Google Play Games Services v2 & Play Services
# ------------------------------------------------------------------------------
-keep class com.google.android.gms.games.** { *; }
-keep interface com.google.android.gms.games.** { *; }
-keep class com.google.android.gms.common.** { *; }
-keep interface com.google.android.gms.common.** { *; }
-keep class com.google.android.gms.tasks.** { *; }
-keep interface com.google.android.gms.tasks.** { *; }
-dontwarn com.google.android.gms.games.**
-dontwarn com.google.android.gms.common.**

# ------------------------------------------------------------------------------
# 4. Google Mobile Ads (AdMob)
# ------------------------------------------------------------------------------
-keep public class com.google.android.gms.ads.** {
    public *;
}
-keep public interface com.google.android.gms.ads.** {
    public *;
}
-dontwarn com.google.android.gms.ads.**

# ------------------------------------------------------------------------------
# 5. Firebase Analytics & Crashlytics
# ------------------------------------------------------------------------------
-keep public class * extends java.lang.Exception
-dontwarn com.google.firebase.**
-keep class com.google.firebase.** { *; }

# ------------------------------------------------------------------------------
# 6. AndroidX, Material Components & ViewBinding
# ------------------------------------------------------------------------------
-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**
-keep class androidx.appcompat.** { *; }
-keep class androidx.recyclerview.widget.** { *; }
-keep class androidx.viewpager2.** { *; }

# ViewBinding classes
-keep class com.redcodersgroup.numberblocks.databinding.** { *; }

# WorkManager, Room Database & App Startup (required by Google Mobile Ads)
-keep class androidx.work.** { *; }
-keep class androidx.work.impl.** { *; }
-keep class * extends androidx.work.Worker { *; }
-keep class * extends androidx.work.ListenableWorker { *; }
-keep class * extends androidx.work.InputMerger { *; }
-keep class androidx.work.impl.WorkDatabase_Impl { *; }
-keep class * extends androidx.work.impl.WorkDatabase { *; }
-keep class * extends androidx.room.RoomDatabase { *; }
-keep class androidx.room.** { *; }
-dontwarn androidx.work.**
-dontwarn androidx.room.**

-keep class androidx.startup.** { *; }
-keep class * extends androidx.startup.Initializer { *; }
-keep class * implements androidx.startup.Initializer { *; }
-dontwarn androidx.startup.**

# ------------------------------------------------------------------------------
# 7. Custom Views & View Components (Inflated via XML)
# ------------------------------------------------------------------------------
-keep class com.redcodersgroup.numberblocks.ui.*View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    *;
}
-keep class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

# ------------------------------------------------------------------------------
# 8. Standard Android Framework Components
# ------------------------------------------------------------------------------
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# ------------------------------------------------------------------------------
# 9. Native Methods & JNI
# ------------------------------------------------------------------------------
-keepclasseswithmembernames class * {
    native <methods>;
}

# ------------------------------------------------------------------------------
# 10. Enums & Serializable
# ------------------------------------------------------------------------------
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
