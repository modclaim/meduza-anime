# Android 1.1.0 tekshiruv qaydi — 2026-10-22

## Avtomatik tekshiruv

- Android: `ContractRegressionTest` 4 ta, `RedesignRegressionTest` 7 ta — jami 11, xato va skip yo‘q.
- API: 33 ta test, xato va skip yo‘q. Haqiqiy, ajratilgan lokal PostgreSQL bilan migrationni ikki marta qo‘llash, parallel PATCH, 401/403/422, audit metadata yashirilishi va parametrli SQL tekshirildi. 2026-10-22 sinov bazasi va konteyner keyin o‘chirildi. Keyingi server deployi quyida alohida qayd etilgan.
- Worker: yakuniy manbaning `wrangler deploy --dry-run` bundli o‘tdi. Haqiqiy deploy bajarilmadi.
- API dependency auditi: `npm audit` — 0 vulnerability.
- Release: 1.1.0, versionCode 7, applicationId `uz.meduza.anime`. APK imzosi oldingi 1.0.5 APK bilan bir xil. AAB imzosi tekshirildi. Debug activity va test video release fayllariga kirmaydi.

- Yakuniy Gradle tekshiruvi: testDebugUnitTest, assembleDebug, assembleRelease, bundleRelease, lintDebug — BUILD SUCCESSFUL. Lint: 0 error, 109 warning, 6 information. Warninglar asosan dependency yangilanishi, ishlatilmayotgan resurs va ikonka tavsiyalaridir.

## Player geometriyasi

Android 15 emulatorda haqiqiy `PlayerSurface` komponenti debug-only lokal 16:9 test video bilan tekshirildi. Ekran 1600×720. To‘ldirish rejimida chap, markaz va o‘ng piksel RGB `(52,147,255)`; Moslash rejimida chekkalar `(0,0,0)`, markaz `(52,147,255)`. Tasvir deformatsiyasiz ekran nisbatiga moslandi. Bu test haqiqiy autentifikatsiyalangan HLS translyatsiyasi yoki jismoniy telefon sinovi o‘rnini bosmaydi.

## Amaldagi server va hisob bilan bog‘liq chegaralar

- 2026-10-23: app-config amaldagi Nexa backendiga qo‘shildi; 14 ta public HTTP tekshiruvdan o‘tdi. Mavjud auth va boshqa backend modullari butun eski lokal API bilan almashtirilmadi.
- `/terms` hozir `/privacy` bilan bir xil hujjatni qaytaryapti. Reader ochilishi tuzatildi; alohida foydalanish shartlari kontenti serverda chop etilishi kerak.
- Haqiqiy hisob orqali yangi OAuth kirish, rating yozish va pulli checkout bajarilmadi. Ratingning muvaffaqiyat/xato holati va JSON kontrakti avtomatik tekshirildi; Premium sahifasi ommaviy server tarifini o‘qidi.
- Dastlab ulangan jismoniy ADB qurilma keyin uzildi. Chiqarilgan ilova sessiyasi tozalanmadi yoki hisobdan chiqarilmadi. Yakuniy vizual tekshiruv alohida `uz.meduza.anime.preview` ilovasida bajarildi.

## Native ADB oqimi

Yakuniy APKda Android 15, 720×1600, density 320 (360dp kenglik) bilan oltita tekshiruv o‘tdi:

1. Nexa ID tugmasi bor; manual email/parol yoki auth domeni/PKCE yozuvi yo‘q.
2. Maxfiylik sahifasi ilova ichida, X bilan ochiladi va yopiladi. Brauzerga o‘tmaydi.
3. Mehmon profilida mehmon yozuvi va Chiqish bor; profil API xatosi ko‘rinmaydi.
4. Mehmon tomosha tugmasidan playerga o‘tolmaydi; kirish oynasi ko‘rinadi.
5. Mehmon Qismlar tabiga o‘tolmaydi.
6. Chiqish kirish sahifasiga qaytaradi.

`tools/adb_redesign_smoke.py` faqat Preview ilovasining ma’lumotlarini reset qiladi. Chiqarilgan `uz.meduza.anime`ga tegmaydi. Skrinshotlar ishchi checkoutning `.qa/android-redesign/` papkasida saqlangan; ular repoga kiritilmagan.

Kichik viewport qo‘shimcha sinovi: 640×1280, density 320 (320dp kenglik), 125% tizim shrifti. Scroll orqali kirishning ikkala tugmasi o‘qiladi. Haqiqiy Premium narxlari va chegirma pillari ko‘rinadigan chegarada, to‘lov panelidan yuqorida joylashishi tekshirildi.

## 2026-10-23 backend va hosting tekshiruvi

- Android release APK serverga tarqatildi va to‘liq public download SHA256 mosligi tekshirildi. Android runtime kodi shu deployda o‘zgarmadi.
- InPAY v2 backendi serverga tarqatildi. Mahalliy API to‘plami: **51/51**, skip yoki xato yo‘q. Ikkita ajratilgan PostgreSQL konteynerida app-config va payment settlement tekshirildi.
- Payment SQL: bitta invoice uchun 20 parallel callback faqat bir marta kun qo‘shadi; ikkita alohida invoice bir foydalanuvchining muddatini to‘g‘ri qo‘shadi. User update xatosi payment statusini ham rollback qiladi.
- Server candidate: 19 ta fixture HTTP tekshiruvi, production bazaga payment yozilmagan. Public server: 18 ta tekshiruv; demo 410, unsigned callbacklar 401, oversized 413, noto‘g‘ri Content-Type 415.
- Live v2 credential yo‘q. Mavjud eski token v1 authdan o‘tadi, ammo v2 401 `invalid_api_key` qaytaradi. Haqiqiy checkout/pul yechish tekshirilmadi; live create 503 qaytarishi tasdiqlandi.
- 320×640 brauzer viewportida yangi return sahifasining scrollWidth qiymati 320; karta va tekshirish tugmasi ekranga sig‘di. Pending/success/failed/retry matnlari skript orqali tekshirildi.
- Faqat `meduza-api` restart qilindi. Boshqa PM2 process PIDlari, APK, Nexa auth, mavjud DB sxemalari va himoyalangan fayl hashlarida o‘zgarish yo‘q.
