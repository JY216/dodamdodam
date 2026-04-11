package dev.yeonlog.dodamdodam.controllers;

import dev.yeonlog.dodamdodam.entities.NoticeEntity;
import dev.yeonlog.dodamdodam.mappers.NoticeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeMapper noticeMapper;

    // 공지 목록
    @RequestMapping(value = "/notices", method = RequestMethod.GET)
    public String noticeList(Model model) {
        List<NoticeEntity> notices = noticeMapper.selectAllNotices();
        model.addAttribute("notices", notices);
        return "notice/notice-list";
    }

    // 공지 상세
    @RequestMapping(value = "/notices/{id}", method = RequestMethod.GET)
    public String noticeDetail(@PathVariable Long id, Model model) {
        NoticeEntity notice = noticeMapper.selectById(id);
        if (notice == null) return "redirect:/notices";
        model.addAttribute("notice", notice);
        return "notice/notice-detail";
    }
}
