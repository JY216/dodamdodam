package dev.yeonlog.dodamdodam.controllers;

import dev.yeonlog.dodamdodam.mappers.DashboardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("admin")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardMapper dashboardMapper;

    @RequestMapping(value = "dashboard", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String dashboard(Model model) {
        // 회원
        model.addAttribute("totalUsers",     dashboardMapper.countAllUsers());
        model.addAttribute("todayUsers",     dashboardMapper.countTodayUsers());
        model.addAttribute("monthlyUsers",   dashboardMapper.countMonthlyUsers());
        model.addAttribute("suspendedUsers", dashboardMapper.countSuspendedUsers());

        // 도서
        model.addAttribute("totalBooks",     dashboardMapper.countAllBooks());
        model.addAttribute("newBooks",       dashboardMapper.countNewBooks());

        // 대출 / 행사
        model.addAttribute("activeLoans",    dashboardMapper.countActiveLoans());
        model.addAttribute("activeEvents",   dashboardMapper.countActiveEvents());

        model.addAttribute("currentMenu", "dashboard");
        return "admin/admin-page";
    }
}
