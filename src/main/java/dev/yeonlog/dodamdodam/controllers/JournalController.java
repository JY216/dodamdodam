package dev.yeonlog.dodamdodam.controllers;

import dev.yeonlog.dodamdodam.entities.JournalEntity;
import dev.yeonlog.dodamdodam.mappers.JournalMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("journal")
@RequiredArgsConstructor
public class JournalController {

    private final JournalMapper journalMapper;

    @RequestMapping(value = "", method = RequestMethod.GET, produces = MediaType.TEXT_HTML_VALUE)
    public String journalPage() {
        return "/journal";
    }

    // 일지 목록 조회
    @RequestMapping(value = "/api/list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<JournalEntity>> getList(
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(journalMapper.findAllByUserId(userDetails.getUsername()));
    }

    // 일지 저장
    @RequestMapping(value = "/api/save", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<JournalEntity> save(
            @RequestBody JournalEntity dto,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return ResponseEntity.status(401).build();
        dto.setUserId(userDetails.getUsername());
        journalMapper.insert(dto);
        return ResponseEntity.ok(dto);
    }

    // 일지 삭제
    @RequestMapping(value = "/api/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return ResponseEntity.status(401).build();
        journalMapper.deleteById(id, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    // 목표 조회
    @RequestMapping(value = "/api/goal", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getGoal(
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return ResponseEntity.status(401).build();
        String userId = userDetails.getUsername();
        Integer goal  = journalMapper.findGoal(userId);
        int readCount = journalMapper.countDoneByUserId(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("goalCount", goal != null ? goal : 12);
        result.put("readCount", readCount);
        return ResponseEntity.ok(result);
    }

    // 목표 저장
    @RequestMapping(value = "/api/goal", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Void> saveGoal(
            @RequestBody Map<String, Integer> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return ResponseEntity.status(401).build();
        journalMapper.upsertGoal(userDetails.getUsername(), body.get("goalCount"));
        return ResponseEntity.ok().build();
    }
}
