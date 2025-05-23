package com.hm.picplz.domain.member.repository;

import com.hm.picplz.domain.member.domain.Member;
import com.hm.picplz.domain.member.domain.SocialProvider;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByAttributeCodeAndSocialProvider(String attributeCode, SocialProvider socialProvider);
    boolean existsByNicknameIs(String nickname);

	boolean existsByNicknameIsAndIdNot(@NotNull @Size(max = 30) String nickname, Long id);
}
