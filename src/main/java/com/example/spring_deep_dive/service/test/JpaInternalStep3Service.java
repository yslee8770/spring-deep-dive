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
public class JpaInternalStep3Service {

    @PersistenceContext
    private final EntityManager em;

    /**
     * detach 이후 변경 → UPDATE 안 나가야 함.
     */
    @Transactional
    public void detach_thenChange(Long memberId) {
        log.info("== detach_thenChange 시작 ==");

        Member member = em.find(Member.class, memberId);
        log.info("load, id={}, name={}", member.getId(), member.getName());
        log.info("contains before detach = {}", em.contains(member));

        em.detach(member);
        log.info("after detach, contains = {}", em.contains(member));

        member.changeName("detached-change");
        log.info("after change (detached), name={}", member.getName());

        log.info("== detach_thenChange 종료 ==");
    }

    /**
     * clear 이후 변경 → UPDATE 안 나가야 함.
     */
    @Transactional
    public void clear_thenChange(Long memberId) {
        log.info("== clear_thenChange 시작 ==");

        Member member = em.find(Member.class, memberId);
        log.info("load, id={}, name={}", member.getId(), member.getName());
        log.info("contains before clear = {}", em.contains(member));

        em.clear();
        log.info("after clear, contains = {}", em.contains(member));

        member.changeName("cleared-change");
        log.info("after change (cleared), name={}", member.getName());

        log.info("== clear_thenChange 종료 ==");
    }

    /**
     * detach 이후 merge + 변경 → UPDATE 나가야 함.
     * - original(분리된 엔티티)와 merged(새로운 영속 엔티티) identity 다름
     */
    @Transactional
    public void detach_and_merge_thenChange(Long memberId) {
        log.info("== detach_and_merge_thenChange 시작 ==");

        Member original = em.find(Member.class, memberId);
        log.info("load, id={}, name={}", original.getId(), original.getName());
        log.info("contains original(before detach) = {}", em.contains(original));

        em.detach(original);
        log.info("after detach, contains original = {}", em.contains(original));

        original.changeName("changed-on-detached");
        log.info("after change (detached), original.name={}", original.getName());

        Member merged = em.merge(original);
        log.info("after merge, contains merged = {}", em.contains(merged));
        log.info("original == merged ? {}", (original == merged));
        log.info("merged.name={}", merged.getName());

        merged.changeName("merged-final");
        log.info("after change (merged), merged.name={}", merged.getName());

        log.info("== detach_and_merge_thenChange 종료 ==");
    }
}
