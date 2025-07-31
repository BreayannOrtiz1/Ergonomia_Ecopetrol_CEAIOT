# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\develop\android-sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

#手动启用support keep注解，
-keep,allowobfuscation @interface android.support.annotation.Keep
-keep @android.support.annotation.Keep class *
-keepclassmembers class * {
    @android.support.annotation.Keep *;
}

-verbose
-ignorewarnings
-optimizationpasses 5
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers
-dontpreverify
-dontoptimize
-dontwarn
-dontshrink
#-dontobfuscate: SourceFile,LineNumberTable 文件名，行数
-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod
#将堆栈中的SourceFile命名为Proguard
-renamesourcefileattribute SourceFile
-allowaccessmodification

-keep class * extends java.lang.annotation.Annotation { *; }
-keep interface * extends java.lang.annotation.Annotation { *; }
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService

# -libraryjars   libs/android-support-v4.jar
-dontwarn android.support.v4.**
-keep class android.support.v4.** { *; }
-keep interface android.support.v4.app.** { *; }

-keepclasseswithmembers class * {
    native <methods>;
}
-keepclasseswithmembernames class * {
    native <methods>;
}
-keepclasseswithmembernames class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembernames class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}
-keepclassmembers class * {
   public <init>(org.json.JSONObject);
}
#-assumenosideeffects class android.util.Log {
#  public static *** v(...);
#  public static *** d(...);
#  public static *** i(...);
#  public static *** w(...);
#  public static *** e(...);
#  public static *** v(...);
#  public static *** wtf(...);
#}
-keepclasseswithmembers,allowshrinking class * {
    public <init>(android.content.Context,android.util.AttributeSet);
}
-keepclasseswithmembers,allowshrinking class * {
    public <init>(android.content.Context,android.util.AttributeSet,int);
}
# Also keep - Enumerations. Keep the special static methods that are required in
# enumeration classes.
-keepclassmembers enum  * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
# Keep names - Native method names. Keep all native class/method names.
-keepclasseswithmembers,allowshrinking class * {
    native <methods>;
}

# google
-keep class com.google.**{ *;}
-keep class com.google.gson.**
# eventbus 混淆
-keepattributes *Annotation*
-keepclassmembers class ** {
    @com.vivalnk.sdk.common.eventbus.Subscribe <methods>;
}
-keep class com.vivalnk.sdk.common.eventbus.** {*;}

# Only required if you use AsyncExecutor
-keepclassmembers class * extends com.vivalnk.sdk.common.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}


# dfu library
-keep class no.nordicsemi.android.dfu.** { *; }

# Only required if you use AsyncExecutor
-keepclassmembers class * extends de.greenrobot.event.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}

#for entities
#-keep class com.vivalnk.sdk.common.**{*;}
-keep class com.vivalnk.sdk.model.**{ *; }
-keep class com.vivalnk.sdk.command.base.CommandType{ *; }
-keep class com.vivalnk.sdk.command.base.CommandAllType{ *; }
-keep class com.vivalnk.sdk.utils.ObjectPool{*;}
-keep class com.vivalnk.sdk.utils.ObjectPool$*{*;}
-keep interface com.vivalnk.sdk.Callback{ *; }
-keep interface com.vivalnk.sdk.DefaultCallback{ *; }
-keep class com.vivalnk.sdk.CommandRequest{ *; }
-keep class com.vivalnk.sdk.CommandRequest$*{ *; }
-keep interface com.vivalnk.sdk.DataReceiveListener{ *; }
-keep class com.vivalnk.sdk.VitalClient{ *; }
-keep class com.vivalnk.sdk.constant.VitalConstant{ *; }
-keep class com.vivalnk.sdk.exception.VitalCode{ *; }
-keep interface com.vivalnk.sdk.ble.BluetoothConnectListener{ *; }
-keep interface com.vivalnk.sdk.ble.BluetoothScanListener{ *; }
-keep interface com.vivalnk.sdk.common.ble.listener.BluetoothStateListener{ *; }
-keep interface com.vivalnk.sdk.common.ble.scan.BleScanListener{ *; }
-keep interface com.vivalnk.sdk.ble.ExtraConnectListener{ *; }
-keep interface com.vivalnk.sdk.ble.channel.ChannelCallback{ *; }
-keep class com.vivalnk.sdk.common.ble.scan.ScanOptions{ *; }
-keep class com.vivalnk.sdk.common.ble.scan.ScanOptions$*{ *; }
-keep class com.vivalnk.sdk.common.ble.connect.BleConnectOptions{ *; }
-keep class com.vivalnk.sdk.common.ble.connect.BleConnectOptions$*{ *; }
-keep class com.vivalnk.sdk.common.eventbus.**{  *; }
-keep class com.vivalnk.sdk.ete.**{ public *; }
-keep class com.vivalnk.sdk.core.VivalnkLibrary { *; }
-keep class com.vivalnk.sdk.data.stream.afib.EventHandler{*;}
-keep class com.vivalnk.sdk.dataparser.vv330.ReceiveDataParser_VV330$DataFeedEvent{*;}
-keep class com.vivalnk.sdk.dataparser.vv330.ReceiveDataParser_VV330{}

#for report
-keep class com.vivalnk.sdk.report.**{*;}
#for OTA
-keep interface com.vivalnk.sdk.ble.ota.OTAListener{*;}
#for public utils
-keep class com.vivalnk.sdk.utils.EcgSmoothUtils {*;}
-keep class com.vivalnk.sdk.dataparser.battery.Battery {*;}
#for database
-keep class  com.vivalnk.sdk.repository.local.database.**{*;}

#for first beat entities
-keep class com.vivalnk.sdk.vital.ete.**{ public *; }
-keep class fi.firstbeat.ete.**{  *; }

#-keep public interface com.vivalnk.sdk.ete.ETECode{ public *;}
-keep public interface com.vivalnk.sdk.common.ble.exception.BleCode{ public *;}

#bugly
-dontwarn com.tencent.bugly.**
-keep public class com.tencent.bugly.**{*;}
