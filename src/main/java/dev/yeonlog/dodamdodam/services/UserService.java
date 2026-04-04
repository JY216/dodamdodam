package dev.yeonlog.dodamdodam.services;

import dev.yeonlog.dodamdodam.entities.EmailTokenEntity;
import dev.yeonlog.dodamdodam.entities.UserEntity;
import dev.yeonlog.dodamdodam.mappers.EmailTokenMapper;
import dev.yeonlog.dodamdodam.mappers.UserMapper;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class UserService {
    private final UserMapper userMapper;
    private final EmailTokenMapper emailTokenMapper;
    private final JavaMailSender mailSender;
    private final BCryptPasswordEncoder passwordEncoder;
    private final SpringTemplateEngine templateEngine;

    public UserService(UserMapper userMapper, EmailTokenMapper emailTokenMapper, JavaMailSenderImpl mailSender, SpringTemplateEngine templateEngine) {
        this.userMapper = userMapper;
        this.emailTokenMapper = emailTokenMapper;
        this.mailSender = mailSender;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.templateEngine = templateEngine;
    }

    // 이메일 인증번호 전송
    public EmailTokenEntity sendEmail(String email) throws MessagingException {
        if (email == null || email.isBlank()) return null;

        String code = RandomStringUtils.randomNumeric(6);
        String salt = new BCryptPasswordEncoder().encode(
                String.format("%s%s%f%f", email, code, Math.random(), Math.random())
        );

        EmailTokenEntity emailToken = new EmailTokenEntity();
        emailToken.setEmail(email);
        emailToken.setCode(code);
        emailToken.setSalt(salt);
        emailToken.setVerified(false);
        emailToken.setUsed(false);
        emailToken.setCreatedAt(LocalDateTime.now());
        emailToken.setExpiresAt(LocalDateTime.now().plusMinutes(10L));

        if (emailTokenMapper.insert(emailToken) < 1) return null;

        Context context = new Context();
        context.setVariable("type", "회원가입");
        context.setVariable("code", code);
        String body = templateEngine.process("user/sendEmail", context);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
        helper.setFrom("ljh5898123@gmail.com");
        helper.setTo(email);
        helper.setSubject("[도담도담] 회원가입 인증번호 안내");
        helper.setText(body, true);

        mailSender.send(message);
        return emailToken;
    }

    // 이메일 인증번호 확인
    @Transactional
    public boolean verifyEmail(String email, String code, String salt) {
        if (email == null || code == null || salt == null) return false;

        EmailTokenEntity dbEmailToken = emailTokenMapper.select(email,code,salt);
        if (dbEmailToken == null || dbEmailToken.isVerified() || dbEmailToken.isUsed()) return false;
        if (dbEmailToken.getExpiresAt().isBefore(LocalDateTime.now())) return false;

        dbEmailToken.setVerified(true);
        return emailTokenMapper.update(dbEmailToken) > 0;
    }

    // 회원가입
    public boolean register(UserEntity user, String email, String code, String salt) {
        System.out.println("email: " + email);
        System.out.println("code: " + code);
        System.out.println("salt: " + salt);
        if (email == null || code == null || salt == null) {
            System.out.println("null 체크 실패");
            return false;
        }

        EmailTokenEntity dbEmailToken = emailTokenMapper.select(email, code, salt);
        if (dbEmailToken == null || !dbEmailToken.isVerified() || dbEmailToken.isUsed()) {
            System.out.println("토큰 체크 실패 - dbEmailToken: " + dbEmailToken);
            return false;
        }

        dbEmailToken.setUsed(true);
        if (emailTokenMapper.update(dbEmailToken) < 1) return false;

        user.setEmail(email);
        // 비밀번호 BCrypt 암호화
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // role, status 기본값 설정
        user.setRole("USER");
        user.setStatus("NORMAL");
        return userMapper.insert(user) > 0;
    }

    // 로그인
    public UserEntity login(String userId, String password) {
        if (userId == null || userId.isBlank()) return null;
        if (password == null || password.isBlank()) return null;

        UserEntity dbUser = userMapper.selectByUserId(userId);
        if (dbUser == null) return null;
        if (!passwordEncoder.matches(password, dbUser.getPassword())) return null;
        if (dbUser.getStatus().equals("SUSPENDED")) return null;

        return dbUser;
    }

    // 아이디 찾기
    public String findUserId(String email) {
        if (email == null || email.isBlank()) return null;
        UserEntity user = userMapper.selectByEmail(email);
        if (user == null) return null;
        return user.getUserId();
    }

    // 비밀번호 재설정용 이메일 인증 전송
    public EmailTokenEntity sendPasswordResetEmail(String email) throws MessagingException {
        if (email == null || email.isBlank()) return null;

        // 해당 이메일로 가입된 유저 확인
        UserEntity user = userMapper.selectByEmail(email);
        if (user == null) return null;

        String code = RandomStringUtils.randomNumeric(6);
        String salt = new BCryptPasswordEncoder().encode(
                String.format("%s%s%f%f", email, code, Math.random(), Math.random())
        );

        EmailTokenEntity emailToken = new EmailTokenEntity();
        emailToken.setEmail(email);
        emailToken.setCode(code);
        emailToken.setSalt(salt);
        emailToken.setVerified(false);
        emailToken.setUsed(false);
        emailToken.setCreatedAt(LocalDateTime.now());
        emailToken.setExpiresAt(LocalDateTime.now().plusMinutes(10L));

        if (emailTokenMapper.insert(emailToken) < 1) return null;

        Context context = new Context();
        context.setVariable("type", "비밀번호 재설정");
        context.setVariable("code", code);
        String body = templateEngine.process("user/sendEmail", context);

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
        helper.setFrom("ljh5898123@gmail.com");
        helper.setTo(email);
        helper.setSubject("[도담도담] 비밀번호 재설정 인증번호 안내");
        helper.setText(body, true);

        mailSender.send(message);
        return emailToken;
    }

    // 비밀번호 재설정
    public boolean resetPassword(String email, String code, String salt, String newPassword) {
        if (email == null || code == null || salt == null || newPassword == null) return false;

        EmailTokenEntity dbEmailToken = emailTokenMapper.select(email, code, salt);
        if (dbEmailToken == null || !dbEmailToken.isVerified() || dbEmailToken.isUsed()) return false;
        if (dbEmailToken.getExpiresAt().isBefore(LocalDateTime.now())) return false;

        dbEmailToken.setUsed(true);
        if (emailTokenMapper.update(dbEmailToken) < 1) return false;

        UserEntity user = userMapper.selectByEmail(email);
        if (user == null) return false;

        String encodedPassword = passwordEncoder.encode(newPassword);
        userMapper.updatePassword(user.getUserId(), encodedPassword);
        return true;
    }
}
