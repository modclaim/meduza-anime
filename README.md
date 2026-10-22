# Meduza Android va iOS

Android mijoz Kotlin, Jetpack Compose, Retrofit, Room, DataStore va Media3dan foydalanadi. iOS mijoz SwiftUI / WKWebView ichida `pwa/` ilovasini ochadi. `composeApp/` dizayn prototipi iOSning chiqariladigan interfeysi sifatida ulanmagan.

## Android

JDK 17+ va Android SDK kerak; `local.properties`dagi `sdk.dir`ni o‘z kompyuteringizga moslang. Loyihani Android Studioda ochish mumkin. Min SDK 24, target/compile SDK 36; hozirgi versiya **1.1.0**, versionCode **7**.

API manzili `app/src/main/java/uz/meduza/anime/core/constants/AppConstants.kt`da. Server `/api/v1/` bilan tugaydigan HTTPS manzilida ishlashi kerak. Android Emulator uchun lokal manzil odatda `http://10.0.2.2:5000/api/v1/`; manifestdagi tarmoq xavfsizligi sozlamalarini ham mos tekshiring.

Release signing qiymatlarini ignored `keystore.properties`da saqlang; format `keystore.properties.example`da. `storeFile` root `app/` loyihasiga nisbatan aniqlanadi. Parollarni Gradle skriptiga yoki Gitga yozmang. Debug standart Android debug kalitini ishlatadi.

```bash
./gradlew :app:testDebugUnitTest :app:assembleDebug :app:assembleRelease :app:bundleRelease :app:lintDebug --no-daemon --max-workers=1 '-Dorg.gradle.jvmargs=-Xmx1536m -XX:ActiveProcessorCount=2' -Pkotlin.compiler.execution.strategy=in-process
```

Natijalar:

- `app/build/outputs/apk/debug/app-debug.apk`
- `app/build/outputs/apk/release/app-release.apk`
- `app/build/outputs/bundle/release/app-release.aab`
- `app/build/test-results/testDebugUnitTest/`
- `app/build/reports/lint-results-debug.html`

Batafsil yangilanish va boshqaruv sozlamalari: [Android 1.1 qaydi](docs/ANDROID_REDESIGN_1.1.md). Debug versiya **Meduza Preview** (`uz.meduza.anime.preview`) sifatida o‘rnatiladi va chiqarilgan ilova sessiyasiga tegmaydi.

`ContractRegressionTest` Toshkent sanasi/hafta kuni va yangi/eski rating JSON kontraktini tekshiradi. Release yig‘ilishi uchun signing fayli va keystore mavjud bo‘lishi shart. Lint xatolari buildni to‘xtatadi; mavjud kutubxonalar, deprecated API va resurslarga oid warninglar alohida hisobotda ko‘rsatiladi.

Foydalanuvchi hisobdan chiqqanda tokenlar, Roomdagi shaxsiy tarix, bookmark va sync ishlar tozalanadi. To‘lovda qo‘lda yozilgan tarif fallbacklari yo‘q: haqiqiy server tariflari yuklanadi. Token yangilashda tarmoq xatosi seansni darhol o‘chirmaydi; server tokenni rad etsa seans tozalanadi.

## iOS

`iosApp/iosApp.xcodeproj`ni macOS/Xcode bilan oching, signing teamni tanlang. Minimum iOS 16.4. `iosApp/Info.plist` ichidagi `MeduzaWebAppURL`ni HTTPS `/app/` manziliga sozlang. Avval PWAni `npm run build:server` bilan tayyorlab Node server yonida tarqating; to‘liq ko‘rsatma [root README](../README.md)da.

WKWebView standart cookie/session saqlashdan, inline videodan va AirPlaydan foydalanadi; tashqi to‘lov havolalari brauzerda ochiladi. O‘z domenidagi navigatsiya ilova ichida qoladi. Yuklanish xatosida qayta urinish bor. PWAning iOS mijoz ichida o‘rnatish oynasi chiqarilmaydi.

Linux auditida plist va manba tuzilmasi tekshirildi; Xcode build, signing, haqiqiy iPhone va App Store tekshiruvi bajarilmadi. Rootdagi eski `ios-build/` bu auditning yangi iOS natijasi emas.
