package com.hm.picplz.domain.photographer.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hm.picplz.domain.photographer.domain.PhotoMood;
import com.hm.picplz.domain.photographer.domain.Photographer;

public interface PhotoMoodRepository extends JpaRepository<PhotoMood, Long> {
	Optional<PhotoMood> findByPhotographerAndContent(Photographer photographer, String content);
}
