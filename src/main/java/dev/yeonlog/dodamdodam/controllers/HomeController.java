package dev.yeonlog.dodamdodam.controllers;

import dev.yeonlog.dodamdodam.entities.EventEntity;
import dev.yeonlog.dodamdodam.entities.LoanEntity;
import dev.yeonlog.dodamdodam.mappers.EventMapper;
import dev.yeonlog.dodamdodam.mappers.LoanMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.CharUtils;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final LoanMapper loanMapper;
    private final EventMapper eventMapper;

    @RequestMapping(value = "/")
    public String getHome(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails != null) {
            List<LoanEntity> loans = loanMapper.selectLoansByUserId(userDetails.getUsername());
            List<LoanEntity> activeLoans = loans.stream()
                    .filter(l ->  "LOANED".equals(l.getStatus()))
                    .toList();

            for (LoanEntity loan : activeLoans) {
                if (loan.getDueDate() != null) {
                    long dday = ChronoUnit.DAYS.between(LocalDate.now(), loan.getDueDate());
                    loan.setDday(dday);
                }
            }
            model.addAttribute("activeLoans", activeLoans);
        }

        // 문화행사 목록
        List<EventEntity> events = eventMapper.selectAllEvents();
        model.addAttribute("events", events);

        return "home/home";
    }
}
