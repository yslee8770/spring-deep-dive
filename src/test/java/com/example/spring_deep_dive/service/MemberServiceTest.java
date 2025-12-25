package com.example.spring_deep_dive.service;

import com.example.spring_deep_dive.domain.member.Member;
import com.example.spring_deep_dive.repository.MemberRepository;
import com.example.spring_deep_dive.service.member.MemberService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void nested_problem_shows_persistence_context_pollution() {
        // given
        Member m = memberRepository.save(Member.createUser("a@a.com", "before","test"));

        // when
        try {
            memberService.nestedProblemStep3(m.getId());
        } catch (Exception ignored) {}

        // then
        Member found = memberRepository.findById(m.getId()).orElseThrow();

        log.info("DB 최종 값 = {}", found.getName());

        // 🔥 결과 예측:
        // DB에는 savepoint rollback 영향 때문에 INNER_NESTED 적용 안 됨
        // OUTER_AFTER는 flush에서 DB에 반영됨
        // 즉 DB 값은 OUTER_AFTER가 됨
        assertThat(found.getName()).isEqualTo("OUTER_AFTER");
    }

    @Test
    @DisplayName("Step5-1: 일반 @Transactional에서는 dirty checking이 DB에 반영된다")
    void normal_tx_flush_happens() {
        // given
        Member m = memberRepository.save(Member.createUser("normal@test.com", "test","before"));

        // when
        memberService.changeName_normal(m.getId(), "AFTER_NORMAL");

        // then
        Member found = memberRepository.findById(m.getId()).orElseThrow();
        log.info("DB name after normal tx = {}", found.getName());
        assertThat(found.getName()).isEqualTo("AFTER_NORMAL");
    }

    @Test
    @DisplayName("Step5-2: readOnly TX에서는 flush가 스킵되어 변경이 DB에 반영되지 않는다")
    void readonly_tx_flush_skipped() {
        // given
        Member m = memberRepository.save(Member.createUser("ro@test.com", "test","before"));

        // when
        memberService.changeName_readOnly(m.getId(), "AFTER_READONLY");

        // then
        Member found = memberRepository.findById(m.getId()).orElseThrow();
        log.info("DB name after readOnly tx = {}", found.getName());
        assertThat(found.getName()).isEqualTo("before"); // 🔥 여기가 포인트
    }

}