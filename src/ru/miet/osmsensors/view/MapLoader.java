package ru.miet.osmsensors.view;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class MapLoader {

    private static String PBF_URL =
            "https://download.openstreetmap.fr/extracts/russia/central_federal_district/moscow-latest.osm.pbf";

    private Path dataDir;
    private File resultFile;

    public MapLoader() {
        dataDir = Paths.get("data");
        resultFile = dataDir.resolve("moscow.osm.pbf").toFile();
    }

    public void load() {
        if (Files.exists(resultFile.toPath())) {
            System.out.println("Файл уже существует: " + resultFile.getAbsolutePath());
            return;
        }

        try {
            Files.createDirectories(dataDir);

            URL url = URI.create(PBF_URL).toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(60000);
            connection.setRequestProperty("User-Agent", "OsmsensorsProject/1.0 (skeletoneyd@gmail.com)");

            int code = connection.getResponseCode();
            if (code != HttpURLConnection.HTTP_OK) {
                System.err.println("Сервер вернул HTTP " + code + " для " + PBF_URL);
                connection.disconnect();
                return;
            }

            Path tmpFile = dataDir.resolve("moscow.osm.pbf.tmp");

            try (InputStream in = connection.getInputStream()) {
                long size = connection.getContentLengthLong();
                System.out.println("Скачивание начато. Размер: " + (size / 1024 / 1024) + " МБ");
                Files.copy(in, tmpFile, StandardCopyOption.REPLACE_EXISTING);
            } finally {
                connection.disconnect();
            }

            Files.move(tmpFile, resultFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Готово: " + resultFile.getAbsolutePath());

        } catch (IOException e) {
            System.err.println("Ошибка загрузки PBF: " + e.getMessage());
            try {
                Files.deleteIfExists(dataDir.resolve("moscow.osm.pbf.tmp"));
            } catch (IOException e2) {}
        }
    }
}