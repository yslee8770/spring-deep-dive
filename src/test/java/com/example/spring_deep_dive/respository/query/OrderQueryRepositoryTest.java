package com.example.spring_deep_dive.respository.query;

import com.example.spring_deep_dive.domain.member.Member;
import com.example.spring_deep_dive.domain.order.Order;
import com.example.spring_deep_dive.respository.MemberRepository;
import com.example.spring_deep_dive.respository.OrderRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class OrderQueryRepositoryTest {

    @Autowired MemberRepository memberRepository;
    @Autowired OrderRepository orderRepository;
    @Autowired OrderQueryRepository orderQueryRepository;

    Member m1;
    Member m2;

    @BeforeEach
    void setUp() {
        m1 = Member.createUser("user1@test.com", "pw", "user1");
        m2 = Member.createUser("user2@test.com", "pw", "user2");

        memberRepository.save(m1);
        memberRepository.save(m2);

        orderRepository.save(Order.create(m1, 10000));
        orderRepository.save(Order.create(m1, 12000));
        orderRepository.save(Order.create(m2, 5000));
    }

    @Test
    void findOrdersWithMember() {
        List<Order> result = orderQueryRepository.findOrdersWithMember();
        assertThat(result).hasSize(3);
    }

    @Test
    void findByMemberEmail() {
        List<Order> result = orderQueryRepository.findByMemberEmail("user1@test.com");
        assertThat(result).hasSize(2);
    }

    @Test
    void findOrdersWithMember_fetchJoin_prevents_additional_select() {
        List<Order> result = orderQueryRepository.findOrdersWithMemberFetchJoin();
        assertThat(result).hasSize(3);

        result.forEach(o -> o.getMember().getEmail());
    }


}