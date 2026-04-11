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

        // 대출 가능 권수 체크 (최대 3권)
        int activeLoansCount = loanMapper.countActiveLoansByUserId(userDetails.getUsername());
        if (activeLoansCount >= 3) {
            redirectAttributes.addFlashAttribute("errorMsg", "최대 3권까지만 대출할 수 있어요. 반납 후 다시 시도해주세요.");
            return "redirect:" + redirect;
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

    @RequestMapping(value = "/mypage/loans/extend", method = RequestMethod.POST)
    public String extendLoan(@RequestParam long loanId,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        if (userDetails == null) return "redirect:/login";

        LoanEntity loan = loanMapper.selectById(loanId);

        // 본인 대출인지 확인
        if (!loan.getUserId().equals(userDetails.getUsername())) {
            redirectAttributes.addFlashAttribute("errorMsg", "본인의 대출만 연장할 수 있어요.");
            return "redirect:/mypage/loans";
        }

        // 이미 연장한 경우
        if (loan.getExtendCount() >= 1) {
            redirectAttributes.addFlashAttribute("errorMsg", "대출 연장은 1회만 가능해요.");
            return "redirect:/mypage/loans";
        }

        // LOANED 상태인지 확인
        if (!"LOANED".equals(loan.getStatus())) {
            redirectAttributes.addFlashAttribute("errorMsg", "대출 중인 도서만 연장할 수 있어요.");
            return "redirect:/mypage/loans";
        }

        loanMapper.extendLoan(loanId);
        redirectAttributes.addFlashAttribute("successMsg", "반납 기한이 7일 연장됐어요!");
        return "redirect:/mypage/loans";
    }
}
