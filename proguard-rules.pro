# ActiveAndroid
-keep class net.sqlcipher.** {
    *;
}
-keep class com.activeandroid.** { *; }
-keep class com.activeandroid.**.** { *; }
-keep class * extends com.activeandroid.Model
-keep class * extends com.activeandroid.serializer.TypeSerializer

-keep class android.support.v4.** { *; }
-keep interface android.support.v4.** { *; }
-dontwarn javax.annotation.**
-keep class com.google.**
-dontwarn com.google.**