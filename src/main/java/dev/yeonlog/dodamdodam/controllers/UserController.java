package dev.yeonlog.dodamdodam.controllers;

import dev.yeonlog.dodamdodam.entities.EmailTokenEntity;
import dev.yeonlog.dodamdodam.entities.UserEntity;
import dev.yeonlog.dodamdodam.services.UserService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String getLogin() {
        return "user/login";
    }

    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public String getRegister() {
        return "user/register";
    }

    // 이메일 인증번호 전송
    @RequestMapping(value = "/register/email", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> postRegisterEmail(@RequestParam(value = "email", required = false) String email) {
        Map<String, Object> response = new HashMap<>();
        try {
            EmailTokenEntity emailToken = userService.sendEmail(email);
            if (emailToken == null) {
                response.put("result", "FAILURE");
                return response;
            }
            response.put("result", "SUCCESS");
            response.put("salt", emailToken.getSalt());
        } catch (MessagingException e) {
            response.put("result", "FAILURE");
        }
        return response;
    }

    // 이메일 인증번호 확인
    @RequestMapping(value = "/register/email", method = RequestMethod.PATCH, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> patchRegisterEmail(@RequestParam(value = "email", required = false) String email,
                                                  @RequestParam(value = "code", required = false) String code,
                                                  @RequestParam(value = "salt", required = false) String salt) {
        Map<String, Object> response = new HashMap<>();
        boolean result = userService.verifyEmail(email, code, salt);
        response.put("result", result? "SUCCESS" : "FAILURE");
        return response;
    }

    // 회원가입
    @RequestMapping(value = "/register", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> postRegister(UserEntity user, @RequestParam(value = "email", required = false) String email,
                                            @RequestParam(value = "code", required = false) String code,
                                            @RequestParam(value = "salt", required = false) String salt) {
        Map<String, Object> response = new HashMap<>();
        boolean result = userService.register(user, email, code, salt);
        response.put("result", result ? "SUCCESS" : "FAILURE");
        return response;
    }

    // 로그인
    @RequestMapping(value = "/login", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody Map<String, Object> postLogin(@RequestParam(value = "userId", required = false) String userId,
                                                @RequestParam(value = "password", required = false) String password,
                                                HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        UserEntity user = userService.login(userId, password);
        if (user != null) {
            session.setAttribute("sessionUser", user);
            response.put("result", "SUCCESS");
        } else {
            response.put("result", "FAILURE");
        }
        return response;
    }
}
