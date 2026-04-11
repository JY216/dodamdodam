package dev.yeonlog.dodamdodam.controllers;

import dev.yeonlog.dodamdodam.services.AiRecommendService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("ai")
@RequiredArgsConstructor
public class AiRecommendController {
    private final AiRecommendService aiRecommendService;

    @RequestMapping(value = "/recommend", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> recommend(@AuthenticationPrincipal UserDetails userDetails) {
        try {
            String result;
            if (userDetails != null) {
                result = aiRecommendService.getGeneralRecommendation();
            } else {
                result = aiRecommendService.getGeneralRecommendation();
            }

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("[]");
        }
    }
}
