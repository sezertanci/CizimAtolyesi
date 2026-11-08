# Çizim Atölyesi — İlerleme ve Açık Sorular

Tarih: 2026-11-08

## Proje özeti

6–10 yaş arası çocuklar için yerel (native) bir Android çizim uygulaması. Uygulama, bağlı tablette test edilecek.

- **Ekran:** Yatay kullanım.
  - Solda dar ve kaydırılabilir bir araç paneli.
  - Sağda dar ve kaydırılabilir bir renk paneli.
  - Ortada tuval.
- **Tuval:** Arka plan rengi değiştirilebilir. Çizim tuvalin dışına taşmaz.
- **En önemli öncelik:** Tuval dolu olsa bile donma veya takılma olmaması.
- **Özellikler:**
  - Çizimler kaydedilir, tekrar açılıp düzenlenebilir.
  - Galeriye dışa aktarılabilir.
  - Hazır boyama sayfaları vardır.
- **Çalışma biçimi:**
  - İş aşama aşama ilerler. Önce her konuda anlaşılır, sonra uygulanır.
  - Kod tekrarı, güvenlik açığı, performans sorunu ve çökme olmaz.
  - Sorular tek tek sorulur.

## Verilen kararlar

| # | Konu | Karar |
|---|------|-------|
| S1 | Uygulama adı | **Çizim Atölyesi** |
| S2 | Hedef yaş grubu | **6–10 yaş** |
| S3 | Giriş yöntemi | **Yalnızca parmak** (stylus basınç desteği yok) |
| S4 | Boyama sayfaları | **İkisi birden:** boya kovasıyla doldurma ve serbest boyama |
| S5 | Ek özellikler | **Gökkuşağı fırçası, simetri modu, çıkartmalar, ses efektleri.** Ebeveyn kilidi yok. |
| S6 | Proje klasörü | `C:\Users\Lenovo\Projeler\CizimAtolyesi` |

## Teknik yaklaşım (onaylandı)

- **Teknoloji ve mimari:**
  - Kotlin, Jetpack Compose, Hilt (KSP), MVVM. Katmanlar şimdilik `ui`, `domain`, `di`; `data` kayıt aşamasında eklenecek.
  - AGP 9.5.1 (yerleşik Kotlin), Gradle 9.8.2, compileSdk 37, targetSdk 36, minSdk 29.
  - Gradle 9.9.0 yerine 9.8.2 seçildi: 9.9.0 çok yeniydi ve indirme sunucusu o sırada hata veriyordu.
  - compileSdk 37: Compose 1.12 ve core 1.19 bunu istiyor; targetSdk 36 kaldı.
  - Paket adı `dev.sezertanci.drawingworkshop`.
  - Yatay yöne kilitli, tam ekran (immersive) mod. `INTERNET` izni yok.
- **Çizim motoru:**
  - Biten fırça darbeleri ekran dışı bir bitmap'e işlenir; yalnızca o an çizilen yol yeniden çizilir.
  - Ara dokunma noktaları (historical points) yumuşatılarak kullanılır. Yalnızca ilk parmak çizer.
  - Boya kovası (flood fill) arka planda çalışır; tolerans 48, kenarlar 2 geçişte karıştırılır.
  - **Geri al / yinele 5 adım.** Plandaki "işlem listesi + yeniden oynatma" yerine **bölge anlık görüntüsü**
    kullanıldı: her işlemden önce yalnızca değişecek dikdörtgen kopyalanır, geri alırken yer değiştirilir.
- **Arayüz:** Araç simgeleri emoji. Renk panelinin üstünde "kalem rengi / arka plan rengi" seçimi var.
- **Katmanlar (alttan üste):**
  - Arka plan rengi.
  - Boya katmanı. Silgi yalnızca bu katmanda çalışır.
  - Boyama sayfasının çizgi katmanı.
- **Kayıt ve dışa aktarma:**
  - Otomatik kayıt.
  - Çizim listesi Room'da, küçük resimler iç depolamada tutulur.
  - PNG, MediaStore ile `Pictures/Çizim Atölyesi` klasörüne aktarılır.
- **Araçlar:**
  - Kurşun kalem, fırça, keçeli kalem, pastel boya, sprey, silgi ve boya kovası.
  - 3 fırça boyutu, geri al ve yinele.
- **Boyama sayfaları:** Özgün vektör çizimlerdir.

## Sıradaki sorular (tek tek sorulacak)

- [x] S3–S6 cevaplandı (bkz. Verilen kararlar).

## Yapılacak işler

1. [x] S3–S6 sorularını tek tek sor ve cevapları bu dosyaya işle.
2. [x] 1. Aşama planı onaylandı (geri al 5 adım, remote `git@github.com:sezertanci/CizimAtolyesi.git`).
3. [x] Proje iskeleti, `.gitignore`, `.editorconfig`, `README.md`.
4. [x] 1. Aşama: derleniyor, 18 birim testi geçiyor. Tablette (2560x1600) denendi: fırça, pastel, sprey,
   kova, silgi, geri al, yinele ve arka plan rengi çalışıyor; çökme yok. Kare sürelerinin %90'ı 14 ms
   altında. Not: tuval çok doluyken yoğun bir yük testi henüz yapılmadı.
5. [ ] Sonraki aşamaları sırayla planla, onaylat ve uygula:
   - Kayıt, açma ve düzenleme.
   - Galeriye dışa aktarma.
   - Boyama sayfaları.
   - Seçilen ek özellikler.

> Her aşama onay alınmadan başlamaz.

## Ortam bilgisi

- **adb:** `C:\Users\Lenovo\AppData\Local\Android\Sdk\platform-tools\adb.exe`
- **Tablet:** Seri numarası `9p8pmf9hdmrwm7l7`, Android 16 (API 36).
- **SDK:** 36 ve 37 kurulu.
- **JAVA_HOME:** Android Studio'nun JBR'ını gösteriyor.
