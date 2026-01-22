package server.core;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LogService {

    private final String filePath;

    public LogService(String filePath) {
        this.filePath = filePath;
    }

    public synchronized void log(String type, String message) {
        try (PrintWriter out = new PrintWriter(new FileWriter(filePath, true))) {
            out.println(LocalDateTime.now() + " | " + type + " | " + message);
        } catch (Exception ignored) { }
    }

    public synchronized List<String> tailLines(int maxLines) {
        try {
            Path p = Path.of(filePath);
            if (!Files.exists(p)) return new ArrayList<>();

            List<String> all = Files.readAllLines(p, StandardCharsets.UTF_8);
            if (all.isEmpty()) return new ArrayList<>();

            int from = Math.max(0, all.size() - Math.max(1, maxLines));
            return new ArrayList<>(all.subList(from, all.size()));
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public String getFilePath() {
        return filePath;
    }
}
