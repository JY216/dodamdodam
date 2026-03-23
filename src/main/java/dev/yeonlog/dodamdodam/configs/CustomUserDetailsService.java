package dev.yeonlog.dodamdodam.configs;

import dev.yeonlog.dodamdodam.entities.UserEntity;
import dev.yeonlog.dodamdodam.mappers.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException{
        UserEntity user = userMapper.selectByUserId(userId);

        if (user == null) throw new UsernameNotFoundException("없는 유저입니다.");

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUserId())
                .password(user.getPassword())
                .roles(user.getRole())
                .build();
    }
}
