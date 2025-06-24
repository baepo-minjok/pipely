package com.example.backend.auth.user.scheduler;

import com.example.backend.auth.email.service.EmailService;
import com.example.backend.auth.user.model.Users;
import com.example.backend.auth.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class UserDormancyScheduler {

    private final EmailService emailService;
    private final UserRepository userRepository;

    @Value("${user.dormancy.period.days}")
    private long dormancyPeriodDays;

    @Scheduled(cron = "0 0 3 * * *") // 매일 새벽 3시에 실행
    @Transactional
    public void processDormantUsers() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(dormancyPeriodDays);

        int pageSize = 100;
        Pageable pageable = PageRequest.of(0, pageSize);
        Page<Users> page;
        do {
            page = userRepository.findByStatusAndLastLoginBefore(Users.UserStatus.ACTIVE, threshold, pageable);
            for (Users user : page.getContent()) {
                // 상태 변경
                user.setStatus(Users.UserStatus.DORMANT);
                userRepository.save(user);
                // 이메일 발송: 휴면 안내
                emailService.sendDormantNotificationEmail(user.getEmail());
            }
            if (page.hasNext()) {
                pageable = page.nextPageable();
            } else {
                break;
            }
        } while (true);
    }

}

