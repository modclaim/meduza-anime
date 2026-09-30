# 📱 Meduza Anime - Android Streaming Mobile App (Kotlin + Jetpack Compose)

**Meduza Anime** — bu `app.png` dizayni asosida yaratilgan, eng so'nggi barqaror **Kotlin 2.1+**, **Jetpack Compose**, **Material 3** va **Clean Architecture (MVVM)** arxitekturasi asosidagi to'liq professional anime streaming mobil ilovasi.

Ilova to'g'ridan-to'g'ri ishlab chiqilgan **Cloudflare Workers** jonli API tarmog'iga (`https://meduza-anime-api.muhammaddiyor-shokirov.workers.dev/api/v1/`) va **Neon PostgreSQL** ma'lumotlar bazasiga 100% xavfsiz bog'langan.

---

## 🎨 Dizayn va Ekranlar (`app.png` asosida)

| Ekran | Tavsif & Dizayn Xususiyatlari |
| :--- | :--- |
| **1. 🔐 Autentifikatsiya (Login / Register)** | Meduza mascot logosi, to'q fon (`#0F1015`), `Username` va `Password` kiritish maydonlari, yorqin firuza/cyan (`#00E5BE`) "Log in" tugmasi, registratsiya o'tish havolasi. |
| **2. 🏠 Asosiy Ekran (Home - "Welcome back")** | • Yuqori panel (Profil avatari, Markaziy Meduza logosi, Yuklab olishlar ikonkasi)<br>• **"CONTINUE WATCHING"** (Foydalanuvchi ko'rgan qismlari, qolgan vaqti va progress-bar bilan gorizontal karusel)<br>• **"MOST POPULAR"** (Serial/Film nishoni, poster, seriyalar soni va ★ 9.6 yulduzli reytingli kartalar)<br>• **"UPCOMING RELEASES"** (Premyeragacha qolgan vaqtni real vaqtda teskari hisoblovchi Countdown kartalar)<br>• 5 ta yorliqli pastki navigatsiya paneli (Home, Explore, Search, Library, Profile) |
| **3. 📺 Anime Tafsilotlari (Anime Detail Screen)** | • Katta sifatli Hero Banner va gradient overlay<br>• Badjlar ("Series" qizil yoki "Movie" firuza)<br>• Baholar (★ 8.8 of 6443) va foydalanuvchi tomonidan 1-10 yulduz baholash dialogi<br>• Sinopsis (ko'proq o'qish / yopish bilan)<br>• "▶ Trailer" va "➕ Watch Listga qo'shish" tugmalari<br>• Fasllar tanlovi (`[ SEASON 1 ]`, `[ SEASON 2 ]`)<br>• Qismlar ro'yxati (`E01`, `E02`, davomiyligi, "Meduza Dub" va Premium nishonlari) |
| **4. 🎞 Video Player & Ko'rish Ekrani (ExoPlayer)** | • **Media3 / ExoPlayer** orqali HLS `.m3u8` adaptiv oqimlar<br>• Video sifatini o'zgartirish (1080p, 720p, 480p, Auto)<br>• **Dublyaj audio treki tanlash** ("O'zbekcha Meduza Dubbing", "Yaponcha Original")<br>• Har 10 soniyada ko'rish vaqtini serverga avtomatik saqlash (`saveProgress`)<br>• Pullik qismlar uchun Premium himoyasi va xarid dialogi<br>• Izohlar bo'limi: Spoiler ogohlantirishlari, Like bosish, izoh qoldirish |
| **5. 🧭 Kashf qilish (Explore Screen)** | Janrlar, Seriallar, Filmlar, Davom etayotgan va Tugallangan animelar bo'yicha saralash va filtrlash. |
| **6. 🔍 Tezkor Qidiruv (Search Screen)** | Debounce qidiruv satri, janr teglari va real vaqtda qidiruv natijalari to'ri. |
| **7. 📅 Premyeralar Jadvali (Schedule Screen)** | Dushanba-Yakshanba haftalik chiqish jadvali va yaqinlashayotgan premyeragacha qolgan soniyalar (Countdown timer). |
| **8. 📚 Kutubxona (Library Screen)** | "Ko'ryapman", "Rejada", "Tugatildi" xatcho'plari va to'liq ko'rish tarixi. |
| **9. 👤 Profil & VIP Premium (Profile Screen)** | Foydalanuvchi ma'lumotlari, statistika (saqlangan, ko'rilgan), 1-klikda 30 kunlik VIP Premium obunani faollashtirish. |

---

## 🛠 Texnologiyalar Steki

- **Dasturlash tili**: Kotlin 2.1.10 (100% Type-Safe)
- **UI Framework**: Jetpack Compose (Material 3)
- **Arxitektura**: Clean Architecture + MVVM + UDF (Unidirectional Data Flow)
- **Asinxronlik**: Kotlin Coroutines & StateFlow
- **Tarmoq (Network)**: Retrofit 2.11 + OkHttp 4.12
- **Xavfsizlik**:
  - `AuthInterceptor` (Barcha so'rovlarga avtomatik `Bearer <token>` qo'shish)
  - `TokenAuthenticator` (401 xatoligida fonda avtomatik `refresh-token` orqali yangilash)
  - Jetpack DataStore Preferences orqali tokenlarni xavfsiz saqlash
- **Rasm yuklash**: Coil 3.1.0 (Async disk & memory caching)
- **Video Player**: AndroidX Media3 / ExoPlayer 1.5.1 (HLS m3u8, MP4, Audio track selection, Subtitles)
- **Navigatsiya**: Navigation Compose 2.8.8

---

## 📁 Loyiha Strukturasi

```text
app/
├── app.png                             # Mockup dizayn tasviri
├── build.gradle.kts                    # Root build script
├── settings.gradle.kts                 # Repositories and project settings
├── gradle/
│   ├── libs.versions.toml              # Version Catalog (Eng so'nggi kutubxonalar)
│   └── wrapper/
│       └── gradle-wrapper.properties   # Gradle 8.11.1 wrapper
└── app/
    ├── build.gradle.kts                # Android app module build script
    └── src/
        └── main/
            ├── AndroidManifest.xml     # Ruxsatlar va konfiguratsiyalar
            ├── res/                    # Resurslar (Vektor logolar, ranglar, mavzu)
            └── java/uz/meduza/anime/
                ├── MeduzaApp.kt        # Application class (Coil sozlamalari)
                ├── MainActivity.kt     # Entrypoint Activity
                ├── core/
                │   ├── constants/      # API URL va kalitlar
                │   ├── network/        # Retrofit, Interceptor, Authenticator
                │   ├── session/        # DataStore SessionManager
                │   └── ui/
                │       ├── theme/      # Material 3 qorong'i mavzu va ranglar
                │       └── components/ # Qayta ishlatiluvchi UI komponentlar
                ├── data/
                │   ├── models/         # DTO va API javob modellari
                │   └── repository/     # Data Layer repositorylari
                ├── navigation/         # NavGraph va Screen marshrutlari
                └── ui/
                    ├── auth/           # Login & Register
                    ├── home/           # Home ("Welcome back")
                    ├── explore/        # Explore & Genres
                    ├── search/         # Search & Filter
                    ├── detail/         # Anime Details, Seasons, Episodes
                    ├── player/         # Media3 ExoPlayer HLS Video Player
                    ├── schedule/       # Premyeralar va Countdown
                    ├── library/        # Bookmarks & History
                    └── profile/        # Profil & VIP Premium
```

---

## 🚀 Loyihani Ishga Tushirish

1. Android Studio (Ladybug / Meerkat yoki undan yuqori) dasturida `/home/admin/Desktop/meduza/app` papkasini oching.
2. Gradle Sync tugmasini bosing.
3. Emulator yoki haqiqiy Android qurilmani tanlab, **Run 'app'** (`Shift + F10`) ni bosing.
4. Ilova avtomatik ravishda Cloudflare Workers tarmog'idagi API ga ulanadi va barcha anime ma'lumotlarini yuklaydi!
