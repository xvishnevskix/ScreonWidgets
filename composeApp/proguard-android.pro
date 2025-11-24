##############################################
# ===== БАЗОВЫЕ АТРИБУТЫ (оставляем) =====
##############################################
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod, KotlinMetadata

# Если помечаете классы/члены @Keep — не трогаем
-keep @androidx.annotation.Keep class * { *; }
-keepclasseswithmembers class * {
    @androidx.annotation.Keep <methods>;
    @androidx.annotation.Keep <fields>;
}

##############################################
# ========= ANDROID ENTRYPOINTS =============
##############################################
# Обычно AGP сам сохраняет entrypoints из манифеста, но можно явно:
-keep class ** extends android.app.Application { *; }
-keep class ** extends android.app.Activity { *; }
-keep class ** extends android.app.Service { *; }
-keep class ** extends android.content.BroadcastReceiver { *; }
-keep class ** extends android.content.ContentProvider { *; }

##############################################
# =============== COMPOSE ===================
##############################################
# Compose, как правило, не требует правил, скрываем лишние warning'и
-dontwarn androidx.compose.**
# Функциональные типы Kotlin часто встречаются в compose-ламах
-keep class kotlin.jvm.functions.Function* { *; }

##############################################
# ======= Kotlinx Serialization =============
##############################################
-keep,includedescriptorclasses class kotlinx.serialization.** { *; }
-keepclassmembers class * { *** Companion; }
-keepclasseswithmembers class * { kotlinx.serialization.KSerializer serializer(...); }
-keepclassmembers class * implements kotlinx.serialization.KSerializer { *; }
-dontnote kotlinx.serialization.**

# Все сериализуемые модели вашего проекта
-keep @kotlinx.serialization.Serializable class videoTrade.** { *; }
# Если используете полиморфизм с именами классов — сохранить имена
-keepnames class videoTrade.** { *; }

##############################################
# =============== Parcelize =================
##############################################
-keepclassmembers class ** implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

##############################################
# ========= OkHttp / Okio / Ktor ============
##############################################
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn io.ktor.**

##############################################
# ================== Koin ===================
##############################################
-keep class org.koin.** { *; }
-keepclassmembers class * {
    @org.koin.core.annotation.* <fields>;
    @org.koin.core.annotation.* <methods>;
}
-dontwarn org.koin.**

##############################################
# =============== Timber ====================
##############################################
# Вырезаем логи в release
-assumenosideeffects class timber.log.Timber {
    public static void d(...);
    public static void v(...);
    public static void i(...);
    public static void w(...);
    public static void e(...);
    public static void wtf(...);
    public static void log(...);
}

##############################################
# =============== LibVLC (JNI) ==============
##############################################
# ВАЖНО: LibVLC ищет классы по строковым именам в JNI (FindClass),
# поэтому полностью сохраняем имена и пакеты org.videolan.*
-keep class org.videolan.libvlc.** { *; }
-keep interface org.videolan.libvlc.** { *; }
-keep class org.videolan.medialibrary.** { *; }

# Гарантируем неизменность имен пакетов/классов в этом пространстве
-keeppackagenames org.videolan,org.videolan.libvlc,org.videolan.medialibrary
-keepnames class org.videolan.** { *; }

# На всякий случай скрываем предупреждения от LibVLC
-dontwarn org.videolan.**

##############################################
# =============== GeckoView =================
##############################################

# Не ругаться на любые отсутствующие классы из mozilla-пакетов
-dontwarn java.beans.BeanInfo
-dontwarn java.beans.FeatureDescriptor
-dontwarn java.beans.IntrospectionException
-dontwarn java.beans.Introspector
-dontwarn java.beans.PropertyDescriptor

##############################################
# ======= Доп. рекомендации/заметки =========
##############################################
# 1) Не используйте в этом проекте:
#    -repackageclasses
#    -adaptclassstrings
# они могут сломать JNI (LibVLC) и прочий рефлекшн.
#
# 2) Если увидите падения из-за рефлексии других библиотек —
#    добавляйте для них точечные -keep (аналогично Koin/LibVLC).
#
# 3) Если появятся предупреждения вида "Missing classes …" —
#    сначала проверьте зависимости; если это опциональные классы,
#    можно подавить -dontwarn для конкретного пакета.
