package dev.yeonlog.dodamdodam.services.admin;

import dev.yeonlog.dodamdodam.dtos.EventAdminDto;
import dev.yeonlog.dodamdodam.mappers.EventAdminMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EventAdminService {

    private final EventAdminMapper eventAdminMapper;
    private static final int PAGE_SIZE = 10;

    public Map<String, Object> getEventList(int page) {
        int offset = (page - 1) * PAGE_SIZE;
        int totalCount = eventAdminMapper.countAll();
        int totalPages = (int) Math.ceil((double) totalCount / PAGE_SIZE);

        if (totalPages == 0) totalPages = 1;

        List<EventAdminDto> events = eventAdminMapper.findAll(offset, PAGE_SIZE);

        Map<String, Object> result = new HashMap<>();
        result.put("events", events);
        result.put("totalCount", totalCount);
        result.put("totalPages", totalPages);
        result.put("currentPage", page);
        return result;
    }

    public void updateStatus(Long id, String status) {
        eventAdminMapper.updateStatus(id, status);
    }

    public void deleteEvent(Long id) {
        eventAdminMapper.deleteById(id);
    }
}
