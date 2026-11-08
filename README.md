# Çizim Atölyesi

6–10 yaş arası çocuklar için yatay ekranda kullanılan bir Android çizim uygulaması. Solda araç paneli,
sağda renk paneli, ortada tuval bulunur. Tuval dolu olsa bile akıcı çalışması önceliklidir.

## Özellikler (1. aşama)

- Kurşun kalem, fırça, keçeli kalem, pastel boya, sprey, silgi ve boya kovası
- Üç fırça boyutu (ince, orta, kalın)
- 24 renklik palet; seçilen renk kaleme veya tuvalin arka planına uygulanabilir
- 5 adımlık geri al ve yinele
- Çizim tuvalin dışına taşmaz; ekran dönse bile çizim kaybolmaz

Kayıt, galeriye aktarma ve boyama sayfaları sonraki aşamalarda eklenecek.

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

- `domain/` — Android'den bağımsız mantık: araç modelleri, geri al geçmişi, çizgi yumuşatma, boya kovası
- `ui/drawing/engine/` — Çizim motoru: ekran dışı bitmap, fırça türleri, bölge anlık görüntüleri
- `ui/drawing/` — Ekran, paneller, ViewModel ve dokunmatik görünüm
- `di/` — Hilt modülleri
