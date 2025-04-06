package com.hm.picplz.domain.photographer.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hm.picplz.domain.photographer.domain.PhotoMood;
import com.hm.picplz.domain.photographer.domain.Photographer;
import com.hm.picplz.domain.photographer.dto.PhotoMoodDto;
import com.hm.picplz.domain.photographer.repository.PhotoMoodRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PhotoMoodService {

    private final PhotoMoodRepository photoMoodRepository;

    @Transactional
    public List<PhotoMoodDto.PhotoMoodRes> createPhotoMood(List<String> photoMoodContents, Photographer photographer) {
        List<PhotoMood> photoMoods = photoMoodContents.stream()
                .map(photoMood ->
                        PhotoMood.builder()
                                .photographer(photographer)
                                .content(photoMood)
                                .build())
                .toList();

        photoMoodRepository.saveAll(photoMoods);
        return photoMoods.stream().map(PhotoMoodDto.PhotoMoodRes::of).toList();
    }
}
