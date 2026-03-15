package Utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Yapılandırma dosyasını (config.properties) okumak için kullanılan yardımcı sınıf.
 */
public class ConfigReader {
    private static Properties properties;

    static {
        try {
            // Yapılandırma dosyasının yolu
            String path = "src/main/resources/config.properties";
            FileInputStream input = new FileInputStream(path);
            properties = new Properties();
            properties.load(input);
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Yapılandırma dosyası bulunamadı: src/main/resources/config.properties");
        }
    }

    /**
     * Anahtar kelimeye karşılık gelen yapılandırma değerini döndürür.
     * @param key Özellik anahtarı (Örn: browser, url)
     * @return Özellik değeri
     */
    public static String getProperty(String key) {
        return properties.getProperty(key);
    }
}
