package org.example.servicies;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Сервис для генерации автотестов через Ollama API
 */
@Service
public class OllamaService {

    private static final String OLLAMA_URL = "http://localhost:11434/api/chat";
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Генерирует Java-код теста по промпту пользователя
     *
     * @param userPrompt — описание, которое вводит пользователь
     * @param framework  — фреймворк, который нужно использовать (например, Selenide, Playwright, RestAssured)
     * @return сгенерированный Java-код или сообщение об ошибке
     */
    public String generateTest(String userPrompt, String framework) {
        try {
            // 1. Экранируем пользовательский промпт
            String safeUserPrompt = escapeJsonString(userPrompt);

            // 2. Системный промпт с подстановкой фреймворка
            String systemPrompt = """
                Вы — Senior QA Automation Engineer.
                Генерируйте чистые и понятные автотесты на Java с использованием %s фреймворка.
                Строго следуйте этим правилам:

                1. Используйте аннотации JUnit 5 и Allure TestOps.
                2. Используйте методы фреймворка, соответствующие выбранной технологии.
                3. Добавляйте комментарии, объясняющие, что делает тест.
                4. Форматируйте код с правильными отступами и разрывами строк.
                5. Возвращайте ТОЛЬКО Java-код — без объяснений и лишнего текста.
                6. Написанный тест также описывайте в формате cucumber scenario

                Пример формата:
                import org.junit.jupiter.api.*;
                import io.qameta.allure.*;
                import static com.codeborne.selenide.Selenide.*;

                @Epic("UI Тесты")
                @Feature("Проверка страницы")
                public class GeneratedTest {
            

                    @Test
                    @Story("Проверка заголовка")
                    void testExample() {
                        // Проверка заголовка
                        Assert.assertEquals(title(), "Example Domain");
                    }
                }
                """;

            // 3. Подставляем фреймворк в системный промпт и экранируем его
            String formattedSystemPrompt = String.format(systemPrompt, framework);
            String safeSystemPrompt = escapeJsonString(formattedSystemPrompt);

            // 4. Формируем JSON-тело запроса
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

            // 5. Отправляем запрос к Ollama
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(OLLAMA_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // 6. Парсим ответ
            JsonNode jsonResponse = objectMapper.readTree(response.body());

            if (jsonResponse.has("message")) {
                JsonNode messageNode = jsonResponse.get("message");
                if (messageNode.has("content")) {
                    return messageNode.get("content").asText();
                } else {
                    return "// Поле 'content' отсутствует в ответе:\n" + messageNode.toString();
                }
            } else {
                return "// Поле 'message' отсутствует в ответе:\n" + response.body();
            }

        } catch (Exception e) {
            return "// Ошибка при генерации теста\n// " + e.getMessage();
        }
    }

    /**
     * Экранирует спецсимволы в строке для безопасной передачи в JSON
     */
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