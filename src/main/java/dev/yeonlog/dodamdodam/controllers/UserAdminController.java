package dev.yeonlog.dodamdodam.controllers;

import dev.yeonlog.dodamdodam.dtos.UserEventDto;
import dev.yeonlog.dodamdodam.dtos.UserLoanDto;
import dev.yeonlog.dodamdodam.dtos.UserOverdueDto;
import dev.yeonlog.dodamdodam.mappers.UserActivityMapper;
import dev.yeonlog.dodamdodam.services.admin.UserAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(value = "admin")
@RequiredArgsConstructor
public class UserAdminController {

    private final UserAdminService userAdminService;
    private final UserActivityMapper userActivityMapper;

    @RequestMapping(value = "/users", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String userList(@RequestParam(required = false) String name, @RequestParam(required = false) String birth, @RequestParam(required = false) String mobile, @RequestParam(defaultValue = "1") int page, Model model) {
        Map<String, Object> data = userAdminService.getMemberList(name, birth, mobile, page);
        model.addAttribute("data", data);
        model.addAttribute("name", name);
        model.addAttribute("birth", birth);
        model.addAttribute("mobile", mobile);
        model.addAttribute("currentMenu", "user-list");
        return "admin/admin-page";
    }

    @RequestMapping(value = "/status", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String userStatusList(@RequestParam(required = false) String name, @RequestParam(required = false) String mobile, @RequestParam(defaultValue = "1") int page, Model model) {
        Map<String, Object> data = userAdminService.getMemberList(name, null, mobile, page);
        model.addAttribute("data", data);
        model.addAttribute("name", name);
        model.addAttribute("mobile", mobile);
        model.addAttribute("currentMenu", "user-status");
        return "admin/admin-page";
    }

    @RequestMapping(value = "/users/{userId}/status", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public String changeStatus(@PathVariable String userId, @RequestParam(required = false) String name, @RequestParam(required = false) String mobile, RedirectAttributes redirectAttributes) {
        userAdminService.toggleStatus(userId);
        redirectAttributes.addAttribute("name", name);
        redirectAttributes.addAttribute("mobile", mobile);

        return "redirect:/admin/status";
    }

    // 대출 목록 + 연체 정보 AJAX
    @RequestMapping(value = "/users/{userId}/loans", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUserLoans(@PathVariable String userId) {
        List<UserLoanDto> loans = userActivityMapper.findLoansByUserId(userId);
        UserOverdueDto overdue = userActivityMapper.findOverdueByUserId(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("loans", loans);
        result.put("overdue", overdue != null ? overdue.getOverdue() : 0);
        return ResponseEntity.ok(result);
    }

    // 행사 참여 목록 AJAX
    @RequestMapping(value = "/users/{userId}/events", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<UserEventDto>> getUserEvents(@PathVariable String userId) {
        List<UserEventDto> events = userActivityMapper.findEventsByUserId(userId);
        return ResponseEntity.ok(events);
    }
}
