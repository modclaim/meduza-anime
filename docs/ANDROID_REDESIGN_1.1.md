# Android 1.1 yangilanishi

1.1.0 / versionCode 7: bosh sahifa, katalog, qidiruv, anime tafsilotlari, kutubxona, profil, kirish, Premium va jadval bir xil qora/mint dizayn tizimiga moslandi. Tasvirlar va kontent birinchi o‘rinda; rangli yirik badge, ortiqcha glow va dekor kamaytirildi. Mavjud logotip saqlandi.

## Foydalanuvchi oqimlari

- Kirish faqat **Nexa ID orqali kirish**. Ilova ichida qo‘lda email/parol formasi, auth domeni va PKCE haqidagi texnik yozuvlar yo‘q. Tizim brauzerining xavfsiz OAuth manzil satri yashirilmaydi.
- Mehmon katalog, qidiruv va anime ma’lumotlarini ko‘radi. Episode tab, player, shaxsiy kutubxona va baholash kirishni talab qiladi. Profile serverdan so‘ralmaydi: mehmon yozuvi va Chiqish tugmasi mavjud.
- Baholash alohida tugma va 1–10 raqamli tanlovdan iborat. Xatoda dialog yopilmaydi va avvalgi baho saqlanadi; muvaffaqiyatda o‘rtacha baho, ovozlar soni va foydalanuvchi bahosi yangilanadi.
- Premium alohida sahifa. Tariflar serverdan olinadi; chegirma taqqoslanadigan tariflarning haqiqiy kunlik narxi asosida hisoblanadi. Soxta narx yoki demo to‘lov yo‘q. To‘lov faqat xavfsiz HTTPS havola orqali ochiladi va hisob natijasi tekshiriladi.
- Maxfiylik va foydalanish hujjatlari ilova ichidagi WebView o‘qish oynasida ochiladi; yuqori o‘ngda X, yuklanish va qayta urinish holati bor. JavaScript va lokal fayl kirishi o‘chiq.
- Player to‘liq ekranda system barlardan foydalanilgan joyni ham egallaydi. **To‘ldirish** tasvirni proporsiyasini saqlab ekranga yoyadi; **Moslash** videoni kesmasdan ko‘rsatadi. Har xil ekran/video nisbatida Moslash rejimidagi tabiiy bo‘sh joylar saqlanishi mumkin. Ilova fon rejimiga o‘tganda ijro to‘xtaydi; audio focus va naushnik uzilishi hisobga olinadi.

## Server boshqaruvi

Backendga alohida ruxsat bilan faqat app-config funksiyasi qo‘shildi. API kontrakti [APP_CONFIG_CONTRACT.md](APP_CONFIG_CONTRACT.md)da. Ishchi checkoutdagi implementatsiya `../api/`da; yangi endpoint moduli alohida `Meduza-app-config-backend.zip` paketida beriladi:

- `GET /api/v1/app-config`: ommaviy flaglar;
- `PATCH /api/v1/app-config`: amaldagi ADMIN huquqi tekshirilgan yozish;
- `guestEnabled`, `maintenanceEnabled`, `maintenanceTitle`, `maintenanceMessage`;
- Postgresda saqlanadi, maxfiy audit ID GET orqali berilmaydi;
- ilova foregroundda 30 soniyada tekshiradi, xatoda oxirgi tasdiqlangan qiymat saqlanadi.

Bu endpoint amaldagi serverga tarqatilgach boshqaruv ishlaydi. Productionni almashtiradigan yangi backend deploy qilinmagan; migration ajratilgan test bazasida sinovdan o‘tkazilgan. Eski client versiyalari flaglarni o‘qimaydi.

## Build va qurilma tekshiruvi

Debug ilova `uz.meduza.anime.preview` / **Meduza Preview** sifatida yig‘iladi: telefondagi chiqarilgan ilova va uning foydalanuvchi sessiyasi bilan alohida ishlaydi. Release applicationId `uz.meduza.anime` va mavjud signing konfiguratsiyasi saqlangan.

```bash
./gradlew :app:testDebugUnitTest :app:assembleDebug :app:assembleRelease :app:bundleRelease :app:lintDebug --no-daemon --max-workers=1 '-Dorg.gradle.jvmargs=-Xmx1536m -XX:ActiveProcessorCount=2' -Pkotlin.compiler.execution.strategy=in-process
```

APK va AAB build natijalari `app/build/outputs/`da. `RedesignRegressionTest` mehmon/video siyosati, texnik ishlar, konfiguratsiya turlari, baholashning xato/muvaffaqiyat holatlari va Premium hisobini tekshiradi. Qo‘shimcha ADB natijalari yakuniy tekshiruv qaydida beriladi.

Muhim: 2026-10-22 tekshiruvida ishlab turgan `/terms` manzili `/privacy` bilan bir xil maxfiylik hujjatini qaytardi. Ilova hujjatlarni to‘g‘ri readerda ochadi; alohida foydalanish shartlarini serverda e’lon qilish kontent egasining vazifasi. Ilovada huquqiy matn o‘ylab yozilmagan.
