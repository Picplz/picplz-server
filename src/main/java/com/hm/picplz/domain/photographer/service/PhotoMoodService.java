package com.hm.picplz.domain.photographer.service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hm.picplz.domain.photographer.domain.PhotoMood;
import com.hm.picplz.domain.photographer.domain.Photographer;
import com.hm.picplz.domain.photographer.dto.PhotoMoodDto;
import com.hm.picplz.domain.photographer.exception.PhotographerErrorCode;
import com.hm.picplz.domain.photographer.repository.PhotoMoodRepository;
import com.hm.picplz.global.error.ExceptionFactory;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PhotoMoodService {

	private final PhotoMoodRepository photoMoodRepository;

	@Transactional
	public PhotoMoodDto.PhotoMoodResponse createPhotoMoods(List<String> photoMoodContents, Photographer photographer) {
		// 1. null·빈 문자열 제거 & trim 후 중복 제거
		List<String> distinctContents = photoMoodContents.stream()
			.filter(Objects::nonNull)
			.map(String::trim)
			.filter(s -> !s.isEmpty())
			.distinct()
			.toList();

		// 2. 엔티티로 매핑
		List<PhotoMood> photoMoods = distinctContents.stream()
			.map(content -> PhotoMood.builder()
				.photographer(photographer)
				.content(content)
				.build())
			.toList();

		// 3. 저장
		photoMoodRepository.saveAll(photoMoods);

		return PhotoMoodDto.PhotoMoodResponse.from(photoMoods);
	}

	@Transactional
	public void addPhotoMood(String photoMoodContent, Photographer photographer) {
		// 기존 감성 집합 조회
		Set<String> existing = photographer.getPhotoMoods().stream()
			.map(PhotoMood::getContent)
			.collect(Collectors.toSet());

		// 중복이면 아무 것도 추가하지 않음
		if (existing.contains(photoMoodContent)) {
			return;
		}

		// 새 감성 엔티티 생성 & 저장
		PhotoMood newMood = PhotoMood.builder()
			.photographer(photographer)
			.content(photoMoodContent)
			.build();
		photoMoodRepository.save(newMood);
	}

	@Transactional
	public void deletePhotoMood(String photoMoodContent, Photographer photographer) {
		PhotoMood delPhotoMood = photoMoodRepository.findByPhotographerAndContent(photographer, photoMoodContent)
			.orElseThrow(() -> ExceptionFactory.of(PhotographerErrorCode.PHOTOMOOD_NOT_FOUND));
		photoMoodRepository.delete(delPhotoMood);
	}
}
