package com.example.spring_deep_dive.domain.member;

import com.example.spring_deep_dive.domain.BaseEntity;
import com.example.spring_deep_dive.domain.order.Order;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    private String email;

    private String name;

    private String password;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Order> orders = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private Role role;


    @Builder
    private Member(String email, String name, String password ,Role role) {
        this.email = email;
        this.name = name;
        this.password = password;
        this.role = role;
    }

    public static Member createUser(String email, String encodedPassword, String name) {
        return Member.builder()
                .email(email)
                .name(name)
                .password(encodedPassword)
                .role(Role.USER)
                .build();
    }

}
