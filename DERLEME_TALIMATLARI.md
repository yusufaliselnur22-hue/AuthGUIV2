# AuthGUI Plugin - Derleme ve Kurulum Talimatları

## Gereksinimler
- Java 21 JDK (https://adoptium.net)
- Apache Maven 3.8+ (https://maven.apache.org/download.cgi)
- Paper 1.21.x sunucusu

## Derleme Adımları

### 1. Java JDK Kurulumu
Java 21 JDK'yı indir ve kur:
- https://adoptium.net/temurin/releases/?version=21

### 2. Maven Kurulumu
Maven'i indir ve kur:
- https://maven.apache.org/download.cgi

Veya IntelliJ IDEA kullanıyorsan Maven zaten dahili olarak gelir.

### 3. Projeyi Derleme

Komut satırını (CMD / Terminal) AuthGUI klasörünün içinde aç ve şu komutu çalıştır:

```
mvn clean package
```

Derleme başarılı olursa `target/` klasöründe `AuthGUI-1.0.0.jar` dosyası oluşur.

### 4. Kurulum
1. `AuthGUI-1.0.0.jar` dosyasını Paper sunucunun `plugins/` klasörüne koy
2. Sunucuyu başlat
3. `plugins/AuthGUI/config.yml` dosyasından ayarları özelleştir
4. Değişiklikleri uygulamak için: `/authgui reload` komutu (op yetkisi gerekli)

---

## Config Dosyası (config.yml) Açıklaması

### Renk Kodları
`&a` = yeşil, `&b` = açık mavi, `&c` = kırmızı, `&e` = sarı, `&7` = gri, `&8` = koyu gri
`&l` = kalın, `&n` = altı çizili, `&o` = italik, `&r` = sıfırla

### Minecraft Item İsimleri
Material alanı için geçerli isimler:
- EMERALD, DIAMOND, NETHER_STAR, GOLD_INGOT, IRON_INGOT
- CHEST, BOOK, COMPASS, CLOCK, MAP
- RED_WOOL, GREEN_WOOL, BLUE_WOOL, YELLOW_WOOL
- Tam liste: https://jd.papermc.io/paper/1.21/ → Material sınıfı

### Örnek Buton Özelleştirmesi
```yaml
register-button:
  name: "&a&l✦ Kayıt Ol"     # Buton ismi (renk kodu destekler)
  material: EMERALD            # Minecraft item adı
  slot: 11                     # Menüdeki slot numarası (0-26)
  lore:
    - "&7İlk kez mi giriyorsun?"
    - "&eTıkla ve kayıt ol!"
```

### Slot Numaraları (3 Satırlık Menü)
```
[ 0][ 1][ 2][ 3][ 4][ 5][ 6][ 7][ 8]
[ 9][10][11][12][13][14][15][16][17]
[18][19][20][21][22][23][24][25][26]
```
- Kayıt Ol butonu varsayılan: slot 11 (sol orta)
- Giriş Yap butonu varsayılan: slot 13 (tam orta)

---

## Özellikler
- Sunucuya ilk girişte Kayıt Ol ekranı açılır
- Tekrar girişte Giriş Yap ekranı açılır
- Şifreler SHA-256 + rastgele salt ile hashlenerek kaydedilir
- Giriş yapana kadar hareket, sohbet ve komutlar engellenir
- Çok fazla başarısız denemede geçici kilitleme
- Tüm butonlar, itemler ve mesajlar config.yml'den özelleştirilebilir
- `/authgui reload` ile sunucuyu yeniden başlatmadan config güncellenir

## Komutlar
| Komut | Açıklama | Yetki |
|-------|----------|-------|
| `/authgui reload` | Config dosyasını yeniden yükler | authgui.admin (op) |
