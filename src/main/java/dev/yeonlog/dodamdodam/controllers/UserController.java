package dev.yeonlog.dodamdodam.controllers;

import dev.yeonlog.dodamdodam.entities.*;
import dev.yeonlog.dodamdodam.mappers.*;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import dev.yeonlog.dodamdodam.services.UserService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserMapper userMapper;
    private final LoanMapper loanMapper;
    private final BookLikeMapper bookLikeMapper;
    private final WishBookMapper wishBookMapper;
    private final EventMapper eventMapper;

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

    // 마이 페이지
    @RequestMapping(value = "/mypage", method = RequestMethod.GET)
    public String mypage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) return "redirect:/login";

        String userId = userDetails.getUsername();
        List<LoanEntity> loans = loanMapper.selectLoansByUserId(userId);

        long loanCount = loans.stream().filter(l -> l.getStatus().equals("LOANED")).count();
        long pendingCount = loans.stream().filter(l -> l.getStatus().equals("PENDING")).count();
        long likeCount = bookLikeMapper.selectLikedBookIds(userId).size();
        long wishCount = wishBookMapper.selectByUserId(userId).size();
        long eventCount = eventMapper.selectApplicationsByUserId(userId).size();

        // 유저 이름 가져오기
        UserEntity user = userMapper.selectByUserId(userId);

        model.addAttribute("userName", user.getName());
        model.addAttribute("loanCount", loanCount);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("likeCount", likeCount);
        model.addAttribute("wishCount", wishCount);
        model.addAttribute("eventCount", eventCount);

        return "user/mypage/mypage";
    }

    // 도서 대출 현황
    @RequestMapping(value = "/mypage/loans", method = RequestMethod.GET)
    public String myLoans(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) return "redirect:/login";

        String userId = userDetails.getUsername();
        UserEntity user = userMapper.selectByUserId(userId);
        List<LoanEntity> loans = loanMapper.selectLoansByUserId(userId);

        // D-day 계산
        loans.forEach(loan -> {
            if (loan.getDueDate() != null && loan.getStatus().equals("LOANED")) {
                long diff = java.time.temporal.ChronoUnit.DAYS.between(
                        java.time.LocalDate.now(), loan.getDueDate()
                );
                loan.setDday(diff);
            }
        });

        model.addAttribute("userName", user.getName());
        model.addAttribute("loans", loans);

        return "user/mypage/mypage-loans";
    }

    // 도서 예약 현황
    @RequestMapping(value = "/mypage/reservations", method = RequestMethod.GET)
    public String myReservations(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) return "redirect:/login";

        String userId = userDetails.getUsername();
        UserEntity user = userMapper.selectByUserId(userId);
        List<LoanEntity> loans = loanMapper.selectLoansByUserId(userId);

        model.addAttribute("userName", user.getName());
        model.addAttribute("loans", loans);

        return "user/mypage/mypage-reservations";
    }

    // 예약 취소
    @RequestMapping(value = "/mypage/reservations/cancel", method = RequestMethod.POST)
    public String cancelReservation(@RequestParam long loanId,
                                    RedirectAttributes redirectAttributes) {
        loanMapper.cancelLoan(loanId);
        redirectAttributes.addFlashAttribute("successMsg", "예약이 취소됐어요.");
        return "redirect:/mypage/reservations";
    }

    @RequestMapping(value = "/mypage/likes", method = RequestMethod.GET)
    public String myLikes(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) return "redirect:/login";

        String userId = userDetails.getUsername();
        UserEntity user = userMapper.selectByUserId(userId);
        List<BookEntity> likedBooks = bookLikeMapper.selectLikedBooks(userId);

        model.addAttribute("userName", user.getName());
        model.addAttribute("likedBooks", likedBooks);

        return "user/mypage/mypage-likes";
    }

    @RequestMapping(value = "/mypage/wish-books", method = RequestMethod.GET)
    public String myWishBooks(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) return "redirect:/login";

        String userId = userDetails.getUsername();
        UserEntity user = userMapper.selectByUserId(userId);
        List<WishBookEntity> wishBooks = wishBookMapper.selectByUserId(userId);

        model.addAttribute("userName", user.getName());
        model.addAttribute("wishBooks", wishBooks);

        return "user/mypage/mypage-wish-books";
    }

    // 아이디/비밀번호 찾기 페이지
    @RequestMapping(value = "/find-account", method = RequestMethod.GET)
    public String getFindAccount() {
        return "user/find-account";
    }

    // 아이디 찾기
    @RequestMapping(value = "/find-id", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> findId(@RequestParam String email) {
        Map<String, Object> response = new HashMap<>();
        String userId = userService.findUserId(email);
        if (userId != null) {
            response.put("result", "SUCCESS");
            response.put("userId", userId);
        } else {
            response.put("result", "FAILURE");
        }
        return response;
    }

    // 비밀번호 재설정 인증번호 전송
    @RequestMapping(value = "/reset-password/email", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> sendResetEmail(@RequestParam String email) {
        Map<String, Object> response = new HashMap<>();
        try {
            EmailTokenEntity token = userService.sendPasswordResetEmail(email);
            if (token == null) {
                response.put("result", "FAILURE");
                return response;
            }
            response.put("result", "SUCCESS");
            response.put("salt", token.getSalt());
        } catch (MessagingException e) {
            response.put("result", "FAILURE");
        }
        return response;
    }

    // 비밀번호 재설정 인증번호 확인
    @RequestMapping(value = "/reset-password/verify", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> verifyResetEmail(@RequestParam String email,
                                                @RequestParam String code,
                                                @RequestParam String salt) {
        Map<String, Object> response = new HashMap<>();
        boolean result = userService.verifyEmail(email, code, salt);
        response.put("result", result ? "SUCCESS" : "FAILURE");
        return response;
    }

    // 비밀번호 재설정
    @RequestMapping(value = "/reset-password", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Map<String, Object> resetPassword(@RequestParam String email,
                                             @RequestParam String code,
                                             @RequestParam String salt,
                                             @RequestParam String newPassword) {
        Map<String, Object> response = new HashMap<>();
        boolean result = userService.resetPassword(email, code, salt, newPassword);
        response.put("result", result ? "SUCCESS" : "FAILURE");
        return response;
    }

    @RequestMapping(value = "/mypage/events", method = RequestMethod.GET)
    public String myEvents(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) return "redirect:/login";

        String userId = userDetails.getUsername();
        UserEntity user = userMapper.selectByUserId(userId);
        List<EventApplicationEntity> applications = eventMapper.selectApplicationsByUserId(userId);

        model.addAttribute("userName", user.getName());
        model.addAttribute("applications", applications);

        return "user/mypage/mypage-events";
    }
}
