package com.example.spring_deep_dive.service.test;

import com.example.spring_deep_dive.domain.member.Member;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class JpaInternalStep2Service {

    @PersistenceContext
    private final EntityManager em;
    /**
     * flush timing 1
     * - 기존 Member를 조회해서 이름만 바꿈
     * - em.flush()는 호출하지 않음
     * - 트랜잭션 커밋 시점에 UPDATE SQL이 나가야 한다.
     */
    @Transactional
    public void flushOnCommit(Long memberId) {
        log.info("== flushOnCommit 시작 ==");

        Member member = em.find(Member.class, memberId);
        log.info("before change, id={}, name={}", member.getId(), member.getName());

        member.changeName("flush-commit");
        log.info("after change, id={}, name={}", member.getId(), member.getName());

        log.info("== flushOnCommit 종료 직전 ==");
    }

    /**
     * flush timing 2
     * - em.flush()를 호출해서 UPDATE를 중간에 미리 날린다.
     */
    @Transactional
    public void flushManually(Long memberId) {
        log.info("== flushManually 시작 ==");

        Member member = em.find(Member.class, memberId);
        log.info("before change, id={}, name={}", member.getId(), member.getName());

        member.changeName("flush-manual");
        log.info("after change, id={}, name={}", member.getId(), member.getName());

        log.info("em.flush() 호출 전");
        em.flush();
        log.info("em.flush() 호출 후");

        log.info("== flushManually 종료 ==");
    }

    /**
     * JPQL flush-trigger 테스트
     * - dirty 상태로 JPQL을 실행하면
     *   JPQL 실행 직전에 flush가 일어나서 UPDATE가 반영되고 SELECT가 실행된다.
     */
    @Transactional
    public void jpqlFlushTrigger(Long memberId) {
        log.info("== jpqlFlushTrigger 시작 ==");

        Member member = em.find(Member.class, memberId);
        log.info("before change, id={}, name={}", member.getId(), member.getName());

        member.changeName("flush-jpql");
        log.info("after change, id={}, name={}", member.getId(), member.getName());

        log.info("JPQL 실행 전 - UPDATE SQL이 아직 안 나왔는지 확인");

        var result = em.createQuery(
                        "select m from Member m where m.id = :id", Member.class)
                .setParameter("id", memberId)
                .getResultList();

        log.info("JPQL 실행 후, result size = {}, first.name={}",
                result.size(), result.isEmpty() ? null : result.get(0).getName());

        log.info("== jpqlFlushTrigger 종료 ==");
    }
}
