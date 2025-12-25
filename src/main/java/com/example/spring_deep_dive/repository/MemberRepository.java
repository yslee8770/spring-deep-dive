package com.example.spring_deep_dive.repository;

import com.example.spring_deep_dive.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

}
