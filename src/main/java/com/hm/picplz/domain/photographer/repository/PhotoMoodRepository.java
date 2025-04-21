package com.hm.picplz.domain.photographer.repository;

import java.util.Optional;

import com.hm.picplz.domain.photographer.domain.PhotoMood;
import com.hm.picplz.domain.photographer.domain.Photographer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PhotoMoodRepository extends JpaRepository<PhotoMood, Long> {
	Optional<PhotoMood> findByPhotographerAndContent(Photographer photographer, String content);
}
