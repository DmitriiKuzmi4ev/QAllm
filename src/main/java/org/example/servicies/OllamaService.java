package org.example.servicies;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class OllamaService {

    private static final String OLLAMA_URL = "http://localhost:11434/api/chat";
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String generateTestWithLanguage(String userPrompt, String framework, String language) {
        try {
            String safeUserPrompt = escapeJsonString(userPrompt);

            // Динамический системный промпт с подстановкой языка и фреймворка
            String systemPrompt = """
                Вы — Senior QA Automation Engineer.
                Генерируйте автотесты на %s с использованием %s.
                Строго следуйте этим правилам:
                
                1. Используйте соответствующие аннотации и библиотеки (%s).
                2. Добавляйте комментарии, объясняющие, что делает тест.
                3. Форматируйте код с отступами.
                4. Возвращайте ТОЛЬКО код — без лишнего текста.
                5. Также описывайте тест в формате cucumber scenario
                6. При создания шага теста используй allure.step(шаг, лямбда)
                
                Пример:
                import io.qameta.allure.*;
                import org.junit.jupiter.api.*;
                
                @Epic("UI Тесты")
                @Feature("Проверка страницы")
                public class GeneratedTest {
                
                    @Test
                    @Story("Проверка заголовка")
                    void testExample() {
                        Assert.assertEquals(title(), "Example Domain");
                    }
                }
                """;

            String formattedSystemPrompt = String.format(systemPrompt, language, framework, getTestingLibrary(framework, language));
            String safeSystemPrompt = escapeJsonString(formattedSystemPrompt);

            String jsonBody = String.format("""
                {
                  "model": "llama3",
                  "messages": [
                    {"role": "system", "content": "%s"},
                    {"role": "user", "content": "%s"}
                  ],
                  "stream": false
                }
                """, safeSystemPrompt, safeUserPrompt);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(OLLAMA_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JsonNode jsonResponse = objectMapper.readTree(response.body());

            if (jsonResponse.has("message")) {
                return jsonResponse.get("message").get("content").asText();
            } else {
                return "// Поле 'content' отсутствует в ответе:\n" + response.body();
            }

        } catch (Exception e) {
            return "// Ошибка при генерации теста\n// " + e.getMessage();
        }
    }

    private String getTestingLibrary(String framework, String language) {
        switch (language) {
            case "Java":
                return "JUnit 5 и Allure";
            case "Python":
                return "pytest и allure-pytest";
            case "Go":
                return "testing и testify";
            default:
                return "unknown library";
        }
    }

    private String escapeJsonString(String input) {
        if (input == null) return "";
        StringBuilder result = new StringBuilder();
        for (char c : input.toCharArray()) {
            switch (c) {
                case '\\': result.append("\\\\"); break;
                case '"':  result.append("\\\""); break;
                case '\b': result.append("\\b");  break;
                case '\f': result.append("\\f");  break;
                case '\n': result.append("\\n");  break;
                case '\r': result.append("\\r");  break;
                case '\t': result.append("\\t");  break;
                default:
                    if (c < 0x20 || c > 0x7F) {
                        result.append(String.format("\\u%04X", (int) c));
                    } else {
                        result.append(c);
                    }
                    break;
            }
        }
        return result.toString();
    }
}