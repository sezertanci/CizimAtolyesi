# Çizim Atölyesi

6–10 yaş arası çocuklar için yatay ekranda kullanılan bir Android çizim uygulaması. Solda araç paneli,
sağda renk paneli, ortada tuval bulunur. Tuval dolu olsa bile akıcı çalışması önceliklidir.

## Özellikler

### Çizim (1. aşama)

- Kurşun kalem, fırça, keçeli kalem, pastel boya, sprey, silgi ve boya kovası
- Üç fırça boyutu (ince, orta, kalın)
- 24 renklik palet; seçilen renk kaleme veya tuvalin arka planına uygulanabilir
- 5 adımlık geri al ve yinele
- Çizim tuvalin dışına taşmaz; ekran dönse bile çizim kaybolmaz

### Galeri ve kayıt (2. aşama)

- Uygulama galeriyle açılır; resimlerin adı yoktur, en son düzenlenen en başta görünür
- Resimler otomatik kaydedilir: son değişiklikten 2 saniye sonra, galeriye dönerken ve uygulama arka plana geçerken
- Silinen resim çöp kutusuna gider, oradan geri getirilebilir; 24 saat sonra kalıcı olarak silinir
- 📤 düğmesiyle resim cihaz galerisine (`Pictures/Çizim Atölyesi`) PNG olarak aktarılır

### Boyama sayfaları (3. aşama)

- Galeride "Resimlerim"in altında 9 boyama sayfası bulunur: kedi, balık, ev, araba, çiçek, kelebek, dinozor, güneş, fayton
- Sayfaya dokununca o sayfadan yeni bir resim açılır; sayfanın kendisi değişmez
- Resim ancak boyanınca kaydedilir; açılıp kapatılan sayfa galeride boş resim bırakmaz
- Çizgiler her zaman en üstte durur: fırça çizgileri örtmez, silgi çizgileri silmez
- Boya kovası çizgileri duvar sayar ve rengi çizgilerin yumuşak kenarlarının altına kadar doldurur
- Küçük resim ve dışa aktarılan PNG çizgileri de içerir

## Gereksinimler

- JDK 17 veya üstü (Android Studio'nun içindeki JBR yeterlidir)
- Android SDK: platform 37 (derleme için)
- Android 10 (API 29) veya üstü bir cihaz ya da emülatör

`local.properties` dosyasında `sdk.dir` ayarlı olmalıdır; Android Studio bu dosyayı kendisi oluşturur.

## Çalıştırma

Derleme:

```bash
./gradlew assembleDebug
```

Birim testleri:

```bash
./gradlew testDebugUnitTest
```

Bağlı cihaza yükleme:

```bash
./gradlew installDebug
```

Windows'ta `./gradlew` yerine `gradlew.bat` kullanılabilir.

## Proje yapısı

- `domain/` — Android'den bağımsız mantık: araç modelleri, geri al geçmişi, çizgi yumuşatma, boya kovası,
  çöp kutusu süresi (`domain/drawing`) ve ölçekleme hesabı (`domain/geometry`)
- `data/` — Room veritabanı, resim dosyalarının iç depoda saklanması, kayıt kuyruğu ve dışa aktarma
- `data/coloring/` — Boyama sayfası kataloğu ve sayfaların çizimi; sayfalar `res/drawable/coloring_*.xml` vektörleridir
- `ui/drawing/engine/` — Çizim motoru: ekran dışı bitmap, fırça türleri, bölge anlık görüntüleri
- `ui/drawing/` — Ekran, paneller, ViewModel, otomatik kayıt ve dokunmatik görünüm
- `ui/gallery/` — Galeri ve çöp kutusu ekranları
- `ui/navigation/` — Ekranlar arası gezinme
- `ui/components/` — Ortak arayüz bileşenleri
- `di/` — Hilt modülleri
