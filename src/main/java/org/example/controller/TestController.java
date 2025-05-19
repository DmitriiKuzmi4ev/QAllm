package org.example.controller;

import org.example.servicies.OllamaService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class TestController {

    private final OllamaService ollamaService;

    public TestController(OllamaService ollamaService) {
        this.ollamaService = ollamaService;
    }

    @GetMapping("/")
    public String showForm(Model model) {
        model.addAttribute("request", new TestRequest());
        return "index"; // имя Thymeleaf шаблона
    }

    @PostMapping("/generate")
    public String generateTest(@ModelAttribute TestRequest request, Model model) {
        System.out.println("Получен промпт: " + request.getPrompt());

        // Определение фреймворка
        String selectedFramework = "UI".equals(request.getTestType())
                ? request.getFramework()
                : request.getApiFramework();

        // Генерация теста с учётом выбранного языка
        String generatedCode = ollamaService.generateTestWithLanguage(
                request.getPrompt(), selectedFramework, request.getLanguage()
        );

        model.addAttribute("generatedCode", generatedCode);
        return "index";
    }
}