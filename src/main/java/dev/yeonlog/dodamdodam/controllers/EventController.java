package dev.yeonlog.dodamdodam.controllers;

import dev.yeonlog.dodamdodam.entities.EventApplicationEntity;
import dev.yeonlog.dodamdodam.entities.EventEntity;
import dev.yeonlog.dodamdodam.mappers.EventMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class EventController {

    private final EventMapper eventMapper;

    // 문화 행사 상세
    @RequestMapping(value = "/events/{id}", method = RequestMethod.GET)
    public String eventDetail(@PathVariable Long id,
                              @AuthenticationPrincipal UserDetails userDetails,
                              Model model) {
        EventEntity event = eventMapper.selectEventById(id);
        if (event == null) return "redirect:/";

        model.addAttribute("event", event);

        // 로그인한 경우 신청 여부 확인
        if (userDetails != null) {
            int count = eventMapper.countApplication(id, userDetails.getUsername());
            model.addAttribute("isApplied", count > 0);
        } else {
            model.addAttribute("isApplied", false);
        }

        return "event/event-detail";
    }

    // 행사 신청
    @RequestMapping(value = "/events/{id}/apply", method = RequestMethod.POST)
    public String applyEvent(@PathVariable Long id,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        if (userDetails == null) return "redirect:/login";

        // 중복 신청 체크
        int count = eventMapper.countApplication(id, userDetails.getUsername());
        if (count > 0) {
            redirectAttributes.addFlashAttribute("errorMsg", "이미 신청한 행사예요.");
            return "redirect:/events/" + id;
        }

        // 정원 체크
        EventEntity event = eventMapper.selectEventById(id);
        if (event.getApplicationCount() >= event.getCapacity()) {
            redirectAttributes.addFlashAttribute("errorMsg", "정원이 마감됐어요.");
            return "redirect:/events/" + id;
        }

        EventApplicationEntity application = EventApplicationEntity.builder()
                .eventId(id)
                .userId(userDetails.getUsername())
                .build();
        eventMapper.insertApplication(application);

        redirectAttributes.addFlashAttribute("successMsg", "신청이 완료됐어요!");
        return "redirect:/events/" + id;
    }

    // 행사 신청 취소
    @RequestMapping(value = "/events/{id}/cancel", method = RequestMethod.POST)
    public String cancelEvent(@PathVariable Long id,
                              @RequestParam(defaultValue = "") String redirect,
                              @AuthenticationPrincipal UserDetails userDetails,
                              RedirectAttributes redirectAttributes) {
        if (userDetails == null) return "redirect:/login";

        eventMapper.deleteApplication(id, userDetails.getUsername());
        redirectAttributes.addFlashAttribute("successMsg", "신청이 취소됐어요.");

        if (!redirect.isEmpty()) return "redirect:" + redirect;
        return "redirect:/events/" + id;
    }
}
