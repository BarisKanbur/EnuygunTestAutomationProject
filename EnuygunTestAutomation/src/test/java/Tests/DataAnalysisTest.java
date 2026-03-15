package Tests;

import Models.FlightData;
import Pages.EnuygunHomePage;
import Pages.FlightListPage;
import Utils.ConfigReader;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import org.knowm.xchart.*;
import org.knowm.xchart.style.Styler;
import org.testng.annotations.Test;

import java.io.FileWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Case 4: Veri çekme, CSV aktarımı ve istatistiksel analiz test sınıfı.
 */
public class DataAnalysisTest extends BaseTest {

    @org.testng.annotations.DataProvider(name = "flightRoutes")
    public Object[][] flightRoutes() {
        return new Object[][] {
            { "Istanbul", "Lefkoşa", "default" },
            { "Ankara", "Lefkoşa", "default" }
        };
    }

    /**
     * Uçuş verilerini çeker, analiz eder ve görselleştirir.
     * @param from Kalkış şehri
     * @param to Varış şehri
     * @param date Tarih (default veya belirli bir tarih)
     */
    @Test(description = "Case 4: Uçuş Verisi Çıkarma ve Analiz", dataProvider = "flightRoutes")
    public void testFlightDataAnalysis(String from, String to, String date) throws Exception {
        EnuygunHomePage homePage = new EnuygunHomePage();
        
        // 1. Arama Yap ve Verileri Çek
        homePage.goToHomePage(ConfigReader.getProperty("url"));
        homePage.acceptCookies();
        
        FlightListPage flightListPage;
        if ("default".equals(date)) {
            flightListPage = homePage.searchFlightByUI(from, to);
        } else {
            flightListPage = homePage.searchFlightDirectly(from, to, date, null);
        }
        
        // Tüm sonuçların gelmesi için bekliyoruz
        Thread.sleep(10000);
        List<FlightData> flights = flightListPage.getAllFlightData();
        
        if (flights.isEmpty()) {
            logger.error(String.format("%s - %s rotası için uçuş verisi çekilemedi!", from, to));
            return;
        }

        // Çıktı klasörü (Rotaya ve tarihe göre ayrıştırılmış)
        String outputDir = String.format("target/analysis/%s_%s/", from, to).replace(" ", "_");
        Files.createDirectories(Paths.get(outputDir));

        // 2. CSV Dosyasına Kaydet
        saveToCSV(flights, outputDir + "flight_results.csv");
        logger.info(String.format("Uçuş verileri '%sflight_results.csv' dosyasına kaydedildi.", outputDir));

        // 3. Veri Analizi (Havayolu Bazlı Min, Max, Ort Fiyatlar)
        Map<String, List<FlightData>> byAirline = flights.stream()
                .collect(Collectors.groupingBy(FlightData::getAirline));

        List<String> airlineNames = new ArrayList<>();
        List<Double> minPrices = new ArrayList<>();
        List<Double> maxPrices = new ArrayList<>();
        List<Double> avgPrices = new ArrayList<>();

        for (Map.Entry<String, List<FlightData>> entry : byAirline.entrySet()) {
            String airline = entry.getKey();
            List<FlightData> airlineFlights = entry.getValue();
            
            double min = airlineFlights.stream().mapToDouble(FlightData::getPrice).min().orElse(0);
            double max = airlineFlights.stream().mapToDouble(FlightData::getPrice).max().orElse(0);
            double avg = airlineFlights.stream().mapToDouble(FlightData::getPrice).average().orElse(0);
            
            airlineNames.add(airline);
            minPrices.add(min);
            maxPrices.add(max);
            avgPrices.add(avg);
        }

        // 4. Görselleştirme - Havayolu Bazlı Fiyatlar (Bar Chart)
        CategoryChart chart = new CategoryChartBuilder().width(800).height(600).title(from + "-" + to + " Fiyat Analizi").xAxisTitle("Havayolu").yAxisTitle("Fiyat (TL)").build();
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
        chart.addSeries("Min Fiyat", airlineNames, minPrices);
        chart.addSeries("Max Fiyat", airlineNames, maxPrices);
        chart.addSeries("Ortalama Fiyat", airlineNames, avgPrices);
        BitmapEncoder.saveBitmap(chart, outputDir + "airline_price_analysis", BitmapEncoder.BitmapFormat.PNG);

        // 5. Zaman Dilimlerine Göre Fiyat Dağılımı (Heat Map)
        int[] xHours = new int[24];
        for (int i = 0; i < 24; i++) xHours[i] = i;
        int[] yPriceBrackets = new int[]{1000, 2000, 3000, 4000, 5000, 6000, 7000, 8000, 9000, 10000};
        int[][] heatData = new int[24][10];

        for (FlightData f : flights) {
            try {
                int hour = Integer.parseInt(f.getDepartureTime().split(":")[0]);
                int priceBracket = (int) (f.getPrice() / 1000);
                if (priceBracket > 9) priceBracket = 9;
                heatData[hour][priceBracket]++;
            } catch (Exception e) {}
        }

        HeatMapChart timeChart = new HeatMapChartBuilder().width(800).height(600).title(from + "-" + to + " Zaman-Fiyat Isı Haritası").xAxisTitle("Saat").yAxisTitle("Fiyat Aralığı").build();
        timeChart.addSeries("Uçuş Yoğunluğu", xHours, yPriceBrackets, heatData);
        BitmapEncoder.saveBitmap(timeChart, outputDir + "time_price_heatmap", BitmapEncoder.BitmapFormat.PNG);

        // 6. Maliyet-Etkinlik Algoritması
        // Skor = Fiyat + (Aktarma Cezası) + (Süre Cezası)
        logger.info(String.format("--- %s -> %s En Maliyet-Etkin Uçuşlar ---", from, to));
        flights.stream()
               .sorted(Comparator.comparingDouble(this::calculateEffectivenessScore))
               .limit(5)
               .forEach(f -> logger.info(String.format("Skor: %.2f | %s", calculateEffectivenessScore(f), f)));
    }

    /**
     * Maliyet-etkinlik skorunu hesaplar. Düşük skor daha iyidir.
     */
    private double calculateEffectivenessScore(FlightData flight) {
        double score = flight.getPrice();
        
        // Aktarma Cezası (Direkt uçuşlar öncelikli)
        if (!flight.getConnection().contains("Direkt")) {
            score += 1000; // Aktarma için 1000 TL sanal ceza
        }
        
        // Süre Cezası (Dakika başına 5 TL sanal ceza)
        try {
            String duration = flight.getDuration();
            int totalMinutes = 0;
            if (duration.contains("sa")) {
                totalMinutes += Integer.parseInt(duration.split("sa")[0].trim()) * 60;
                if (duration.contains("dk")) {
                    totalMinutes += Integer.parseInt(duration.split("sa")[1].replace("dk", "").trim());
                }
            } else if (duration.contains("dk")) {
                totalMinutes += Integer.parseInt(duration.replace("dk", "").trim());
            }
            score += totalMinutes * 5;
        } catch (Exception e) {
            score += 2000; // Süre okunamazsa ceza
        }
        
        return score;
    }

    /**
     * Uçuş verilerini CSV dosyasına yazar.
     */
    private void saveToCSV(List<FlightData> data, String filePath) throws Exception {
        try (Writer writer = new FileWriter(filePath)) {
            StatefulBeanToCsv<FlightData> beanToCsv = new StatefulBeanToCsvBuilder<FlightData>(writer).build();
            beanToCsv.write(data);
        }
    }
}
