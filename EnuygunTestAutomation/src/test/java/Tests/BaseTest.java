package Tests;

import Utils.Driver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Tüm test sınıfları için temel teşkil eden sınıf.
 * WebDriver kurulumu (setup) ve kapatılması (tearDown) işlemlerini yönetir.
 */
public class BaseTest {
    protected static final Logger logger = LogManager.getLogger(BaseTest.class);

    /**
     * Her test metodundan önce çalışır. Tarayıcıyı başlatır.
     */
    @BeforeMethod
    public void setup() {
        logger.info("Test kurulumu başlatılıyor... Tarayıcı hazırlanıyor.");
        Driver.getDriver();
    }

    /**
     * Her test metodundan sonra çalışır. Tarayıcıyı kapatır ve temizler.
     */
    @AfterMethod
    public void tearDown() {
        logger.info("Test tamamlandı. Tarayıcı kapatılıyor.");
        Driver.quitDriver();
    }
}
