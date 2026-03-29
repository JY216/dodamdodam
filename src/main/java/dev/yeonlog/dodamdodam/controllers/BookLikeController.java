package dev.yeonlog.dodamdodam.controllers;

import dev.yeonlog.dodamdodam.mappers.BookLikeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class BookLikeController {
    private final BookLikeMapper bookLikeMapper;

    @RequestMapping(value = "/books/like", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity<?> toggleLike(@RequestParam Long bookId,
                                        @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("message", "로그인이 필요해요."));
        }

        String userId = userDetails.getUsername();
        int count = bookLikeMapper.countLike(userId, bookId);

        if (count > 0) {
            bookLikeMapper.deleteLike(userId, bookId);
            return ResponseEntity.ok(Map.of("liked", false));
        } else {
            bookLikeMapper.insertLike(userId, bookId);
            return ResponseEntity.ok(Map.of("liked", true));
        }
    }

    @RequestMapping(value = "/books/unlike", method = RequestMethod.POST)
    public String unlike(@RequestParam Long bookId,
                         @RequestParam(defaultValue = "/mypage/likes") String redirect,
                         @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return "redirect:/login";
        bookLikeMapper.deleteLike(userDetails.getUsername(), bookId);
        return "redirect:" + redirect;
    }
}