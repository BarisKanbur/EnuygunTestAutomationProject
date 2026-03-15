package Tests;

import Pages.EnuygunHomePage;
import Pages.FlightListPage;
import Utils.ConfigReader;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.List;

/**
 * Uçuş arama ve filtreleme senaryolarını içeren test sınıfı.
 */
public class FlightSearchTest extends BaseTest {

    /**
     * Testlerde kullanılacak ortak uçuş arama verilerini sağlar.
     */
    @DataProvider(name = "flightData")
    public Object[][] getFlightData() {
        return new Object[][] {
            {"Istanbul", "Ankara", "10 Mayıs 2026", "15 Mayıs 2026"}
        };
    }

    /**
     * Case 1: Temel uçuş arama ve zaman filtresi (10:00 - 18:00) doğrulaması.
     */
    @Test(dataProvider = "flightData", description = "Temel Uçuş Arama ve Zaman Filtresi Doğrulaması (10:00-18:00)")
    public void testFlightSearchWithTimeFilter(String fromCity, String toCity, String depDate, String retDate) throws InterruptedException {
        EnuygunHomePage homePage = new EnuygunHomePage();
        
        // 1. Ana sayfaya git ve çerezleri kabul et
        homePage.goToHomePage(ConfigReader.getProperty("url"));
        homePage.acceptCookies();
        logger.info("Ana sayfa yüklendi. Gözlem için 2 saniye bekleniyor...");
        Thread.sleep(2000); 
        
        // 2. UI üzerinden arama yap (Nereden, Nereye)
        FlightListPage flightListPage = homePage.searchFlightByUI(fromCity, toCity);
        logger.info("Arama sonuçları yüklendi. Gözlem için 4 saniye bekleniyor...");
        Thread.sleep(4000); 
        
        // 3. Zaman filtresini manuel olarak uyla (10:00 - 18:00)
        flightListPage.applyDepartureTimeFilterManual(10, 18);
        logger.info("Manuel 10:00-18:00 filtresi uygulandı. Gözlem için 4 saniye bekleniyor...");
        Thread.sleep(4000); 
        
        // 4. Sonuçları doğrula
        List<String> flightTimes = flightListPage.getDepartureTimes();
        Assert.assertFalse(flightTimes.isEmpty(), "10:00-18:00 zaman filtresi uygulandıktan sonra hiç uçuş bulunamadı!");
        
        for (String time : flightTimes) {
            String[] parts = time.split(":");
            int hour = Integer.parseInt(parts[0]);
            
            // Saat aralığının (10:00 - 18:00) içinde olup olmadığını kontrol et
            boolean isWithinRange = (hour >= 10 && hour <= 18);
            if (hour == 18) {
                // Eğer saat tam 18 ise, dakikanın 00 olması gerekir (18:00 sınırı)
                isWithinRange = Integer.parseInt(parts[1]) == 0;
            }
            Assert.assertTrue(isWithinRange, "Filtre aralığı (10:00-18:00) dışında uçuş bulundu: " + time);
        }
    }

    /**
     * Case 2: Türk Hava Yolları filtresi ve fiyat sıralaması doğrulaması.
     */
    @Test(dataProvider = "flightData", description = "Türk Hava Yolları Filtresi ve Fiyat Sıralaması Doğrulaması")
    public void testTurkishAirlinesPriceSorting(String fromCity, String toCity, String depDate, String retDate) throws InterruptedException {
        EnuygunHomePage homePage = new EnuygunHomePage();
        
        // 1. Ana sayfaya git ve hazırlık yap
        homePage.goToHomePage(ConfigReader.getProperty("url"));
        homePage.acceptCookies();
        logger.info("Ana sayfa yüklendi.");
        Thread.sleep(2000);
        
        // 2. Arama yap
        FlightListPage flightListPage = homePage.searchFlightDirectly(fromCity, toCity, depDate, retDate);
        logger.info("Arama sonuçları yüklendi.");
        Thread.sleep(4000);
        
        // 3. Filtreleri uygula: Zaman (10-18 Manuel) ve Türk Hava Yolları
        flightListPage.applyDepartureTimeFilterManual(10, 18);
        logger.info("Zaman filtresi uygulandı.");
        Thread.sleep(3000);
        flightListPage.applyTurkishAirlinesFilter();
        logger.info("Havayolu filtresi uygulandı.");
        Thread.sleep(4000);
        
        // 4. Doğrulama 1: Tüm uçuşların sadece Türk Hava Yolları (THY) olduğunu kontrol et
        List<String> airlines = flightListPage.getAirlineNames();
        Assert.assertFalse(airlines.isEmpty(), "Türk Hava Yolları filtresi uygulandıktan sonra uçuş bulunamadı!");
        
        logger.info("THY filtresi için havayolu isimleri doğrulanıyor...");
        for (String airline : airlines) {
            // Kesin kontrol: AnadoluJet veya AJet içermemeli, sadece THY olmalı
            Assert.assertTrue(airline.contains("Türk Hava Yolları") && !airline.contains("AJet") && !airline.contains("AnadoluJet"), 
                    "Filtrelenmiş listede THY dışı uçuş bulundu: " + airline);
        }
        
        // 5. Doğrulama 2: Fiyatların artan sırada (ucuzdan pahalıya) olduğunu kontrol et
        List<Double> actualPrices = flightListPage.getPrices();
        logger.info("Arayüzden çekilen fiyatlar: " + actualPrices);
        
        List<Double> sortedPrices = new java.util.ArrayList<>(actualPrices);
        java.util.Collections.sort(sortedPrices);
        
        Assert.assertEquals(actualPrices, sortedPrices, "Uçuş fiyatları artan sırada değil!");
        logger.info("Fiyat sıralaması başarıyla doğrulandı.");
    }

    /**
     * Case 3: Kritik kullanıcı yolu doğrulaması (Arama -> Uçuş Seçimi -> Yolcu Detayları Sayfası).
     */
    @Test(description = "Kritik Kullanıcı Yolu Doğrulaması: Arama -> Uçuş Seç -> Yolcu Bilgileri")
    public void testCriticalUserPath() throws InterruptedException {
        EnuygunHomePage homePage = new EnuygunHomePage();
        
        // Kritik yol testi için basit bir Tek Yönlü arama kullanıyoruz
        String fromCity = "Istanbul";
        String toCity = "Ankara";
        String depDate = "10 Mayıs 2026";
        
        // 1. Hazırlık ve arama
        homePage.goToHomePage(ConfigReader.getProperty("url"));
        homePage.acceptCookies();
        logger.info("Ana sayfa yüklendi.");
        Thread.sleep(2000);
        
        FlightListPage flightListPage = homePage.searchFlightDirectly(fromCity, toCity, depDate, null);
        logger.info("Arama sonuçları yüklendi.");
        Thread.sleep(4000);
        
        // 3. İlk uçuşu seç
        flightListPage.selectFirstFlight();
        logger.info("Uçuş seçildi. Sayfalar arası geçiş bekleniyor...");
        Thread.sleep(3000);
        
        // 4. Yolcu Detayları sayfasının yüklendiğini doğrula
        Pages.PassengerDetailsPage passengerDetailsPage = new Pages.PassengerDetailsPage();
        Assert.assertTrue(passengerDetailsPage.isPageLoaded(), "Yolcu Detayları sayfası yüklenemedi!");
        logger.info("Yolcu Detayları sayfasına başarıyla ulaşıldı.");
    }
}
