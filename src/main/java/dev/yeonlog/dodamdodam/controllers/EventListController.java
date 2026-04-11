package dev.yeonlog.dodamdodam.controllers;

import dev.yeonlog.dodamdodam.dtos.EventApplicantDto;
import dev.yeonlog.dodamdodam.dtos.EventListDto;
import dev.yeonlog.dodamdodam.mappers.EventListMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(value = "admin")
@RequiredArgsConstructor
public class EventListController {

    private final EventListMapper eventListMapper;
    private static final int PAGE_SIZE = 10;

    @RequestMapping(value = "/event-list", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String eventList(@RequestParam(defaultValue = "1") int page, Model model) {
        int offset = (page - 1) * PAGE_SIZE;
        int totalCount = eventListMapper.countAll();
        int totalPages = (int) Math.ceil((double) totalCount / PAGE_SIZE);
        if (totalPages == 0) totalPages = 1;

        List<EventListDto> events = eventListMapper.findAll(offset, PAGE_SIZE);

        Map<String, Object> data = new HashMap<>();
        data.put("events", events);
        data.put("totalCount", totalCount);
        data.put("totalPages", totalPages);
        data.put("currentPage", page);

        model.addAttribute("data", data);
        model.addAttribute("currentMenu", "event-list");
        return "admin/admin-page";
    }

    @RequestMapping(value = "/event-list/{eventId}/applicants", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<EventApplicantDto>> getApplicants(@PathVariable Long eventId) {
        List<EventApplicantDto> applicants = eventListMapper.findApplicantsByEventId(eventId);
        return ResponseEntity.ok(applicants);
    }
}
