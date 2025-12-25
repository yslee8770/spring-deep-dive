package com.example.spring_deep_dive.service.test;

import com.example.spring_deep_dive.repository.MemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.spring_deep_dive.domain.member.Member;
import org.junit.jupiter.api.BeforeEach;

@Slf4j
@SpringBootTest
class JpaInternalStep2ServiceTest {

    @Autowired
    JpaInternalStep2Service service;

    @Autowired
    MemberRepository memberRepository;

    Long memberId;

    @BeforeEach
    void setUp() {
        Member member = Member.createUser("flush@test.com", "encoded", "original");
        memberId = memberRepository.save(member).getId();
        log.info(">>> 테스트용 member 저장 완료, id={}", memberId);
    }

    @Test
    @DisplayName("flushOnCommit: 커밋 시점에 UPDATE flush")
    void flushOnCommit_test() {
        service.flushOnCommit(memberId);

        // 로그 / SQL 기대:
        // - setUp에서 INSERT 1번
        // - flushOnCommit 실행 중에는 UPDATE 없음
        // - 메서드 끝나면서 (커밋 직전) UPDATE 1번 발생
    }

    @Test
    @DisplayName("flushManually: em.flush() 호출 시점에 UPDATE flush")
    void flushManually_test() {
        service.flushManually(memberId);

        // 기대:
        // - setUp에서 INSERT 1번
        // - "em.flush() 호출 전"까지 UPDATE 없음
        // - em.flush() 호출 시점에 UPDATE 1번
        // - 커밋 시점에는 추가 UPDATE 없음
    }

    @Test
    @DisplayName("jpqlFlushTrigger: JPQL 실행이 flush-trigger로 UPDATE를 먼저 날린다")
    void jpqlFlushTrigger_test() {
        service.jpqlFlushTrigger(memberId);

        // 기대:
        // - setUp에서 INSERT 1번
        // - JPQL 실행 전까지 UPDATE 없음
        // - JPQL 실행 직전에 UPDATE
        // - 그리고 SELECT (member 조회) 쿼리
    }
}