package com.example.spring_deep_dive.service.test;

import com.example.spring_deep_dive.domain.member.Member;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class JpaInternalStep1Service {

    private final EntityManager em;

    /**
     * 1차 캐시 테스트
     * - 같은 트랜잭션 안에서 같은 ID를 두 번 조회했을 때
     *   SQL은 한 번만 나가고, 두 객체는 == 로 같아야 한다.
     */
    @Transactional
    public void firstLevelCache(Long memberId) {
        log.info("== 1차 캐시 테스트 시작 ==");

        Member m1 = em.find(Member.class, memberId);
        log.info("m1 loaded, id={}, name={}", m1.getId(), m1.getName());

        Member m2 = em.find(Member.class, memberId);
        log.info("m2 loaded, id={}, name={}", m2.getId(), m2.getName());

        log.info("m1 == m2 ? {}", (m1 == m2));
        log.info("== 1차 캐시 테스트 종료 ==");
    }

    /**
     * dirty checking 기본 테스트
     * - 값이 실제로 바뀌면 UPDATE SQL 발생
     */
    @Transactional
    public void dirtyChecking_changeValue(Long memberId, String newName) {
        log.info("== dirty checking: 값 변경 케이스 ==");

        Member m = em.find(Member.class, memberId);
        log.info("before change, id={}, name={}", m.getId(), m.getName());

        m.changeName(newName); // 값 실제 변경

        log.info("after change, id={}, name={}", m.getId(), m.getName());
        log.info("== dirty checking: 값 변경 케이스 종료 ==");
    }

    /**
     * dirty checking - 동일 값 대입 테스트
     * - 스냅샷과 현재 값이 같으면 dirty checking 안 걸리고 UPDATE SQL이 안 나가야 한다.
     */
    @Transactional
    public void dirtyChecking_sameValue(Long memberId) {
        log.info("== dirty checking: 동일 값 대입 케이스 ==");

        Member m = em.find(Member.class, memberId);
        String currentName = m.getName();
        log.info("before same-value change, id={}, name={}", m.getId(), currentName);

        m.changeName(currentName);

        log.info("after same-value change, id={}, name={}", m.getId(), m.getName());
        log.info("== dirty checking: 동일 값 대입 케이스 종료 ==");
    }
}