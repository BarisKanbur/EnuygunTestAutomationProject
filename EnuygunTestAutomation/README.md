# Enuygun Test Otomasyon Projesi

Bu proje, www.enuygun.com uçuş bileti arama ve filtreleme fonksiyonlarını test eden bir otomasyon çatısıdır.

## Proje Hakkında
Bu çerçeve, uçuş arama ve filtreleme süreçlerini doğrular. Aşağıdaki dört ana senaryoyu kapsamaktadır:

### Senaryo 1: Kalkış Saati Filtresi
- Enuygun.com ana sayfasına gider.
- Belirli bir rota için (Örn: İstanbul - Ankara) uçuş araması yapar.
- Kalkış saati filtresini uygular (10:00 - 18:00).
- Listelenen tüm uçuşların tam olarak bu saat aralığında olduğunu doğrular.

### Senaryo 2: Havayolu ve Fiyat Sıralaması
- Uçuş araması yapar.
- Kalkış saati filtresini (10:00 - 18:00) uygular.
- Belirli bir havayolu filtresini (Türk Hava Yolları) uygular.
- Listelenen uçuşların sadece Türk Hava Yolları olduğunu doğrular.
- Uçuşların fiyatlarına göre artan sırada (ucuzdan pahalıya) listelendiğini doğrular.

### Senaryo 3: Kritik Kullanıcı Yolu
- Uçuş araması yapar.
- Listeden ilk uçuşu seçer.
- Yolcu detayları (ödeme/checkout) sayfasının başarıyla yüklendiğini doğrular.

### Senaryo 4: Veri Analizi ve Görselleştirme
- İstanbul - Lefkoşa rotası için veri çeker.
- Çekilen verileri `target/flight_results.csv` dosyasına kaydeder.
- Havayolu bazlı minimum, maksimum ve ortalama fiyat analizleri yapar.
- Fiyat dağılımlarını grafiklerle (`target/airline_price_analysis.png`, `target/time_price_distribution.png`) görselleştirir.

## Teknik Detaylar
- **Dil**: Java 17
- **Otomasyon Aracı**: Selenium WebDriver
- **Test Çerçevesi**: TestNG
- **Tasarım Deseni**: Page Object Model (POM) + Singleton Driver
- **Raporlama**: ExtentReports v5 (HTML Raporu)
- **Loglama**: Log4j2
- **Veri Analizi**: OpenCSV & XChart

## Özellikler
- **Çoklu Tarayıcı Desteği**: Chrome ve Firefox üzerinde çalışabilir (`config.properties` üzerinden yapılandırılabilir).
- **Headless Mod**: CI/CD süreçleri için arayüzsüz çalışma desteği.
- **Ekran Görüntüsü**: Hata alan testlerde otomatik olarak ekran görüntüsü alır ve rapora ekler.
- **Detaylı Loglama**: Tüm adımlar Türkçe log mesajları ile takip edilebilir.
- **Yerelleştirme**: Tüm kod yorumları, loglar ve hata mesajları Türkçe'dir.

## Kurulum ve Çalıştırma

1. Proje ana dizinine gidin.

2. Tüm testleri Maven ile çalıştırın:
   ```bash
   mvn clean test
   ```

3. Belirli bir senaryoyu çalıştırın:
   
   **Sadece Senaryo 1 (Zaman Filtresi):**
   ```bash
   mvn test -Dtest=FlightSearchTest#testFlightSearchWithTimeFilter
   ```
   
   **Sadece Senaryo 2 (Havayolu ve Fiyat Sıralaması):**
   ```bash
   mvn test -Dtest=FlightSearchTest#testTurkishAirlinesPriceSorting
   ```
   
   **Sadece Senaryo 3 (Kritik Yol):**
   ```bash
   mvn test -Dtest=FlightSearchTest#testCriticalUserPath
   ```

   **Sadece Senaryo 4 (Veri Analizi):**
   ```bash
   mvn test -Dtest=DataAnalysisTest
   ```

## Yapılandırma
`src/main/resources/config.properties` dosyasını düzenleyerek tarayıcıyı veya bekleme sürelerini değiştirebilirsiniz:
```properties
browser=chrome
url=https://www.enuygun.com/
implicitWait=10
explicitWait=15
headless=false
```
