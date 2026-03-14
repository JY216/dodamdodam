package dev.yeonlog.dodamdodam.controllers;

import dev.yeonlog.dodamdodam.dtos.ChatRequest;
import dev.yeonlog.dodamdodam.services.GeminiServices.ChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
@RequestMapping("/ai")
@RequiredArgsConstructor
public class GeminiController {

    private final ChatbotService chatbotService;

    @RequestMapping(value = "/chatbot", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String chatbotPage() {
        return "ai/chatbot";
    }

    @RequestMapping(value = "/chat", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Map<String, String>> chat(@RequestBody ChatRequest request) {
        String reply = chatbotService.chat(request.getMessage());
        return ResponseEntity.ok(Map.of("reply", reply));
    }
}
