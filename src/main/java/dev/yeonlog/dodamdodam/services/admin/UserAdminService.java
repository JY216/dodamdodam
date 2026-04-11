package dev.yeonlog.dodamdodam.services.admin;

import dev.yeonlog.dodamdodam.dtos.UserAdminDto;
import dev.yeonlog.dodamdodam.mappers.UserAdminMapper;
import dev.yeonlog.dodamdodam.mappers.UserStatusMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserAdminService {
    private static final int PAGE_SIZE = 10;

    private final UserAdminMapper memberMapper;
    private final UserStatusMapper userStatusMapper;

    public Map<String, Object> getMemberList(
            String name,
            String birth,
            String mobile,
            int page) {
        int offset = (page - 1) * PAGE_SIZE;
        int totalCount = memberMapper.countAll(name, birth, mobile);
        int totalPages = (int) Math.ceil((double) totalCount / PAGE_SIZE);

        if (totalPages == 0) totalPages = 1;

        List<UserAdminDto> members = memberMapper.findAll(name, birth, mobile, offset, PAGE_SIZE);

        Map<String, Object> result = new HashMap<>();
        result.put("members", members);
        result.put("totalCount", totalCount);
        result.put("totalPages", totalPages);
        result.put("currentPage", page);
        return result;
    }

    public void toggleStatus(String userId) {
        String current = userStatusMapper.findStatusByUserId(userId);
        String next = "SUSPENDED".equals(current) ? "NORMAL" : "SUSPENDED";
        userStatusMapper.updateStatus(userId, next);
    }
}
