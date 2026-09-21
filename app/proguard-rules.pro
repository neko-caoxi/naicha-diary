-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**

-keepclassmembers class com.naicha.diary.data.** {
    *** Companion;
}
-keepclasseswithmembers class com.naicha.diary.data.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.naicha.diary.data.**$$serializer { *; }

-keep class kotlinx.serialization.** { *; }
-dontwarn kotlinx.serialization.**
