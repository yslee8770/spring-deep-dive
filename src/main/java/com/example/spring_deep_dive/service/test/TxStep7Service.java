package com.example.spring_deep_dive.service.test;

import com.example.spring_deep_dive.domain.member.Member;
import com.example.spring_deep_dive.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
@RequiredArgsConstructor
public class TxStep7Service {

    private final MemberRepository memberRepository;
    private final TxStep7RequiresNewService requiresNewService;

    private void logTx(String point) {
        boolean active = TransactionSynchronizationManager.isActualTransactionActive();
        String name = TransactionSynchronizationManager.getCurrentTransactionName();
        log.info("[{}] active={}, txName={}", point, active, name);
    }

    // ============================
    // REQUIRED → REQUIRED
    // ============================

    @Transactional
    public void outerRequired(Long memberId) {
        logTx("outerRequired");
        Member m = memberRepository.findById(memberId).orElseThrow();
        m.changeName("OUTER-REQUIRED-BEFORE");

        innerRequired(memberId);    // REQUIRED → REQUIRED

        m.changeName("OUTER-REQUIRED-AFTER");
    }

    @Transactional
    public void innerRequired(Long memberId) {
        logTx("innerRequired");
        Member m = memberRepository.findById(memberId).orElseThrow();
        m.changeName("INNER-REQUIRED");
    }

    // ============================
    // REQUIRED → REQUIRES_NEW
    // ============================

    @Transactional
    public void outerRequiresNew(Long memberId) {
        logTx("outerRequiresNew");
        Member m = memberRepository.findById(memberId).orElseThrow();
        m.changeName("OUTER");

        requiresNewService.innerRequiresNew(memberId);

        throw new RuntimeException("outer rollback");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void innerRequiresNew(Long memberId) {
        logTx("innerRequiresNew");
        Member m = memberRepository.findById(memberId).orElseThrow();
        m.changeName("INNER");
    }
}
