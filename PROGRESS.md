# Çizim Atölyesi — İlerleme ve Açık Sorular

Tarih: 2026-11-08 (3. aşama tamamlandı)

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
  - Kotlin, Jetpack Compose, Hilt (KSP), MVVM. Katmanlar `ui`, `domain`, `data`, `di`.
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
5. [x] 2. Aşama: kayıt, galeri, çöp kutusu ve dışa aktarma. Derleniyor, 29 birim testi geçiyor.
   Tablette denendi: yeni resim, otomatik kayıt, yeniden açma, dışa aktarma, çöp kutusuna atma,
   geri getirme ve hızlı çift dokunuşta gezinme koruması çalışıyor; çökme yok.
   - **Açılış ekranı galeridir.** Galeri bölümlerden oluşur: şimdilik "Resimlerim". Hazır boyama
     sayfaları 3. aşamada "Boyama sayfaları" bölümü olarak eklendi.
   - **Resmin adı yok.** Galeride küçük resimler, en son düzenlenen en başta görünür.
   - **Kayıt:** Room'da resim listesi (`id`, `updatedAt`, `deletedAt`, `backgroundColor`).
     Boya katmanı şeffaf PNG, küçük resim ayrı PNG olarak iç depoda tutulur. Geçici dosyaya yazılıp
     yeniden adlandırılır. Ana iş parçacığında yalnızca bitmap kopyası alınır, sıkıştırma IO'da yapılır.
   - **Otomatik kayıt:** son değişiklikten 2 saniye sonra, galeriye dönerken ve uygulama arka plana
     geçerken. Geri al geçmişi kaydedilmez.
   - **Çöp kutusu:** Silinen resim çöp kutusuna gider, oradan geri getirilebilir. 24 saat sonra
     kalıcı olarak silinir (galeri ve çöp kutusu ekranı açılırken temizlik yapılır).
   - **Dışa aktarma:** Çizim ekranındaki 📤 düğmesi; PNG, MediaStore ile `Pictures/Çizim Atölyesi`
     klasörüne, izin gerekmeden.
6. [x] 3. Aşama: boyama sayfaları. Derleniyor, 36 birim testi geçiyor. Tablette denendi: eski resimler
   veritabanı geçişinden sonra duruyor; 8 sayfanın hepsi açılıyor ve boya kovası çizgileri duvar sayıyor;
   fırça çizgilerin altında kalıyor; küçük resim ve dışa aktarma çizgileri içeriyor; boyanmadan kapatılan
   sayfa kaydedilmiyor. Kare sürelerinin %90'ı 16 ms altında, çökme yok.
   - **Sayfalar:** 8 özgün VectorDrawable, beyaz zemin. Her sayfanın değişmez bir kimliği var
     (`cat`, `fish`, `house`, `car`, `flower`, `butterfly`, `dinosaur`, `sun`).
   - **Kayıt:** Resim kaydına `templateId` eklendi (Room 1→2 AutoMigration, şema `app/schemas`
     altında). Çizgi katmanı kaydedilmez; açılışta sayfadan yeniden çizilir.
   - **Duvarlar:** Opaklığı 192 ve üstü olan çizgi pikselleri duvardır; daha yumuşak kenarlar
     doldurulur, böylece çizgi ile boya arasında boşluk kalmaz.
   - **Fayton (`carriage`):** İlk sürüm ticari bir boyama kitabının sayfasından fotoğrafla çevrilmişti;
     telif sorunu olmaması için özgün bir çizimle değiştirildi ve eski çizim git geçmişinden de
     temizlendi. Yeni çizim aynı türde ama özgün bir kabak biçimli masal arabasıdır (boncuklu taç,
     yuvarlak pencere, kemerli kapı, yıldızlar, uçları kıvrık şasi, kıvrık telli tekerlekler).
     Kimlik `carriage` aynı kaldı; tabletteki eski fayton resimleri silindi. Tüm bölgelerin kapalı
     olduğu bilgisayarda bölge haritasıyla, tablette boya kovasıyla denendi.
7. [x] Ek özellik: iki parmakla yakınlaştırma. Küçük bölgeler kovayla yanlış dolmasın diye eklendi.
   Derleniyor, 42 birim testi geçiyor; tablette kullanıcı denedi, çalışıyor.
   - Tuvalin kenarları değişmez, yalnızca içindeki görüntü 1–4 kat büyür. İki parmak açıp kapamak
     büyütür, birlikte sürüklemek kaydırır; görüntü tuvalin kenarından dışarı kaymaz.
   - Tek parmak her zaman çizer. Çizerken ikinci parmak konursa yarım çizgi iptal edilir.
   - Fırça ekranda hep aynı kalınlıkta görünür; büyütülmüşken resim üzerinde daha ince çizer.
   - **Kova artık parmak kalkınca, parmağın son durduğu yeri doldurur** (önceden değdiği an dolduruyordu).
   - Büyütme kaydedilmez; sayfa her açılışta normal boyutta gelir.
8. [x] 4. Aşama: gökkuşağı fırçası, simetri modu, çıkartmalar, ses efektleri. Hepsi tamamlandı.
   Her parça ayrı yapılır, tablette denenir ve onaylanınca commit edilir.
   - **Sıra:** gökkuşağı fırçası → çıkartmalar → ses efektleri → simetri (en zoru, en sona).
   - **Gökkuşağı fırçası (commit edildi: `04ffefe`):** çizgi boyunca renk kendiliğinden döner; renk
     paneli bu araçta kullanılmaz. `RainbowCycle` (48 renk kademesi, 360 dp'de bir tam tur) ve
     `RainbowStroke` (aynı kademedeki parçalar tek yol; bölüm uçları düz, bölümler bir parça örtüşür,
     yalnızca çizginin iki ucu yuvarlak). Seçiliyken boyut noktaları gökkuşağı renginde görünür.
     Ortak `toDirtyRegion` yardımcısı `StrokeBounds.kt` içinde. 48 birim testi geçiyor; tablette düz
     çizgi, sarmal, nokta ve geri alma denendi.
   - **Çıkartmalar (commit edildi: `5a31921`):** emoji; geometrik şekiller ileride ayrıca
     eklenecek. Dokununca çıkartma önce düzenleme hâlinde konur: sürüklenerek taşınır, sağ alt
     tutamaçla büyütülüp küçültülür, ✓ ile resme işlenir, ✕ ile vazgeçilir. Çıkartmanın dışına
     dokunmak onu oraya taşır. Araç değişince, kayıtta ve dışa aktarmada açık çıkartma resme işlenir;
     açık çıkartmada geri al onu kaldırır. Çıkartma aracında sağda renk paneli yerine 32 emojilik
     çıkartma paneli görünür. `StickerEditor` / `StickerDraft` / `StickerCatalog` (`domain/sticker`),
     `StickerRenderer` (motor), ortak panel iskeleti `PanelColumn`. 59 birim testi geçiyor; tablette
     koyma, büyütme, emoji değiştirme, sürükleme, ✓, geri al ve araç değiştirme denendi.
   - **Ses efektleri (commit edildi: `cf621bb`, GitHub'a gönderildi):** sesler özgün, programla üretilir. İlk açılışta
     açık gelir; galerideki 🔊/🔇 düğmesiyle kapatılır, seçim hatırlanır. Dört ses var: düğme dokunuşu,
     kova doldurma, çıkartma yapışması ve galeriye kaydetme. `SoundSynth` notaları PCM'e çevirir,
     `WavEncoder` WAV yapar, `SoundPoolPlayer` açılışta arka planda yükleyip çalar; ayar
     `PreferencesSoundSettings` içinde. Düğmeler ortak `clickableWithSound` ile ses çıkarır. Sürekli
     çizim sesi bilerek eklenmedi. 65 birim testi geçiyor; tablette düğme, kova ve çıkartma seslerinin
     çaldığı ve kapatma seçiminin yeniden açılışta korunduğu doğrulandı.
   - **Simetri (commit edildi, kullanıcı tablette onayladı):** ayna (kaleydoskop) seçildi. Modlar:
     kapalı, sol–sağ, üst–alt, 4 parça (iki eksende ayna), 8 parça (kaleydoskop). Merkez tuvalin
     ortası. Sol paneldeki 🪞 düğmesi beş seçenekli kutuyu açar; düğme seçili modun eksen simgesini
     gösterir. Eksenler ekranda soluk kesikli çizgiyle görünür, kayda ve dışa aktarmaya girmez.
     Bütün çizgi araçlarında çalışır, çıkartmalar aynalanmaz. Kova seçiliyken "×N" (simetrik, varsayılan)
     ve "×1" (tek) seçimi çıkar. Kopyalar tek adımda geri alınır (kopya başına ayrı bölge kopyası,
     `LayerChange`). Simetri resimle birlikte kaydedilmez. Kare olmayan tuvalde 8 parçanın çapraz
     kopyalarının bir kısmı tuvalin dışına düşer ve kesilir (bilinen sınır). Kod: `domain/symmetry`
     (`SymmetryTransform`, `SymmetryMode`), `domain/geometry/PixelRect` (eski `FillBounds`),
     `SymmetryPainter`, `SymmetryGuides`, `SymmetryControls`. 78 birim testi geçiyor; tablette 8 parça
     çizim, tek adımda geri alma ve simetrik kova denendi.
   - **Resmi temizleme (tablette denendi, kullanıcı onayı bekleniyor):** sol panelde ↩️ ↪️ altında 🧹
     düğmesi (🗑️ galeride kullanıldığı için seçilmedi). Basınca "Resim temizlensin mi?" kutusu açılır
     (✅ / ❌; dışarı dokunmak "hayır" sayılır). Boya katmanını (çizgi, dolgu, işlenmiş çıkartma) ve
     düzenlenen çıkartmayı siler; boyama sayfasının çizgileri ve arka plan rengi kalır. Tek adımda
     geri alınır. Kod: `DrawingEngine.clear()`, `Canvas.clearPixels()`, `ClearConfirmDialog`.
   - **Sıradaki adım:** 4. aşama bitti; yeni iş kullanıcıyla konuşularak belirlenir. Push yalnızca istenirse yapılır.
   - **İsteğe bağlı küçük düzeltme:** bazı emojiler (ör. kedinin bıyıkları) kare düzenleme
     çerçevesinden taşıyor; resme işleme doğru. Kullanıcı isterse yapılır.

> Her aşama onay alınmadan başlamaz.

## Ortam bilgisi

- **adb:** `C:\Users\Lenovo\AppData\Local\Android\Sdk\platform-tools\adb.exe`
- **Tablet:** Seri numarası `9p8pmf9hdmrwm7l7`, Android 16 (API 36).
- **SDK:** 36 ve 37 kurulu.
- **JAVA_HOME:** Android Studio'nun JBR'ını gösteriyor.
- **Derleme ve test:** proje kökünde `./gradlew.bat assembleDebug testDebugUnitTest -q`. Test sayısı
  `app/build/test-results/testDebugUnitTest/*.xml` içindeki `tests=` değerlerinin toplamıdır (şu an 78).
- **Git Bash'te adb:** önce `export MSYS_NO_PATHCONV=1`. Ekran görüntüsü küçültülmüş gelirse görüntüdeki
  koordinatlar 1,06 ile çarpılarak dokunma koordinatı bulunur. Uygulama açıldıktan sonraki ilk dokunuş
  bazen algılanmaz; ikinci kez dokunulur.
- **Python:** PATH eski olabilir; tam yol kullanılır: `C:\Users\Lenovo\AppData\Local\Programs\Python\Python313\python.exe`.
- **GitHub:** `main` dalı `origin/main`'i izliyor; push yalnızca kullanıcı isteyince yapılır.
