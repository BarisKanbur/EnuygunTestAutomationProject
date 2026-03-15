package Pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import java.time.Duration;
import Models.FlightData;

import java.util.ArrayList;
import java.util.List;

/**
 * Uçuş listeleme sayfasındaki filtreleme ve seçim işlemlerini temsil eden sınıf.
 */
public class FlightListPage extends BasePage {

    @FindBy(xpath = "//div[contains(@class, 'ctx-filter-airline') or contains(text(), 'Havayolu')]/ancestor::div[contains(@class, 'header')]")
    private WebElement airlineFilterAccordion;

    @FindBy(css = "label.search__filter_airlines-TK")
    private WebElement turkishAirlinesFilter;

    /**
     * Kalkış saati slider'ını manuel olarak belirli bir aralığa çeker.
     * @param startHour Başlangıç saati (0-24)
     * @param endHour Bitiş saati (0-24)
     * @return FlightListPage objesi
     */
    public FlightListPage applyDepartureTimeFilterManual(int startHour, int endHour) {
        logger.info("Zaman filtresi uygulanıyor: " + startHour + ":00 - " + endHour + ":00");
        
        // Sayfanın ve filtrelerin tam yüklenmesi için bekleme
        try { Thread.sleep(8000); } catch (Exception e) {}

        // Selectors (Subagent tespiti)
        By railSelector = By.cssSelector(".rc-slider-rail");
        By handle1Selector = By.cssSelector(".rc-slider-handle-1");
        By handle2Selector = By.cssSelector(".rc-slider-handle-2");

        // 1. Akordiyonun açık olup olmadığını kontrol et (Slider görünürlüğü ile)
        boolean isSliderVisible = false;
        try {
            // Try to find the rail using the new selector
            List<WebElement> rails = driver.findElements(railSelector);
            if (!rails.isEmpty()) {
                isSliderVisible = rails.get(0).isDisplayed();
            }
        } catch (Exception e) {
            // Slider bulunamadı, muhtemelen kapalı
        }

        if (!isSliderVisible) {
            try {
                // Herhangi bir filtre başlığına tıklamayı dene
                logger.info("Slider görünmüyor. Filtre başlıklarından biri tıklanmaya çalışılıyor...");
                WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
                List<WebElement> accordions = driver.findElements(By.cssSelector(".ctx-filter-departure-return-time"));
                if (accordions.get(0).isDisplayed()) {
                    forceClickViaJS(accordions.get(0));
                    Thread.sleep(1000);
                }
            } catch (Exception e) {
                logger.warn("Filtre başlığı bulunamadı veya tıklanamadı: " + e.getMessage());
            }
        } else {
            logger.info("Zaman filtresi akordiyonu zaten açık.");
        }

        // 2. Slider tutamaçlarını ayarla
        try {
            List<WebElement> rails = driver.findElements(railSelector);
            List<WebElement> startHandles = driver.findElements(handle1Selector);
            List<WebElement> endHandles = driver.findElements(handle2Selector);

            if (!rails.isEmpty() && !startHandles.isEmpty() && !endHandles.isEmpty()) {
                WebElement rail = rails.get(0);
                WebElement leftHandle = startHandles.get(0);
                WebElement rightHandle = endHandles.get(0);
                
                int sliderWidth = rail.getSize().getWidth();
                logger.info("Slider ray genişliği: " + sliderWidth);

            Actions action = new Actions(driver);
            
            // Sol tutamaç (Handle 1) -> startHour
            // Başlangıçta 0:00'da. Hareket: (startHour/24)*width
            int leftMoveOffset = (int)((startHour / 24.0) * sliderWidth);
            action.clickAndHold(leftHandle).moveByOffset(leftMoveOffset, 0).release().perform();
            logger.info("Sol tutamaç " + startHour + ":00 konumuna çekildi.");
            
            Thread.sleep(1500);

            // Sağ tutamaç (Handle 2) -> endHour
            // Başlangıçta 24:00'da. Hareket: -((24-endHour)/24)*width
            int rightMoveOffset = (int)(((endHour - 24.0) / 24.0) * sliderWidth);
            action.clickAndHold(rightHandle).moveByOffset(rightMoveOffset, 0).release().perform();
            logger.info("Sağ tutamaç " + endHour + ":00 konumuna çekildi.");
            }
        } catch (Exception e) {
            logger.error("Slider ayarlanırken hata oluştu: " + e.getMessage());
        }
        
        try { Thread.sleep(5000); } catch (Exception e) {} // Sonuçların yenilenmesi için bekle
        return this;
    }

    /**
     * Sadece Türk Hava Yolları filtresini uygular.
     */
    public FlightListPage applyTurkishAirlinesFilter() {
        forceClickViaJS(airlineFilterAccordion);
        try { Thread.sleep(1000); } catch (Exception e) {}
        forceClickViaJS(turkishAirlinesFilter);
        logger.info("Türk Hava Yolları filtresi uygulandı.");
        try { Thread.sleep(2000); } catch (Exception e) {} 
        return this;
    }

    private void forceClickViaJS(WebElement element) {
        try {
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block: 'center', inline: 'nearest'});", element);
            Thread.sleep(1000);
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
            Thread.sleep(1000);
        } catch (Exception e) {
            logger.error("JS Tıklama Hatası: " + e.getMessage());
        }
    }

    public List<String> getDepartureTimes() {
        List<String> times = new ArrayList<>();
        List<WebElement> flights = driver.findElements(org.openqa.selenium.By.cssSelector(".flight-item"));
        for (WebElement flight : flights) {
            times.add(flight.findElement(org.openqa.selenium.By.cssSelector("[data-testid=\"departureTime\"]")).getText());
        }
        return times;
    }

    public List<Double> getPrices() {
        List<Double> prices = new ArrayList<>();
        List<WebElement> flights = driver.findElements(org.openqa.selenium.By.cssSelector(".flight-item"));
        for (WebElement flight : flights) {
            String priceText = flight.findElement(org.openqa.selenium.By.cssSelector("[data-testid=\"flightInfoPrice\"]")).getText()
                    .replaceAll("[^\\d,]", "").replace(",", ".");
            if (!priceText.isEmpty()) {
                prices.add(Double.parseDouble(priceText));
            }
        }
        return prices;
    }

    public List<String> getAirlineNames() {
        List<String> airlines = new ArrayList<>();
        List<WebElement> flights = driver.findElements(org.openqa.selenium.By.cssSelector(".flight-item"));
        for (WebElement flight : flights) {
            airlines.add(flight.findElement(org.openqa.selenium.By.cssSelector(".summary-marketing-airlines")).getText());
        }
        return airlines;
    }

    public List<FlightData> getAllFlightData() {
        List<FlightData> flightDataList = new ArrayList<>();
        List<WebElement> flightItems = driver.findElements(By.cssSelector(".flight-item"));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        for (WebElement item : flightItems) {
            try {
                List<WebElement> depTimes = item.findElements(By.cssSelector("[data-testid='departureTime']"));
                if (depTimes.isEmpty()) continue;
                String departure = depTimes.get(0).getText();
                String arrival = item.findElement(By.cssSelector("[data-testid='arrivalTime']")).getText();
                String airline = item.findElement(By.cssSelector(".summary-marketing-airlines")).getText();
                String priceText = item.findElement(By.cssSelector("[data-testid='flightInfoPrice']")).getText()
                        .replaceAll("[^\\d,]", "").replace(",", ".");
                double price = priceText.isEmpty() ? 0 : Double.parseDouble(priceText);
                String connection = "Direkt";
                List<WebElement> transitInfo = item.findElements(By.cssSelector("[data-testid='transferStateTransfer'], [data-testid='transferStateDirect'], .summary-transit"));
                if (!transitInfo.isEmpty()) {
                    connection = transitInfo.get(0).getText();
                }
                
                String duration = "Bilinmiyor";
                List<WebElement> durationInfo = item.findElements(By.cssSelector("[data-testid='departureFlightTime'], .summary-duration span"));
                if (!durationInfo.isEmpty()) {
                    duration = durationInfo.get(0).getText();
                }
                flightDataList.add(new FlightData(departure, arrival, airline, price, connection, duration));
            } catch (Exception e) {}
        }
        int implicitWait = Integer.parseInt(Utils.ConfigReader.getProperty("implicitWait"));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWait));
        return flightDataList;
    }

    public void selectFirstFlight() {
        clickFirstAvailableSelectButton();
        handlePackageSelection();
        try { Thread.sleep(4000); } catch (Exception e) {}
        boolean isRoundTrip = !driver.findElements(org.openqa.selenium.By.xpath("//div[contains(text(),'Dönüş')] | //h2[contains(text(),'Dönüş')] | //span[contains(text(),'Dönüş')]")).isEmpty();
        if (isRoundTrip) {
            clickFirstAvailableSelectButton();
            handlePackageSelection();
        }
    }

    private void clickFirstAvailableSelectButton() {
        try {
            List<WebElement> chooseButtons = driver.findElements(org.openqa.selenium.By.cssSelector(".action-select-btn:not(.provider-select-btn), [data-testid='flightInfoChoose']"));
            for(WebElement btn : chooseButtons) {
                if(btn.isDisplayed()) {
                    forceClickViaJS(btn);
                    break;
                }
            }
        } catch (Exception e) {}
    }

    private boolean handlePackageSelection() {
        try {
            String[] selectors = { ".provider-select-btn", ".btn-choose", "//button[contains(., 'Seç')]" };
            for (String selector : selectors) {
                List<WebElement> buttons = selector.startsWith("//") ? driver.findElements(By.xpath(selector)) : driver.findElements(By.cssSelector(selector));
                for (WebElement btn : buttons) {
                    if (btn.isDisplayed()) {
                        forceClickViaJS(btn);
                        return true;
                    }
                }
            }
        } catch (Exception e) {}
        return false;
    }
}
