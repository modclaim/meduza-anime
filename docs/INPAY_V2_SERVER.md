# Meduza InPAY v2 — 2026-10-23

Backend kodi serverga joylashtirildi. Android ilova kodi va APK/AAB o‘zgartirilmadi. Jonli v2 kalit serverga o‘rnatildi va InPAY tomonidan qabul qilindi. Hozir checkout uchun bloklovchi omil — Meduza domenining InPAY whitelist ro‘yxatida yo‘qligi.

## Sabab va tuzatish

Eski v1 avtorizatsiya/create chaqiruvi xatoga tushganda server ichki demo kassani muvaffaqiyatli buyurtma sifatida qaytargan. Ochiq simulate endpointi to‘lovsiz Premium bera olgan. V2 xatosi endi 503 bilan tugaydi; demo yo‘li yo‘q. Simulate va webhook log manzillari 410 qaytaradi.

V2 so‘rovlari qat’iy `https://inpay.uz/api/v2/` manziliga yuboriladi. Secret faqat Authorization headerida; redirect kuzatilmaydi va Axios xatolari maxfiy header bilan loglanmaydi. Summa faol tarif jadvalidan olinadi, client summasi qabul qilinmaydi. `Idempotency-Key` bir invoice so‘rovining provider retry holatini himoya qiladi. Yangi foydalanuvchi bosishi yangi order yaratadi.

Androidning `POST /api/v1/payments/create` va `GET /api/v1/payments/status/:orderId` kontrakti saqlangan. `payUrl` faqat jonli `https://inpay.uz/checkout/{id}` bo‘lishi mumkin. Ommaviy status shaxsiy email yoki username qaytarmaydi; yuqori entropiyali lokal order identifikatoriga tayanadi. Eski demo buyurtmasi bilan yangi obuna berilmaydi.

## Server konfiguratsiyasi

Aynan **Meduza kassasining** jonli API v2 kaliti kerak. VoiPlay yoki boshqa kassa kalitidan foydalanmang.

Server: `/home/ubuntu/meduza-api/.env`

```dotenv
INPAY_SECRET_KEY=<Meduza kassasining sk_live_ kaliti>
# Ixtiyoriy; kiritilmasa server merchant secretini InPAY API orqali oladi va 5 daqiqa keshlaydi.
INPAY_WEBHOOK_SECRET=<whsec_ webhook siri>
```

Bu placeholderlarni amaldagi qiymat sifatida saqlamang. Kalitni chatga, Gitga, ZIPga yoki frontendga qo‘ymang. `.env` ruxsati 0600 bo‘lsin. Kalit qo‘yilgach faqat `pm2 restart meduza-api` bajariladi. Asosiy API, botlar yoki Nginxni umumiy restart qilish shart emas.

Dastlab serverdagi eski merchant token v2 tomonidan 401 `invalid_api_key` bilan rad etildi. Foydalanuvchi bergan yangi jonli v2 kalit keyin faqat server `.env` fayliga o‘rnatildi (0600); boshqa konfiguratsiya qiymatlari saqlandi. InPAY `GET balance/` 200 va `livemode:true`, `GET webhooks/secret/` 200 qaytardi. Faqat `meduza-api` restart qilindi.

`POST payments/` esa 400 `domain_not_whitelisted`, `param:return_url` qaytardi: `meduza.nexa.uz` domeni merchant kabinetidagi `/merchant/whitelist/` ro‘yxatida ruxsat etilmagan. Meduza API buni clientga xavfsiz 503 sifatida qaytaradi, demo yaratmaydi. Kalitning yaroqliligi tasdiqlandi, lekin haqiqiy invoice va uning cancel/settlement sinovi domen ruxsati sabab tugallanmagan. Whitelist himoyasi chetlab o‘tilmadi.

## Webhook va Premium

Webhook `application/json` raw bytes sifatida, 64 KB chegarada umumiy JSON parserdan oldin qabul qilinadi. `X-InPAY-Signature: t=...,v1=...` uchun HMAC-SHA256 va ±300 soniya tekshiruvi bor. Imzo yo‘q yoki xato bo‘lsa 401. Saqlash yoki provider vaqtincha ishlamasa 503 qaytariladi, shunda provider qayta urinishi mumkin.

Asosiy webhook: `https://meduza.nexa.uz/api/v1/payments/webhook`. Eski callback aliaslarining barchasi ham ayni tekshiruvdan o‘tadi. To‘g‘ri imzo ham yolg‘iz o‘zi Premium bermaydi: server o‘z merchant secret keyi bilan provider GET orqali payment ID, livemode, summa, UZS va lokal order metadata mosligini tasdiqlaydi. `succeeded`dan keyingina payment transition va obuna uzayishi bitta atomik SQL bilan commit bo‘ladi. Takroriy xabar qo‘shimcha kun bermaydi. Failed attempt hosted kassada qayta urinishga ruxsat beradi; canceled invoice yopiladi.

Kassa yaratilib, DBga yozish ishlamasa invoice bekor qilishga urinish bor. Provayder javob bermasa orphan invoice qolishi mumkin; uni merchant kabinetidagi pending invoice auditida tekshirish kerak. Bunday holatda clientga muvaffaqiyatli checkout berilmaydi.

## Dalillar va qolgan tekshiruv

- 51/51 lokal API testi; skip yo‘q. Haqiqiy PostgreSQLda parallel duplicate settlement, alohida to‘lovlarning bir userga qo‘shilishi va rollback tekshirildi.
- Server candidate uchun 19 ta fixture HTTP tekshiruv; production payment yozuvi yaratilmagan.
- Public server uchun 18 ta tekshiruv; health/katalog/tarif/app-config 200, demo 410, barcha unsigned webhook aliaslar 401. Android APK hash saqlandi, boshqa servis PIDlari o‘zgarmadi.
- Yangi sahifaning JavaScripti compile va holat testlaridan o‘tdi. 320 px brauzer kengligida gorizontal overflow yo‘q.
- Bazadagi 19 paymentning 3 ta success yozuvida provider ID yo‘q. Ular eski demo yoki qo‘lda tasdiqlashdan bo‘lishi mumkin; haqiqiy xaridlarni xato bekor qilmaslik uchun avtomatik revoke qilinmadi.
- Kalit o‘rnatilgach: balance va webhook secret 200, live mode tasdiqlandi. Invoice yaratish domen whitelist sabab 400 bilan bloklandi; invoice yaratilmagan va pul yechilmagan.
- `meduza.nexa.uz` Meduza kassasining whitelist ro‘yxatiga qo‘shilgach: mavjud tarifda invoice yaratish, live checkout URL/metadata/summa holatini tekshirish va pul yechmasdan cancel qilish; keyin haqiqiy foydalanuvchi to‘lovi bilan biznes qabul sinovi kerak.

## Tarqatish va qaytarish

Amaldagi Nexa backendini butun lokal API bilan almashtirmang. Server to‘lov modullari alohida ko‘chirildi; app.ts ichida webhook router umumiy JSON parserdan oldin ulandi. Schema migration talab qilinmadi. Source/dist, .env va DB snapshoti private deploy backupda saqlandi. Faqat `meduza-api` restart qilindi.

Private server backup: `/home/ubuntu/.meduza-deploy/20261023-inpay-v2-001/backup`. Oldingi app-config deploy: `/home/ubuntu/.meduza-deploy/20261023-app-config-001`. Bu backuplarda maxfiy konfiguratsiya bor; ularni public/Gitga chiqarmang. Eski payment moduliga rollback demo zaifligini qaytarishi mumkin, shuning uchun zaruratda payment create uchun maintenance javobini afzal ko‘ring.

Oldingi server sourceida hardkodlangan credentiallar diagnostika chiqishiga tushgan edi. Active qiymatlar src/dist va example/wrangler konfiguratsiyasidan olib tashlandi; .envda qolgan. Koordinatsiyalangan DB va eski InPAY credential rotation hali bajarilmagan.

Yangi API v2 kaliti foydalanuvchi tomonidan chatga yuborilgan. Production xavfsizligi uchun integratsiya tekshiruvi tugagach bu kalitni merchant kabinetida almashtirish va yangi qiymatni faqat server muhitida saqlash kerak. Kalitning o‘zi hujjat, Git yoki paketga kiritilmagan.
