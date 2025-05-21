package com.hm.picplz.domain.member;

import com.hm.picplz.domain.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByAttributeCodeAndProvider(String attributeCode, String provider);
    boolean existsByNicknameIs(String nickname);
}
