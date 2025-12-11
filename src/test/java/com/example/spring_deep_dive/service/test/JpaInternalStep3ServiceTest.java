package com.example.spring_deep_dive.service.test;

import com.example.spring_deep_dive.domain.member.Member;
import com.example.spring_deep_dive.respository.MemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class JpaInternalStep3ServiceTest {

    @Autowired
    JpaInternalStep3Service service;

    @Autowired
    MemberRepository memberRepository;

    Long memberId;

    @BeforeEach
    void setUp() {
        Member member = Member.createUser("detach@test.com", "encoded", "original");
        memberId = memberRepository.save(member).getId();
        log.info(">>> 테스트용 member 저장 완료, id={}", memberId);
    }

    @Test
    @DisplayName("detach 후 변경: UPDATE SQL이 나가면 안 된다")
    void detach_thenChange_test() {
        service.detach_thenChange(memberId);

        // 기대:
        // - setUp에서 INSERT 1번
        // - detach_thenChange 실행 중 / 커밋 시점에 UPDATE 없음
    }

    @Test
    @DisplayName("clear 후 변경: UPDATE SQL이 나가면 안 된다")
    void clear_thenChange_test() {
        service.clear_thenChange(memberId);

        // 기대:
        // - setUp에서 INSERT 1번
        // - clear_thenChange 실행 후 커밋 시점에도 UPDATE 없음
    }

    @Test
    @DisplayName("detach + merge 후 변경: UPDATE SQL이 나가야 한다")
    void detach_and_merge_thenChange_test() {
        service.detach_and_merge_thenChange(memberId);

        // 기대:
        // - setUp에서 INSERT 1번
        // - detach_and_merge_thenChange 트랜잭션 커밋 시점에 UPDATE 1번
        // - 로그: original != merged, merged.name == 'merged-final'
    }
}