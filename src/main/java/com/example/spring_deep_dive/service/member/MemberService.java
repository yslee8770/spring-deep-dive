package com.example.spring_deep_dive.service.member;

import com.example.spring_deep_dive.domain.member.Member;
import com.example.spring_deep_dive.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberNestedService memberNestedService;

    private void logTx(String point) {
        boolean active = TransactionSynchronizationManager.isActualTransactionActive();
        String name = TransactionSynchronizationManager.getCurrentTransactionName();
        log.info("[{}] active={}, name={}", point, active, name);
    }

    @Transactional  // REQUIRED
    public void nestedProblemStep3(Long memberId) {
        logTx("outer START");

        Member m = memberRepository.findById(memberId).orElseThrow();
        m.changeName("OUTER_BEFORE");

        log.info("Outer changed name → OUTER_BEFORE");

        // 🔥 inner NESTED — savepoint rollback 포함됨
        try {
            memberNestedService.nestedChange(memberId);
        } catch (Exception e) {
            log.info("Inner NESTED failed — caught exception");
        }

        log.info("After inner failure, current member name = {}", m.getName());
        // 여기서 name은 INNER_NESTED이거나 OUTER_BEFORE일 수 있음
        // 즉, JPA 영속성 컨텍스트의 오염 상태를 확인

        m.changeName("OUTER_AFTER");
        log.info("Outer changed name again → OUTER_AFTER");

        logTx("outer END");
    }

    // =========================
    // Step5-1: 일반 TX → dirty checking 반영됨
    // =========================
    @Transactional
    public void changeName_normal(Long memberId, String newName) {
        logTx("changeName_normal");

        Member member = memberRepository.findById(memberId).orElseThrow();
        log.info("before normal change, name = {}", member.getName());

        member.changeName(newName); // dirty checking 대상

        log.info("after normal change, name = {}", member.getName());
        // commit 시 flush → UPDATE SQL 발생
    }

    // =========================
    // Step5-2: readOnly TX → flush 스킵
    // =========================
    @Transactional(readOnly = true)
    public void changeName_readOnly(Long memberId, String newName) {
        logTx("changeName_readOnly");

        Member member = memberRepository.findById(memberId).orElseThrow();
        log.info("before readOnly change, name = {}", member.getName());

        member.changeName(newName); // 메모리 상으로만 변경

        log.info("after readOnly change, name = {}", member.getName());
        // readOnly=true → flush 생략 → UPDATE 안 나가야 함
    }

}

