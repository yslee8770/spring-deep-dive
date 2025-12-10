package com.example.spring_deep_dive.service;

import com.example.spring_deep_dive.domain.member.Member;
import com.example.spring_deep_dive.respository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberNestedService {

    private final MemberRepository memberRepository;

    @Transactional(propagation = Propagation.NESTED)
    public void nestedChange(Long memberId) {
        logTx("nestedChange (NESTED)");

        Member m = memberRepository.findById(memberId).orElseThrow();
        m.changeName("INNER_NESTED");

        log.info("Inner changed name to INNER_NESTED — before throwing exception");

        // savepoint rollback 발생
        throw new RuntimeException("NESTED failure");
    }

    private void logTx(String point) {
        boolean active = TransactionSynchronizationManager.isActualTransactionActive();
        String name = TransactionSynchronizationManager.getCurrentTransactionName();
        log.info("[{}] active={}, name={}", point, active, name);
    }
}
