package Listeners;

import Utils.ConfigReader;
import Utils.Driver;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Testng Listener sınıfı. Test sonuçlarını izler, rapor oluşturur ve hata durumunda ekran görüntüsü alır.
 */
public class TestListener implements ITestListener {
    private static ExtentReports extent;
    private static ThreadLocal<ExtentTest> test = new ThreadLocal<>();

    /**
     * Test suite'i başladığında raporlama mekanizmasını hazırlar.
     */
    @Override
    public void onStart(ITestContext context) {
        String dateName = new SimpleDateFormat("yyyyMMddhhmmss").format(new Date());
        String reportPath = "test-output/ExtentReport_" + dateName + ".html";
        ExtentSparkReporter spark = new ExtentSparkReporter(reportPath);
        spark.config().setDocumentTitle("Enuygun Test Otomasyon Raporu");
        spark.config().setReportName("Uçuş Arama Fonksiyonel Testleri");

        extent = new ExtentReports();
        extent.attachReporter(spark);
        extent.setSystemInfo("İşletim Sistemi", System.getProperty("os.name"));
        extent.setSystemInfo("Tarayıcı", ConfigReader.getProperty("browser"));
    }

    /**
     * Her bir test metodu başladığında çağrılır.
     */
    @Override
    public void onTestStart(ITestResult result) {
        ExtentTest extentTest = extent.createTest(result.getMethod().getMethodName());
        test.set(extentTest);
    }

    /**
     * Test başarıyla tamamlandığında çağrılır.
     */
    @Override
    public void onTestSuccess(ITestResult result) {
        test.get().log(Status.PASS, "Test Başarıyla Geçti");
    }

    /**
     * Test hata aldığında çağrılır. Ekran görüntüsü alır ve rapora ekler.
     */
    @Override
    public void onTestFailure(ITestResult result) {
        test.get().log(Status.FAIL, "Test Başarısız Oldu");
        test.get().log(Status.FAIL, result.getThrowable());

        // Ekran görüntüsü yakalama
        try {
            if (Driver.getDriver() != null) {
                File src = ((TakesScreenshot) Driver.getDriver()).getScreenshotAs(OutputType.FILE);
                String dateName = new SimpleDateFormat("yyyyMMddhhmmss").format(new Date());
                String relativeDest = "test-output/Screenshots/" + result.getName() + "_" + dateName + ".png";
                File finalDestination = new File(relativeDest);
                Files.createDirectories(finalDestination.getParentFile().toPath());
                Files.copy(src.toPath(), finalDestination.toPath());
                test.get().addScreenCaptureFromPath("Screenshots/" + result.getName() + "_" + dateName + ".png");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Test suite'i tamamlandığında raporu kapatır ve dosyaya yazar.
     */
    @Override
    public void onFinish(ITestContext context) {
        if (extent != null) {
            extent.flush();
        }
    }
}
