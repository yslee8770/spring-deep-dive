package com.example.spring_deep_dive.service.test;

import com.example.spring_deep_dive.domain.member.Member;
import com.example.spring_deep_dive.respository.MemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class JpaInternalStep1ServiceTest {

    @Autowired
    JpaInternalStep1Service service;

    @Autowired
    MemberRepository memberRepository;

    private Long createMember(String email, String name) {
        Member member = Member.createUser(email, "test", name);
        return memberRepository.save(member).getId();
    }

    @Test
    @DisplayName("1차 캐시: 같은 엔티티를 두 번 조회해도 SQL은 한 번만 나간다")
    void firstLevelCache_identityAndSingleSelect() {
        // given
        Long memberId = createMember("cache@test.com", "cache-user");

        // when
        service.firstLevelCache(memberId);

        // then
        // 콘솔 로그:
        // - SELECT는 1번만 찍혀야 함
        // - [m1 == m2 ? true] 로그 확인
    }

    @Test
    @DisplayName("dirty checking: 값이 실제로 바뀌면 UPDATE SQL이 발생한다")
    void dirtyChecking_changeValue_triggersUpdate() {
        // given
        Long memberId = createMember("dirty@test.com", "before");

        // when
        service.dirtyChecking_changeValue(memberId, "after");

        // then
        // 콘솔 로그:
        // - INSERT 1번
        // - 트랜잭션 종료 시점에 UPDATE 1번
    }

    @Test
    @DisplayName("dirty checking: 동일 값 대입은 UPDATE SQL이 발생하지 않는다")
    void dirtyChecking_sameValue_noUpdate() {
        // given
        Long memberId = createMember("same@test.com", "same");

        // when
        service.dirtyChecking_sameValue(memberId);

        // then
        // 콘솔 로그:
        // - INSERT 1번만 찍히고
        // - UPDATE는 없어야 한다.
    }
}