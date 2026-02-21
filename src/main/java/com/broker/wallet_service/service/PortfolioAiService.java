package com.broker.wallet_service.service;

import com.broker.wallet_service.DTOS.PortfolioReportDto;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Service
public class PortfolioAiService {
    private final AiService aiService;

    public PortfolioAiService(AiService aiService) {
        this.aiService = aiService;
    }

    public Map<String,Object> generatePortfolioSummary(PortfolioReportDto report) {

        String prompt = buildPrompt(report);

        // Analiza con IA y devuelve texto JSON
        String jsonResponse = aiService.analyzeTerm(prompt);
        jsonResponse = cleanJson(jsonResponse);
        // 👇 ver respuesta real de Gemini
        System.out.println("===== GEMINI RAW RESPONSE =====");
        System.out.println(jsonResponse);
        System.out.println("================================");

        // Convertimos el JSON devuelto a Map
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(jsonResponse, new TypeReference<Map<String,Object>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Error parsing AI JSON response", e);
        }
    }
    private String buildPrompt(PortfolioReportDto report) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are a financial assistant. Analyze the evolution of the portfolio over the period, ")
                .append("considering all movements (incorporations, increments, decrements, removals) and cash adjustments. ")
                .append("Compare performance to benchmarks and evaluate how the portfolio has changed. ")
                .append("Return a JSON with 4 sections, each section must be just a string: ")
                .append("1) portfolioOverview, 2) assetAllocation, 3) investorProfile, 4) portfolioTrendAnalysis. ")
                .append("AssetAllocation should be broken down by country and by category. ")
                .append("PortfolioTrendAnalysis should describe: ")
                .append("the direction the investor is moving, which sectors or markets they are favoring, ")
                .append("whether they are increasing or reducing exposure, and the risks and potential benefits of these changes. ")
                .append("Be concise and provide brief observations. ")
                .append("PortfolioReport: ")
                .append("NetWorthCurrent: ").append(report.getNetWorthCurrent()).append(", ")
                .append("NetWorthSnapshot: ").append(report.getNetWorthSnapshot()).append(", ")
                .append("CashAdjustment: ").append(report.getCashAdjustment()).append(", ")
                .append("Movements: ").append(report.getIncorporations()).append(report.getIncrements())
                .append(report.getDecrements()).append(report.getRemovals())
                .append(", CurrentAssets: ").append(report.getCurrentAssets())
                .append(", Benchmarks: ").append(report.getBenchmarks())
                .append(". Return a valid JSON only, without extra text. Include percentages, country and category breakdown, evolution analysis, investor profile, and portfolio trend analysis.");
        return sb.toString();
    }
    private String cleanJson(String text){
        if(text == null) return null;

        // quita ```json ```
        text = text.replace("```json", "")
                .replace("```", "")
                .trim();

        return text;
    }
}
