package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Enuygun ana sayfa işlemlerini temsil eden sınıf.
 */
public class EnuygunHomePage extends BasePage {

    /**
     * Çerezleri kabul eder. Eğer pop-up çıkmazsa sessizce devam eder.
     */
    public void acceptCookies() {
        try {
            logger.info("Çerez pop-up'ının tamamen görünür olması için 3 saniye bekleniyor...");
            Thread.sleep(3000); 
            clickElement(By.xpath("//*[@id='onetrust-accept-btn-handler']"));
            logger.info("Çerezler kabul edildi. Devam etmeden önce 2 saniye bekleniyor...");
            Thread.sleep(2000); 
        } catch (Exception e) {
            // Çerez pop-up'ı bulunamadı, işleme devam et
        }
    }

    /**
     * Şehir adına göre havalimanı kodunu döndürür.
     */
    private String getCityCode(String city) {
        String lowerCity = city.toLowerCase();
        if (lowerCity.contains("ankara")) return "esb";
        if (lowerCity.contains("lefkoşa")) return "ecn";
        return "ista"; // Varsayılan İstanbul
    }

    private String formatDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return "";
        String d = dateStr.replace("Ocak", "01").replace("Şubat", "02").replace("Mart", "03")
                .replace("Nisan", "04").replace("Mayıs", "05").replace("Haziran", "06")
                .replace("Temmuz", "07").replace("Ağustos", "08").replace("Eylül", "09")
                .replace("Ekim", "10").replace("Kasım", "11").replace("Aralık", "12").replace(" ", ".");

        if (d.contains(".") && d.indexOf(".") == 1) {
            d = "0" + d;
        }
        return d;
    }

    public FlightListPage searchFlightDirectly(String fromCity, String toCity, String departureDate, String returnDate) {
        String base = Utils.ConfigReader.getProperty("url");
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }

        String url = base + "/ucak-bileti/arama/sehir-sehir-" + getCityCode(fromCity) + "-" + getCityCode(toCity) 
                     + "/?gidis=" + formatDate(departureDate);
                     
        if (returnDate != null && !returnDate.isEmpty()) {
            url += "&donus=" + formatDate(returnDate) + "&yetiskin=1&sinif=ekonomi&yon=gidis-donus";
        } else {
            url += "&yetiskin=1&sinif=ekonomi&yon=tek-yon";
        }

        logger.info("Doğrudan arama URL'ine gidiliyor: " + url);
        driver.get(url);
        return new FlightListPage();
    }

    /**
     * Kullanıcı arayüzü (UI) üzerinden form doldurarak uçuş araması yapar.
     * @param fromCity Kalkış şehri
     * @param toCity Varış şehri
     * @return FlightListPage objesi
     */
    public FlightListPage searchFlightByUI(String fromCity, String toCity) {
        logger.info("UI üzerinden sadeleştirilmiş uçuş araması başlatılıyor: " + fromCity + " -> " + toCity);

        // 1. Gidiş-Dönüş Seçimi
        logger.info("'Gidiş-dönüş' seçeneği tıklanıyor.");
        clickElement(By.xpath("//div[text()='Gidiş-dönüş']/ancestor::label"));

        // 2. Hotel Checkbox Kontrolü (Uncheck)
        try {
            WebElement hotelCheckbox = driver.findElement(By.xpath("//div[contains(text(),'otelleri de listele')]/parent::label//input"));
            if (hotelCheckbox.isSelected()) {
                logger.info("Otel seçeneği seçili bulundu, kapatılıyor.");
                clickElement(By.xpath("//div[contains(text(),'otelleri de listele')]/parent::label"));
            }
        } catch (Exception e) {
            logger.warn("Otel onay kutusu bulunamadı veya kontrol edilemedi.");
        }

        // 3. Nereden
        By originInput = By.xpath("//label[contains(.,'Nereden')]//input");
        clickElement(originInput);
        sendKeysToElement(originInput, fromCity);
        try { Thread.sleep(2000); } catch (InterruptedException e) {} 
        By firstOriginResult = By.xpath("(//div[contains(@id, 'typeahead')]//li|//div[contains(@data-testid, 'ResultItem')])[1]");
        try {
            clickElement(firstOriginResult);
        } catch (Exception e) {
            driver.findElement(originInput).sendKeys(org.openqa.selenium.Keys.ENTER);
        }

        // 4. Nereye
        By destinationInput = By.xpath("//label[contains(.,'Nereye')]//input");
        clickElement(destinationInput);
        sendKeysToElement(destinationInput, toCity);
        try { Thread.sleep(2000); } catch (InterruptedException e) {}
        By firstDestinationResult = By.xpath("(//div[contains(@id, 'typeahead')]//li|//div[contains(@data-testid, 'ResultItem')])[1]");
        try {
            clickElement(firstDestinationResult);
        } catch (Exception e) {
            driver.findElement(destinationInput).sendKeys(org.openqa.selenium.Keys.ENTER);
        }

        // 5. Arama Butonu
        logger.info("'Ucuz bilet bul' butonuna tıklanıyor.");
        By searchBtn = By.xpath("//button[contains(., 'Ucuz bilet bul') or @data-testid='EnuygunHomePageFlightSearchButton']");
        clickElement(searchBtn);

        return new FlightListPage();
    }
}
