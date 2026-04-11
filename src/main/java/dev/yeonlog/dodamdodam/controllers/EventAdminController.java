package dev.yeonlog.dodamdodam.controllers;

import dev.yeonlog.dodamdodam.services.admin.EventAdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping(value = "admin")
@RequiredArgsConstructor
public class EventAdminController {
    private final EventAdminService eventAdminService;

    @RequestMapping(value = "/event-manage", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String eventManage(@RequestParam(defaultValue = "1") int page, Model model) {
        model.addAttribute("data", eventAdminService.getEventList(page));
        model.addAttribute("currentMenu", "event-manage");

        return "admin/admin-page";
    }

    @RequestMapping(value = "/events/{id}/status", method = RequestMethod.POST, produces = MediaType.TEXT_HTML_VALUE)
    public String updateStatus(@PathVariable Long id, @RequestParam String status, @RequestParam(defaultValue = "1") int page, RedirectAttributes redirectAttributes) {
        eventAdminService.updateStatus(id, status);
        redirectAttributes.addAttribute("page", page);
        return "redirect:/admin/event-manage";
    }

    @RequestMapping(value = "/events/{id}/delete", method = RequestMethod.POST, produces = MediaType.TEXT_HTML_VALUE)
    public String deleteEvent(@PathVariable Long id, @RequestParam(defaultValue = "1") int page, RedirectAttributes redirectAttributes) {
        eventAdminService.deleteEvent(id);
        redirectAttributes.addAttribute("page", page);
        return "redirect:/admin/event-manage";
    }
}
