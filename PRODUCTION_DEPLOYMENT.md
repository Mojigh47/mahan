# راهنمای استقرار پروداکشن (Production Deployment Guide)

## نسخه: 2.0 - آماده برای انتشار

---

## ۱. بررسی نهایی (Pre-Deployment Checklist)

### الف) امنیت (Security)
- ✅ `android:allowBackup="false"` در `AndroidManifest.xml`
- ✅ سیستم احراز هویت PBKDF2 با 120,000 تکرار
- ✅ محافظت در برابر Brute Force (5 تلاش ناموفق = قفل 5 دقیقه)
- ✅ Constant-time password comparison برای جلوگیری از Timing Attack
- ✅ CSV Injection Prevention در خروجی‌های بک‌آپ
- ✅ ProGuard/R8 Obfuscation فعال برای Release Build
- ✅ **احراز هویت سراسری**: تمامی صفحات مالی (`Home`, `Main`, `Settle`, `Reports`) به `BaseAuthActivity` مجهز شدند.
- ✅ **یکپارچگی تراکنش‌ها**: عملیات تسویه و پرداخت در سطح دیتابیس اتمیک شدند.

### ب) کیفیت کد (Code Quality)
- ✅ تمامی محاسبات مالی از `BigDecimal` استفاده می‌کنند
- ✅ تست‌های واحد برای `MoneyCalculator` و `ReportCalculator`
- ✅ Validation برای تمامی ورودی‌های کاربر
- ✅ Database migrations از نسخه 5 تا 7

### ج) CI/CD
- ✅ GitHub Actions برای Lint و Unit Tests
- ✅ Release Build Workflow با Signing
- ✅ Automated APK Upload به GitHub Releases

---

## ۲. تنظیم Keystore برای Release Build

### مرحله ۱: ایجاد Keystore
```bash
keytool -genkey -v -keystore mahan-release.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias mahan-key
```

### مرحله ۲: ذخیره اطلاعات در GitHub Secrets
در تنظیمات مخزن GitHub، Secrets زیر را اضافه کنید:
- `KEYSTORE_PATH`: مسیر فایل keystore (Base64 encoded)
- `KEYSTORE_PASSWORD`: رمز عبور keystore
- `KEY_ALIAS`: نام کلید (مثلاً `mahan-key`)
- `KEY_PASSWORD`: رمز عبور کلید

### مرحله ۳: Encode Keystore برای GitHub Secrets
```bash
base64 -i mahan-release.jks -o keystore.b64
# سپس محتوای keystore.b64 را در KEYSTORE_PATH قرار دهید
```

---

## ۳. فرآیند Release

### روش ۱: استفاده از GitHub Actions
1. به تب "Actions" در مخزن GitHub بروید
2. "Production Release Build" را انتخاب کنید
3. "Run workflow" را کلیک کنید
4. نسخه را وارد کنید (مثلاً `2.0.1`)
5. اپلیکیشن امضا شده در Releases منتشر می‌شود

### روش ۲: Build محلی
```bash
./gradlew assembleRelease
```

---

## ۴. تغییرات اصلاحی در این نسخه

### تغییرات امنیتی:
1. **Centralized Authentication**: `BaseAuthActivity` برای تمامی صفحات حساس
2. **Secure Backup**: `SecureBackupHelper` با رمزنگاری AES-256
3. **Disabled Backup**: `android:allowBackup="false"` برای جلوگیری از استخراج داده‌ها

### تغییرات زیرساختی:
1. **Enhanced ProGuard Rules**: بهتر شدن Obfuscation و حذف Logging
2. **Release Build Workflow**: خودکار سازی فرآیند Release

---

## ۵. نظارت پس از انتشار (Post-Deployment Monitoring)

### نکات مهم:
- بررسی Crash Reports از طریق Firebase Crashlytics (اگر فعال باشد)
- نظارت بر میزان استفاده و عملکرد
- بررسی نظرات کاربران در Google Play Store

---

## ۶. نسخه‌های آینده

برای نسخه‌های بعدی، اقدامات زیر توصیه می‌شود:
1. **Firebase Analytics**: اضافه کردن تحلیلات برای درک رفتار کاربر
2. **Crash Reporting**: Firebase Crashlytics برای نظارت خودکار بر خرابی‌ها
3. **Remote Config**: تنظیم ویژگی‌ها بدون نیاز به Update
4. **API Backend**: ایجاد سرور برای همگام‌سازی داده‌ها بین دستگاه‌ها

---

## ۷. تماس و پشتیبانی

برای سؤالات یا مشکلات، لطفاً در مخزن GitHub یک Issue ایجاد کنید.
