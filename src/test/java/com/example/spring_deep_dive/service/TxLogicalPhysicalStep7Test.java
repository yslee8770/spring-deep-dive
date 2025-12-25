package com.example.spring_deep_dive.service;

import com.example.spring_deep_dive.domain.member.Member;
import com.example.spring_deep_dive.repository.MemberRepository;
import com.example.spring_deep_dive.service.test.TxStep7Service;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
class TxStep7Test {

    @Autowired MemberRepository memberRepository;
    @Autowired
    TxStep7Service service;

    private Member newMember(String email, String name) {
        return memberRepository.save(Member.createUser(email,"test", name));
    }

    @Test
    @DisplayName("Step7-1: REQUIRED → REQUIRED (논리 2, 물리 1)")
    void required_required() {
        Member m = newMember("req@test.com", "before");

        service.outerRequired(m.getId());

        Member found = memberRepository.findById(m.getId()).orElseThrow();
        assertThat(found.getName()).isEqualTo("OUTER-REQUIRED-AFTER");
    }

    @Test
    @DisplayName("Step7-2: REQUIRED → REQUIRES_NEW (논리 2, 물리 2)")
    void required_requiresNew() {
        Member m = newMember("rn@test.com", "before");

        try {
            service.outerRequiresNew(m.getId());
        } catch (Exception ignored) {}

        Member found = memberRepository.findById(m.getId()).orElseThrow();

        // inner(REQUIRES_NEW)만 살아있고 outer는 롤백됨
        assertThat(found.getName()).isEqualTo("INNER");
    }
}