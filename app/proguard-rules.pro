# Number Blocks Merge - ProGuard / R8 Rules

# 1. Gson Model Preservation (Crucial for serialization & persistence)
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.redcodersgroup.numberblocks.engine.GameSnapshot { *; }
-keep class com.redcodersgroup.numberblocks.engine.BoardState { *; }
-keep class com.redcodersgroup.numberblocks.engine.Tile { *; }
-keep class com.redcodersgroup.numberblocks.engine.Position { *; }
-keep class com.redcodersgroup.numberblocks.engine.Direction { *; }
-keep class com.redcodersgroup.numberblocks.engine.MoveResult { *; }

# 2. Google Mobile Ads (AdMob) Rules
-keep class com.google.android.gms.ads.** { *; }
-dontwarn com.google.android.gms.ads.**

# 3. AndroidX and Material Components
-keep class com.google.android.material.** { *; }
-dontwarn com.google.android.material.**

# 4. Standard Android Activity and View preservation
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

# 5. Native methods & JNI preservation
-keepclasseswithmembernames class * {
    native <methods>;
}

# 6. Enum value methods
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
