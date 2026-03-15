package Pages;

import Utils.ConfigReader;
import Utils.Driver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * BasePage sınıfı, tüm sayfa nesneleri için temel teşkil eder.
 * WebDriver başlatma, PageFactory ve ortak element etkileşim metodlarını içerir.
 */
public class BasePage {
    protected WebDriver driver;
    protected WebDriverWait wait;
    protected static final Logger logger = LogManager.getLogger(BasePage.class);

    /**
     * Constructor: WebDriver'ı alır, PageFactory'yi başlatır ve
     * config dosyasındaki explicitWait değerine göre bekleme objesini oluşturur.
     */
    public BasePage() {
        this.driver = Driver.getDriver();
        PageFactory.initElements(driver, this);
        int explicitWait = Integer.parseInt(ConfigReader.getProperty("explicitWait"));
        wait = new WebDriverWait(driver, Duration.ofSeconds(explicitWait));
    }

    /**
     * Belirtilen URL'ye gider ve pencereyi maksimize eder.
     * @param url Gidilecek URL
     */
    public void goToHomePage(String url) {
        driver.get(url);
        driver.manage().window().maximize();
    }

    /**
     * Bir elemente tıklanabilir olana kadar bekler ve tıklar.
     * @param element Tıklanacak WebElement
     */
    protected void clickElement(WebElement element) {
        wait.until(ExpectedConditions.elementToBeClickable(element)).click();
    }

    /**
     * Bir locator kullanarak elemente tıklanabilir olana kadar bekler ve tıklar.
     * @param locator Elementin seçicisi
     */
    protected void clickElement(By locator) {
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    /**
     * Bir elemente metin gönderir. Önce görünür olmasını bekler, ardından temizler.
     * @param element Metin gönderilecek WebElement
     * @param text Gönderilecek metin
     */
    protected void sendKeysToElement(WebElement element, String text) {
        wait.until(ExpectedConditions.visibilityOf(element));
        element.clear();
        element.sendKeys(text);
    }

    /**
     * Bir locator kullanarak elemente metin gönderir. Önce görünür olmasını bekler.
     * @param locator Elementin seçicisi
     * @param text Gönderilecek metin
     */
    protected void sendKeysToElement(By locator, String text) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        driver.findElement(locator).clear();
        driver.findElement(locator).sendKeys(text);
    }

    /**
     * Bir elementin görünür olmasını bekler ve metnini döndürür.
     * @param element Metni alınacak WebElement
     * @return Elementin metni
     */
    protected String getElementText(WebElement element) {
        return wait.until(ExpectedConditions.visibilityOf(element)).getText();
    }
}
