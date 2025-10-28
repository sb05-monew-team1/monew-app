package com.codeit.monew.user.service;

import com.codeit.monew.user.repository.UserRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserCleanupService {
  private final UserRepository userRepository;
  /*
  //매시간 정각에 실행되어 논리 삭제 후 24시간이 지난 사용자 물리삭제함.
  @Scheduled(cron = "0 0 * * * ?") //매일 매시 정각(0분 0초)
  public void cleanupDeletedUsers() {
    log.info("논리 삭제 24시간 경과 사용자 물리 삭제 스케줄러 시작");

    Instant cutoffTome = Instant.now().minus(24, ChronoUnit.HOURS);

    try{
      userRepository.deleteByDeletedAtBefore(cutoffTome);
      log.info("물리 삭제 완료. 기준 시간: {}", cutoffTome);
    } catch (Exception e){
      log.error("사용자 물리 삭제 스케줄러 실행 중 오류 발생", e);
    }
  }

   */

  @Scheduled(cron = "0 * * * * ?")
  public void cleanupDeletedUsersForTest() {
    Instant cutoffTime = Instant.now().minus(1, ChronoUnit.MINUTES);
    userRepository.deleteByDeletedAtBefore(cutoffTime);
  }
}
