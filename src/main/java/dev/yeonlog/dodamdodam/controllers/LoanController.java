package dev.yeonlog.dodamdodam.controllers;

import dev.yeonlog.dodamdodam.entities.BookCopyEntity;
import dev.yeonlog.dodamdodam.entities.LoanEntity;
import dev.yeonlog.dodamdodam.mappers.BookMapper;
import dev.yeonlog.dodamdodam.mappers.LoanMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class LoanController {
    private final LoanMapper loanMapper;
    private final BookMapper bookMapper;

    // 예약 신청
    @RequestMapping(value = "/reserve", method = RequestMethod.POST)
    public String reserve(@RequestParam long bookId,
                          @RequestParam(defaultValue = "/search") String redirect,
                          @AuthenticationPrincipal UserDetails userDetails,
                          RedirectAttributes redirectAttributes) {
        // 로그인 확인
        if (userDetails == null) {
            return "redirect:/login";
        }

        // 중복 예약 체크
        int duplicateCount = loanMapper.countPendingOrActiveLoan(userDetails.getUsername(), bookId);
        if (duplicateCount > 0) {
            redirectAttributes.addFlashAttribute("errorMsg", "이미 예약하거나 대출 중인 도서예요.");
            return "redirect:" + redirect;
        }

        // 대출 가능한 사본 찾기
        BookCopyEntity availableCopy = bookMapper.selectAvailableCopy(bookId);
        if (availableCopy == null) {
            redirectAttributes.addFlashAttribute("errorMsg", "대출 가능한 사본이 없어요.");
            return "redirect:/search";
        }

        // 예약 등록
        LoanEntity loan = LoanEntity.builder()
                .userId(userDetails.getUsername())
                .copyId(availableCopy.getId())
                .status("PENDING")
                .build();
        loanMapper.insertLoan(loan);

        redirectAttributes.addFlashAttribute("successMsg", "예약이 완료됐어요! 관리자 승인 후 대출이 시작 돼요.");
        return "redirect:" + redirect;
    }
}
