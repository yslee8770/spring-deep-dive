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
public class TxStep7RequiresNewService {

    private final MemberRepository memberRepository;

    private void logTx(String point) {
        boolean active = TransactionSynchronizationManager.isActualTransactionActive();
        String name = TransactionSynchronizationManager.getCurrentTransactionName();
        log.info("[{}] active={}, txName={}", point, active, name);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void innerRequiresNew(Long memberId) {
        logTx("innerRequiresNew(REQUIRES_NEW)");
        Member m = memberRepository.findById(memberId).orElseThrow();
        m.changeName("INNER");
    }
}
