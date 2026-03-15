package Utils;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.time.Duration;

/**
 * WebDriver yönetimini sağlayan Singleton Driver sınıfı.
 */
public class Driver {
    private static WebDriver driver;

    private Driver() {
    }

    /**
     * Yapılandırma dosyasına göre uygun WebDriver'ı başlatır ve döndürür.
     * @return WebDriver objesi
     */
    public static WebDriver getDriver() {
        if (driver == null) {
            // Tarayıcı tipini oku (chrome/firefox)
            String browser = ConfigReader.getProperty("browser");
            if (browser == null) {
                browser = "chrome";
            }
            // Headless mod kontrolü
            boolean headless = Boolean.parseBoolean(ConfigReader.getProperty("headless"));
            
            switch (browser.toLowerCase()) {
                case "firefox":
                    WebDriverManager.firefoxdriver().setup();
                    FirefoxOptions firefoxOptions = new FirefoxOptions();
                    if (headless) firefoxOptions.addArguments("--headless");
                    driver = new FirefoxDriver(firefoxOptions);
                    break;
                case "chrome":
                default:
                    WebDriverManager.chromedriver().setup();
                    ChromeOptions chromeOptions = new ChromeOptions();
                    if (headless) chromeOptions.addArguments("--headless");
                    chromeOptions.addArguments("--disable-gpu");
                    chromeOptions.addArguments("--window-size=1920,1080");
                    driver = new ChromeDriver(chromeOptions);
                    break;
            }
            
            int implicitWait = Integer.parseInt(ConfigReader.getProperty("implicitWait"));
            if (!headless) {
                driver.manage().window().maximize();
            }
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));
        }
        return driver;
    }

    /**
     * Mevcut WebDriver oturumunu kapatır ve nesneyi temizler.
     */
    public static void quitDriver() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }
}
