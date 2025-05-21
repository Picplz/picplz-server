package com.hm.picplz.domain.member;

import com.hm.picplz.domain.member.domain.Member;
import com.hm.picplz.domain.member.domain.SocialProvider;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByAttributeCodeAndSocialProvider(String attributeCode, SocialProvider socialProvider);
    boolean existsByNicknameIs(String nickname);
}
