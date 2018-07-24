# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\Users\Administrator\AppData\Local\Android\sdk/tools/proguard/proguard-android.txt
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

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-keepclassmembers class fqcn.of.javascript.interface.for.webview {
   public *;
}
#指定代码的压缩级别
    -optimizationpasses 5

    #包明不混合大小写
    -dontusemixedcaseclassnames

    #不去忽略非公共的库类
    -dontskipnonpubliclibraryclasses

     #优化  不优化输入的类文件
    -dontoptimize

     #预校验
    -dontpreverify

     #混淆时是否记录日志
    -verbose

     # 混淆时所采用的算法
   # -optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

    #保护注解
    -keepattributes *Annotation*

    # 保持哪些类不被混淆
    -keep public class * extends android.app.Fragment
    -dontwarn android.support.v4.**
    -keep public class * extends android.support.v4.**{ *; }
    -dontwarn android.support.v7.**
    -keep public class * extends android.support.v7.**{ *; }
    -keep public class * extends android.app.Activity
    -keep public class * extends android.app.Application
    -keep public class * extends android.app.Service
    -keep public class * extends android.content.BroadcastReceiver
    -keep public class * extends android.content.ContentProvider
    -keep public class * extends android.app.backup.BackupAgentHelper
    -keep public class * extends android.preference.Preference
    -keep public class com.android.vending.licensing.ILicensingService

   #####    Gson
   -keep class com.google.gson.** {*;}
   -keep class sun.misc.Unsafe { *; }
   -keep class com.google.gson.stream.** { *; }
   -keep class com.google.gson.examples.android.model.** { *; }
   -keep class com.google.** {
       <fields>;
       <methods>;
   }
   -keepclassmembers class * implements java.io.Serializable {
       static final long serialVersionUID;
       private static final java.io.ObjectStreamField[] serialPersistentFields;
       private void writeObject(java.io.ObjectOutputStream);
       private void readObject(java.io.ObjectInputStream);
       java.lang.Object writeReplace();
       java.lang.Object readResolve();
   }


    ###########  不混淆R类
   -keep public class **.R$*{
       public static final int *;
   }
   ###########  不混淆枚举
   -keepclassmembers enum * {
       public static **[] values();
       public static ** valueOf(java.lang.String);
   }

#   -keepclasseswithmembers class com.booyue.appmarket.bean.** {
#        <fields>;
#        <methods>;
#   }
    #apk 包内所有 class 的内部结构
    -dump class_files.txt
    #未混淆的类和成员
    -printseeds seeds.txt
    #列出从 apk 中删除的代码
    -printusage unused.txt
    #混淆前后的映射
    -printmapping mapping.txt

    #保持 native 方法不被混淆
    -keepclasseswithmembernames class * {
        native <methods>;
    }
    #保持 Parcelable 不被混淆
    -keep class * implements android.os.Parcelable {
      public static final android.os.Parcelable$Creator *;
    }
    #保持 Serializable 不被混淆
    -keepnames class * implements java.io.Serializable
    #避免混淆泛型 如果混淆报错建议关掉
    -keepattributes Signature
    # 实体类不参与混淆。
    -keep class com.booyue.karaoke.bean.** { *; }
    # 自定义控件不参与混淆
    -keep class com.booyue.karaoke.customview.** { *; }

     -keepnames class * implements android.os.Parcelable {
         public static final ** CREATOR;
     }

    #####    Vitamio
    -dontwarn io.vov.vitamio.**
    -keep class io.vov.vitamio.** { *; }


    #5.0以上sdk需要添加
     -keepclassmembers enum * {
         public static **[] values();
         public static ** valueOf(java.lang.String);
     }