package Tests;

import Utils.ConfigReader;
import Utils.Driver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class DomDumper {
    public static void main(String[] args) throws InterruptedException {
        WebDriver driver = Driver.getDriver();
        driver.get("https://www.enuygun.com/ucak-bileti/arama/sehir-sehir-ista-ecn/?gidis=10.05.2026&yetiskin=1&sinif=ekonomi&yon=tek-yon");
        Thread.sleep(15000);
        try {
            java.util.List<WebElement> flights = driver.findElements(By.cssSelector(".flight-item"));
            if (!flights.isEmpty()) {
                System.out.println("========== FIRST FLIGHT ITEM DOM ==========");
                System.out.println(flights.get(0).getAttribute("outerHTML"));
                System.out.println("================================");
            } else {
                System.out.println("No flights found!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Driver.quitDriver();
        }
    }
}
