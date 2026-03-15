package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;

/**
 * Yolcu Detayları (Ödeme öncesi son adım) sayfasını temsil eden sınıf.
 */
public class PassengerDetailsPage extends BasePage {

    @FindBy(xpath = "//h2[contains(text(), 'Yolcu Bilgileri') or contains(text(), 'İletişim Bilgileri') or contains(@class, 'passenger-title')]")
    private WebElement pageTitle;

    @FindBy(id = "contact_email")
    private WebElement contactEmailInput;

    /**
     * Yolcu detayları sayfasının başarıyla yüklenip yüklenmediğini kontrol eder.
     * E-posta giriş alanı veya "İletişim Bilgileri" başlığının görünürlüğü doğrulanır.
     * @return Sayfa yüklendiyse true, aksi halde false döndürür.
     */
    public boolean isPageLoaded() {
        try {
            logger.info("Yolcu Detayları sayfasının (Adım 2: Bilgilerini Gir) yüklenmesi bekleniyor...");
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            
            // İletişim e-posta alanı veya belirli başlığın gelmesini bekle
            return wait.until(d -> {
                try {
                    // İletişim e-posta alanı görünür mü?
                    boolean emailVisible = driver.findElements(By.id("contact_email")).stream().anyMatch(e -> e.isDisplayed());
                    // "İletişim Bilgileri" başlığı veya "Bilgilerini Gir" adımı görünür mü?
                    boolean titleVisible = driver.findElements(By.xpath("//h2[contains(text(), 'İletişim Bilgileri')]")).stream().anyMatch(e -> e.isDisplayed());
                    
                    return emailVisible || titleVisible;
                } catch (Exception e) {
                    return false;
                }
            });
        } catch (Exception e) {
            logger.error("Yolcu detayları sayfası yükleme doğrulaması başarısız oldu: " + e.getMessage());
            return false;
        }
    }
}
