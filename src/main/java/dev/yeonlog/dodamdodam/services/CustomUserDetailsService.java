package dev.yeonlog.dodamdodam.services;

import dev.yeonlog.dodamdodam.entities.UserEntity;
import dev.yeonlog.dodamdodam.mappers.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        UserEntity user = userMapper.selectByUserId(userId);
        if (user == null) {
            throw new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + userId);
        }
        if (user.getStatus().equals("SUSPENDED")) {
            throw new UsernameNotFoundException("정지된 계정입니다.");
        }
        return new User(
                user.getUserId(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()))
        );
    }
}
