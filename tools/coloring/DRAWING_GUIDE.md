# Boyama sayfası çizim rehberi

Bu rehber, yeni boyama sayfalarını baştan doğru çizmek için yazıldı. Mevcut 15 sayfa çizilirken karşılaşılan
her hata ve çözümü burada toplandı. Yeni bir sayfaya başlamadan önce baştan sona okunmalıdır.

## 1. Neden bu kadar dikkat gerekiyor?

Sayfalar 6–10 yaş arası çocuklar içindir ve parmakla boyanır. Uygulamadaki boya kovası, opaklığı **192 ve
üstü** olan pikselleri duvar sayar ve duvarlarla çevrili alanı doldurur. Bu yüzden:

- **Çizgilerin arasında kalan her boşluk ayrı bir bölgedir.** İki çizgi birbirine çok yaklaşırsa aralarında
  ince bir şerit oluşur. Çocuk o şeridi kovayla dolduramaz, fırçayla da tutturamaz ve sayfa "boyanmamış"
  görünür.
- **Çizgilerin sivri açıyla birleştiği köşelerde** küçük cepler kalır. Bunlar da boyanamaz.
- Vektörler ekranda görülmeden çizilirse bu sorunlar fark edilmez. **Her sayfa önizlemede denetlenir.**

Hedef: **Çok küçük bölge (300 pikselden küçük) sayısı 0** olmalıdır. Küçük bölgeler (300–1500 piksel) ancak
bilerek bırakılmışsa kabul edilir. Örneğin göz parıltısı gibi.

## 2. Çalışma akışı

1. `pages/<id>.svg` yazılır. Ölçü her zaman **1400×1000** viewBox'tır.
2. Vektör üretilir: `python build.py <id>` → `app/src/main/res/drawable/coloring_<id>.xml` ve `out/<id>.svg`.
3. Önizleme sunucusu açılır (`python -m http.server 8765`, bu klasörde) ve
   `http://localhost:8765/preview.html?page=<id>` açılır. Sol tarafta çizim, sağ tarafta boya kovası
   bölgeleri görünür (kırmızı = çok küçük, sarı = küçük).
4. Sorunlu bölgelerin yeri tarayıcı konsolunda şu satırla bulunur:
   ```js
   Object.entries(window.regionStats).map(([n,s])=>n+': '+s.regions+' tiny '+s.tiny+' small '+s.small+' :: '+s.smallRegions.filter(r=>r.area<700).map(r => r.area + ':' + r.box.join(',')).join(' | ')).join('\n')
   ```
   Çıktıdaki `alan:x1,y1,x2,y2` değeri, SVG koordinatlarıyla birebir aynıdır. Hangi şekillerin o kutuya
   düştüğüne bakılarak sebep bulunur.
5. Düzelt → yeniden üret → yeniden denetle. Çok küçük bölge kalmayana kadar sürer.
6. Sayfa `app/src/main/java/.../data/coloring/BuiltInColoringPageCatalog.kt` listesine eklenir.
7. Tüm sayfalar birlikte denetlenir (`preview.html`, parametresiz), uygulama derlenir ve tablette denenir.

> Kayıtlar sayfa kimliğini sakladığı için **var olan kimlikler hiçbir zaman değiştirilmez**. Bir sayfa
> yeniden çizilecekse aynı `<id>.svg` dosyası düzenlenir.

## 3. Kaynak biçimi: üretici neyi nasıl anlar?

SVG, bir illüstrasyon gibi **arkadan öne** yazılır. Dosyada sonra gelen öğe öndedir.

| Yazım | Anlamı |
|---|---|
| `fill="#fff"` | Öndeki nesne. Arkasında kalan bütün çizgileri örter, kendi kenarı çizilir. **Yalnızca `Z` ile kapatılmış alt yollar örter.** |
| `fill="#000"` | Dolu siyah şekil (göz bebeği, burun, tırnak). `fill-rule="evenodd"` ile içinde boşluk (parıltı) bırakılır. Kenar çizgisi istenmiyorsa `stroke="none"`. |
| dolgusuz yol | Yalnızca çizgi. Hiçbir şeyi örtmez. |
| `data-tube="W"` | Yol, W kalınlığında uçları yuvarlak bir tüpe çevrilir (sap, kuyruk, direk, hortum). Beyaz dolgulu şekil gibi örter. Bir yoldaki bütün alt yollar **tek parçada birleşir** (ağaç dalı, havuç yaprağı). |
| `id="x"` + `data-clip="x"` | Çizgi yalnızca `x` şeklinin içinde kalır (pul, desen, dilim, kırışıklık). Böylece desen çizgileri şeklin dışına taşmaz. |
| `<use xlink:href="#x" transform="matrix(-1,0,0,1,1400,0)"/>` | Sayfanın ortasına (x=700) göre ayna kopyası. **`scale(-1,1)` kullanılmaz**, yalnızca bu matris. Kopyalanacak şekil `<g id="x">` içine alınır. |

Varsayılan çizgi kalınlığı 12'dir. Ayrıntı çizgileri 7–10, desen çizgileri 8 olabilir. 7'nin altına inilmez.

### Örtme mantığını doğru kullanmak

- Bir nesnenin önünde başka bir nesne varsa çizgileri elle kesmeye çalışılmaz. Arkadaki tam çizilir, öndeki
  beyaz dolguyla sonra yazılır, üretici gerisini halleder.
- Sap, bacak ya da direk gibi parçalar **kapalı** (`Z`) yazılır. Kapatılmamış sap arkasındaki çizgiyi örtmez
  ve çizgi sapın içinden geçer.
- Zemine basan sap ve bacakların alt kenarı **zemin çizgisiyle tam çakışır** (ör. zemin y=800 ise alt kenar
  da 800). Birkaç piksel yukarıda kalırsa zeminle sap arasında ince şerit oluşur.

## 4. Ölçü kuralları

Bunlar denenerek bulunan güvenli sınırlardır. Ölçüler 1400×1000 koordinat sistemindedir.

| Kural | Değer | Neden |
|---|---|---|
| İki paralel kenar arası | **en az ~30 px** | Daha dar aralık boyanamayan şerit olur. |
| Tüp kalınlığı | **en az 28–30 px** | İnce tüpün içi boyanamaz. |
| Daireler/elipsler | **ya en az ~10 px iç içe geçsin ya da en az 30 px ayrı dursun** | Kıl payı değen ya da teğet duran iki eğri, temas noktasının iki yanında sivri cepler bırakır. |
| Göz parıltısı (siyah içinde beyaz) | **en az r10** | Daha küçüğü boyanamaz; r10 ≈ 330 piksel ile kabul edilen alt sınır. |
| Benek, tırnak, perçin, küçük delik | **siyah dolgu** | Küçük beyaz alan bırakmak yerine dolu siyah çizilir. |
| Bir köşenin başka bir kenara uzaklığı | **en az ~30 px**, ya da köşe öteki şeklin içine derin girsin | Kenara yaklaşan sivri uç, iki çizgi arasında cep oluşturur. |
| Çizgi kalınlığı | **en az 7** | İnce çizgi duvar sayılmayabilir, kova taşar. |

## 5. Karşılaşılan hatalar ve çözümleri

Bu tablo, mevcut sayfalarda yapılan her düzeltmeyi kaydeder. Benzer bir şekil çizerken önce buraya bakılır.

| Durum | Ne oldu | Çözüm |
|---|---|---|
| Hilal ay (fayton) | Hilalin iki sivri ucu cep bıraktı | Dolunay + krater çemberleri kullanıldı. **Sivri uçlu şekillerden kaçın.** |
| Tekerlek (fayton) | Teller ile iç halka arasında küçük üçgen cepler oluştu | İç halka kaldırıldı, teller tekerleğe kırpıldı (`data-clip`), göbek r30 yapıldı. Az ve geniş açılı tel kullan. |
| Pencere içi çerçeve (fayton) | Çapraz çizgiler küçük parçalar bıraktı | Tek artı çerçeve (bir dikey + bir yatay çizgi) yeterli. |
| Yüz – gövde (baykuş) | Yüz kenarı gövde kenarına çok yakındı, ince şerit oldu | Yüz daraltıldı, gözler küçültüldü. Paralel kenarlar arası ≥ 30 px. |
| Kanat ucu, ayak arası, kulak tüyü (baykuş) | Sivri uçlar komşu kenara yakın bitti | Uçlar ya komşu şeklin içine derin sokuldu ya da en az 30 px uzağa alındı. |
| Mantar sapı (kaplumbağa) | Sap `Z` ile kapatılmamıştı, zemin çizgisi sapın içinden geçti | Sap kapalı yol yapıldı, alt kenarı zeminle çakıştırıldı. |
| İki mantar (kaplumbağa) | Küçük mantar büyüğüne değiyordu | Aralarına mesafe kondu; ya iç içe ya ≥ 30 px ayrı. |
| Halkalı gezegen (roket) | Halka ile gezegen kenarı arası dardı | Halka genişletildi; halka iki yarıya bölündü: arka yarı gezegenden önce, ön yarı sonra çizildi. |
| Fil bacakları | Dikdörtgen bacaklar "kutu" gibi göründü | Alt köşeler `Q` eğrisiyle yuvarlatıldı, kenarlar hafif içe/dışa kavisli yapıldı. **Kutu şekli kullanılmaz.** |
| Fil tırnakları | Beyaz yarım daire tırnaklar küçük cep oldu | Tırnaklar siyah dolguya çevrildi. |
| Fil kulağı | Kulak ucu baş kenarına yakın bitti | Kulak ucu başın içine alındı (baş önde olduğu için örtülür). |
| Yelken – direk (yelkenli) | Yelkenler direğin tepesine kadar çıkınca direk ile yelken arasında şerit kaldı | Yelkenler direğin biraz altından (y=168) başlatıldı, direk uzatıldı. |
| Bayrak (yelkenli) | Bayrak direğe teğetti | Bayrak, direğin sağ kenarından başlatıldı. |
| Ada – dalga (yelkenli) | Ada kıyısı ile dalga çizgisi arasında şeritler oldu | Ada, dalga çizgisinin bittiği yere oturtuldu; iki çizgi aynı noktada buluşuyor. |
| Palmiye yaprakları | Yaprak uçları birbirine yakın, cepler oluştu | Yapraklar genişletildi ve sayısı azaltıldı; kavşak büyük bir hindistancevizi ile örtüldü. |
| Patiler – karın (tavşan) | Pati elipsi karın kenarına neredeyse teğetti, 1 piksellik cepler oluştu | Patiler dışa kaydırılarak karın kenarını belirgin açıyla kesmesi sağlandı. |

## 6. Güzel ve zengin sayfa için ilkeler

- **Kutucuk değil, eğri.** Dikdörtgen yerine kavisli (`C`, `Q`) kenarlar ve yuvarlatılmış köşeler kullanılır.
  Ev, araba gibi düz şekillerde bile köşeler `rx` veya eğriyle yumuşatılır.
- **Ana nesne + sahne.** Ortada büyük ve net bir kahraman (sayfanın yaklaşık yarısı), çevresinde boyanacak
  2–4 sahne öğesi: gökyüzü (güneş, bulut, yıldız, ay), zemin (çimen, çiçek, mantar), küçük yardımcı
  nesneler (kuş, balık, kelebek).
- **Kahraman büyük olur.** Ana nesne sayfanın en az yarısını kaplar; üstte ya da yanlarda geniş boş alan
  kalmaz. Boş kalan yer büyütmeyle ya da konuya uygun bir sahne öğesiyle doldurulur.
- **Her sayfa kendi sahnesini taşır; şablon tekrarlanmaz.** Galeride yan yana durduklarında sayfalar
  birbirinin kopyası gibi görünmemelidir. Yeni sayfaya başlamadan önce son eklenen 5–6 sayfa
  (`preview.html`) açılıp bakılır ve şunlar onlardan farklı seçilir:
  - **Arka plan konuya göre seçilir:** kaleye tepe ve kıvrılan yol, trene dağ ve tünel, itfaiyeye istasyon
    binası, köpeğe bahçe çiti. "Güneş + iki bulut" her sayfanın varsayılan gökyüzü değildir.
  - **Güneş** ancak sahneye bir şey katıyorsa kullanılır. Kullanılacaksa biçimi (yüzlü, yarım, dalgalı
    ışınlı) ve yeri önceki sayfalardan farklı olur; aynı 8 ışınlı güneş üst üste sayfalarda yer almaz.
  - **Zemin** her zaman düz yatay çizgi olmaz: tepe, yamaç, yol, su kıyısı, çimenli eğri, kaldırım gibi
    biçimler dönüşümlü kullanılır. Ana nesne her sayfada aynı yükseklikte, en altta durmaz.
  - **Yerleşim** değiştirilir: ortada tek nesne, çapraz hareket, yakın plan, nesnenin bir yapının önünde
    ya da içinden çıkması gibi.
  - Aynı yardımcı şekil (bulut, ağaç, çalı) kopyalanıp başka sayfaya taşınmaz; her sayfada yeniden ve
    farklı biçimde çizilir.
- **Bölge sayısı 25–70 arası.** Daha azı sıkıcı, daha fazlası küçük çocuk için yorucu olur.
- **Desen için `data-clip`.** Pul, şerit, dilim, kırışıklık gibi ayrıntılar desen çizgisiyle eklenir ve
  şekle kırpılır. Desen çizgileri arasında da ≥ 30 px boşluk bırakılır.
- **Simetrik gövdelerde ayna.** Kulak, kanat, ayak gibi çift parçalar bir kez çizilip `use` ile aynalanır.
- **Yüz ifadesi.** Gözler siyah dolgu + r10 ve üstü parıltı; gülümseme ve bıyık gibi çizgiler dolgusuz.
- **Özgünlük.** Sayfalar özgün çizimlerdir. Telifli bir boyama kitabından, çizgi filmden ya da markadan
  kopya veya fotoğraftan çeviri yapılmaz.

## 7. Bitirmeden önce kontrol listesi

- [ ] viewBox 1400×1000, kök öğede `fill="none" stroke="#000" stroke-width="12"` ve yuvarlak uç/birleşim var.
- [ ] Örtmesi gereken her şekil `fill="#fff"` ve `Z` ile kapalı.
- [ ] Zemine basan parçaların alt kenarı zeminle çakışıyor.
- [ ] Ayna için yalnızca `matrix(-1,0,0,1,1400,0)` kullanıldı.
- [ ] Sayfa son eklenen sayfalarla yan yana karşılaştırıldı: gökyüzü, zemin ve yerleşim onlardan farklı.
- [ ] Önizlemede **çok küçük: 0**. Küçük bölgelerin her biri bilerek bırakıldı (ör. göz parıltısı).
- [ ] Sol taraftaki çizimde kutu gibi duran, kopuk ya da üst üste binmiş çizgi yok.
- [ ] Sayfa kataloğa eklendi; README'deki sayfa listesi güncellendi.
- [ ] `gradlew assembleDebug` ve `testDebugUnitTest` geçiyor, sayfa tablette denendi.
