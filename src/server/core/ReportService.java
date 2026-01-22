package server.core;

import server.model.Product;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.*;

public class ReportService {

    private final ServerDataStore store = ServerDataStore.getInstance();

    public String branchDailyJson(int branchId) {
        Map<Integer, Integer> sold = store.getBranchSales(branchId);

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"type\":\"branchDaily\",");
        sb.append("\"date\":\"").append(LocalDate.now()).append("\",");
        sb.append("\"branchId\":").append(branchId).append(",");
        sb.append("\"items\":[");

        boolean first = true;
        for (var e : sold.entrySet()) {
            int productId = e.getKey();
            int qty = e.getValue();
            Product p = store.getProduct(productId);

            if (!first) sb.append(",");
            first = false;

            sb.append("{");
            sb.append("\"productId\":").append(productId).append(",");
            sb.append("\"name\":\"").append(escape(p != null ? p.getName() : ("product#" + productId))).append("\",");
            sb.append("\"category\":\"").append(escape(p != null ? p.getCategory() : "UNKNOWN")).append("\",");
            sb.append("\"qty\":").append(qty);
            sb.append("}");
        }

        sb.append("]}");
        return sb.toString();
    }

    public String productReportJson(int productId) {
        Product p = store.getProduct(productId);

        int total = 0;
        Map<Integer, Integer> perBranch = new HashMap<>();
        for (int branchId : store.listBranchIds()) {
            int sold = store.getBranchSales(branchId).getOrDefault(productId, 0);
            perBranch.put(branchId, sold);
            total += sold;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"type\":\"productReport\",");
        sb.append("\"productId\":").append(productId).append(",");
        sb.append("\"name\":\"").append(escape(p != null ? p.getName() : ("product#" + productId))).append("\",");
        sb.append("\"category\":\"").append(escape(p != null ? p.getCategory() : "UNKNOWN")).append("\",");
        sb.append("\"totalSold\":").append(total).append(",");
        sb.append("\"byBranch\":[");

        boolean first = true;
        for (var e : perBranch.entrySet()) {
            if (!first) sb.append(",");
            first = false;
            sb.append("{\"branchId\":").append(e.getKey()).append(",\"qty\":").append(e.getValue()).append("}");
        }

        sb.append("]}");
        return sb.toString();
    }

    public String categoryReportJson(String category) {
        String cat = category == null ? "" : category.trim();

        Map<Integer, Integer> totalsByProduct = new HashMap<>();
        for (int branchId : store.listBranchIds()) {
            Map<Integer, Integer> sold = store.getBranchSales(branchId);
            for (var e : sold.entrySet()) {
                int productId = e.getKey();
                Product p = store.getProduct(productId);
                if (p != null && p.getCategory().equalsIgnoreCase(cat)) {
                    totalsByProduct.put(productId, totalsByProduct.getOrDefault(productId, 0) + e.getValue());
                }
            }
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"type\":\"categoryReport\",");
        sb.append("\"category\":\"").append(escape(cat)).append("\",");
        sb.append("\"items\":[");

        boolean first = true;
        for (var e : totalsByProduct.entrySet()) {
            int productId = e.getKey();
            int qty = e.getValue();
            Product p = store.getProduct(productId);

            if (!first) sb.append(",");
            first = false;

            sb.append("{");
            sb.append("\"productId\":").append(productId).append(",");
            sb.append("\"name\":\"").append(escape(p != null ? p.getName() : ("product#" + productId))).append("\",");
            sb.append("\"qty\":").append(qty);
            sb.append("}");
        }

        sb.append("]}");
        return sb.toString();
    }

    public String exportJsonToWordRtf(String json, String fileName) throws Exception {
        if (fileName == null || fileName.isBlank()) fileName = "report.rtf";
        if (!fileName.toLowerCase().endsWith(".rtf")) fileName = fileName + ".rtf";

        String rtf = toRtf("Sales Report", json);
        Path out = Path.of(fileName);
        Files.write(out, rtf.getBytes(StandardCharsets.UTF_8));
        return out.toAbsolutePath().toString();
    }

    private String toRtf(String title, String body) {
        String safeBody = body.replace("\\", "\\\\").replace("{", "\\{").replace("}", "\\}");
        return "{\\rtf1\\ansi\\deff0"
                + "{\\fonttbl{\\f0 Arial;}{\\f1 Courier New;}}"
                + "\\fs28\\b " + title + "\\b0\\par"
                + "\\fs20\\f1 " + safeBody + "\\f0\\par"
                + "}";
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
