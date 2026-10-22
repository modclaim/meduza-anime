# Android ilovasini masofadan boshqarish

`GET /api/v1/app-config` — ochiq, maxfiy qiymatlarsiz va `Cache-Control: no-store` bilan qaytadi. Android ilova ochilganda va faol holatda har 30 soniyada tekshiradi. Internet bo‘lmasa oxirgi tasdiqlangan qiymatni saqlaydi. Noto‘g‘ri JSON/turdagi qiymat avvalgi sozlamani almashtirmaydi.

```json
{
  "success": true,
  "data": {
    "guestEnabled": true,
    "maintenanceEnabled": false,
    "maintenanceTitle": "Texnik ishlar",
    "maintenanceMessage": "Ilovani yaxshilayapmiz. Birozdan keyin qayta urinib ko‘ring.",
    "updatedAt": "2026-10-22T12:00:00.000Z"
  }
}
```

Bitta administrator so‘rovi bilan boshqarish:

```bash
# MEDUZA_ADMIN_TOKENni server tomonidan berilgan Meduza administrator access tokeniga sozlang.
curl --request PATCH 'https://meduza.nexa.uz/api/v1/app-config' \
  --header "Authorization: Bearer $MEDUZA_ADMIN_TOKEN" \
  --header 'Content-Type: application/json' \
  --data '{"guestEnabled":false}'
```

```bash
curl --request PATCH 'https://meduza.nexa.uz/api/v1/app-config' \
  --header "Authorization: Bearer $MEDUZA_ADMIN_TOKEN" \
  --header 'Content-Type: application/json' \
  --data '{"maintenanceEnabled":true,"maintenanceTitle":"Texnik ishlar","maintenanceMessage":"Yangi qismlar tayyorlanmoqda. Tez orada qaytamiz."}'
```

Qayta ochish: `{"guestEnabled":true,"maintenanceEnabled":false}`. Ikkala flagni bir PATCHda yuborish mumkin. Faqat yuborilgan maydon yangilanadi; parallel PATCHlar bir-birining boshqa maydonini qaytarib yubormaydi.

## Ruxsat va tekshiruv

PATCH autentifikatsiyalangan va bazadagi amaldagi ADMIN roliga ega foydalanuvchiga ochiq. JWT ichida yozilgan eski ADMIN roliga ishonilmaydi. Booleanlar haqiqiy JSON boolean bo‘lishi kerak: `"false"` satri rad etiladi. Noma’lum maydon, bo‘sh so‘rov, bo‘sh matn, 80 belgidan uzun sarlavha va 500 belgidan uzun xabar rad etiladi. SQL parametrlar bilan bajariladi. O‘zgartirgan admin IDsi bazada saqlanadi, ommaviy GET orqali qaytarilmaydi.

## Tarqatish

1. `maintenance/20261022-app-config.sql`ni avval izolyatsiyalangan baza/Neon branchda tekshiring.
2. Baza zaxirasini oling. Neon uchun direct ulanishni `DATABASE_URL_UNPOOLED`da belgilang va `node maintenance/apply-app-config.cjs`ni bajaring. Skript mavjud kontent/foydalanuvchilarni o‘zgartirmaydi; singleton jadval va boshlang‘ich qator yaratadi.
3. Yangi `src/modules/app-config/` modulini va Express/Hono router ulanishlarini amaldagi backendga tarqating. Bu o‘zgarish Nexa auth, katalog yoki to‘lov servislarini almashtirishni talab qilmaydi.
4. GET javobini tekshiring. Administrator tokeni bilan PATCH va foydalanuvchi tokeni bilan 403 holatini tekshiring. Productionni test uchun texnik ishlarga o‘tkazmang.
5. Yangi Android 1.1.0ni tarqating. Oldingi Android versiyalarida yangi flaglar o‘qilmaydi.

Endpoint hali tarqatilmagan/404 bo‘lsa Androidning birinchi ishga tushishida texnik ishlar o‘chiq, mehmon ko‘rish yoqilgan bo‘ladi. Mehmonlar har qanday holatda video yoki qismga kira olmaydi. Bu ilova darajasidagi cheklov; mavjud media API/CDN himoyasi o‘z serverida ham saqlanishi kerak.

Ushbu ishda migration va haqiqiy SQL konkurent yangilanishlar alohida lokal PostgreSQL konteynerida tekshirildi; production bazaga o‘zgartirish kiritilmadi. Tekshiruvni qaytarish uchun `TEST_POSTGRES_URL` bilan `npm test` ishlatiladi. Integratsiya testi xavfsizlik uchun faqat ajratilgan `127.0.0.1:55439/postgres` manzilida ishlaydi.

Sozlamalarning singleton qatori mavjud bo‘lmasa GET 503 qaytaradi. Bu clientdagi oxirgi tasdiqlangan texnik ishlar holatini tasodifan bekor qilmaslik uchun kerak.
