package com.hm.picplz.domain.photographer.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.hm.picplz.domain.photographer.domain.PhotoMood;
import com.hm.picplz.domain.photographer.dto.PhotographerSearchDto;
import com.hm.picplz.global.common.service.WebClientService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hm.picplz.domain.area.domain.Area;
import com.hm.picplz.domain.area.exception.AreaErrorCode;
import com.hm.picplz.domain.following.repository.FollowingRepository;
import com.hm.picplz.domain.member.domain.Member;
import com.hm.picplz.domain.member.domain.Role;
import com.hm.picplz.domain.member.dto.MemberDto;
import com.hm.picplz.domain.member.service.MemberService;
import com.hm.picplz.domain.photographer.domain.ActiveArea;
import com.hm.picplz.domain.photographer.domain.Photographer;
import com.hm.picplz.domain.photographer.domain.PhotographerCamera;
import com.hm.picplz.domain.photographer.dto.PhotoMoodDto;
import com.hm.picplz.domain.photographer.dto.PhotographerDto;
import com.hm.picplz.domain.photographer.exception.PhotographerErrorCode;
import com.hm.picplz.domain.photographer.repository.ActiveAreaRepository;
import com.hm.picplz.domain.area.repository.AreaRepository;
import com.hm.picplz.domain.photographer.dto.DefaultCameraDto;
import com.hm.picplz.domain.photographer.repository.DefaultCameraRepository;
import com.hm.picplz.domain.photographer.repository.PhotographerRepository;
import com.hm.picplz.global.common.entity.YesNo;
import com.hm.picplz.global.error.ExceptionFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class PhotographerService {

	private final MemberService memberService;
	private final PhotoMoodService photoMoodService;
	private final WebClientService webClientService;
	private final PhotographerRepository photographerRepository;
	private final DefaultCameraRepository defaultCameraRepository;
	private final FollowingRepository followingRepository;
	private final ActiveAreaRepository activeAreaRepository;
	private final AreaRepository areaRepository;

	/**
	 * 작가 회원가입 메서드
	 * @param createPhotographerRequest 작가 회원가입을 위한 입력 정보
	 */
	@Transactional
	public PhotographerDto.Detail createPhotographer(PhotographerDto.CreatePhotographerRequest createPhotographerRequest) {
		// 마지막 닉네임 중복 확인
		memberService.checkNickname(createPhotographerRequest.getNickname());

		// 1. social_code로 기존 Member 조회 또는 생성
		Member member = memberService.findOrCreateMember(
			MemberDto.CreateMemberRequest.of(createPhotographerRequest, Role.PHOTOGRAPHER)
		);

		// 2. 이미 Photographer 프로필이 있는지 확인
		if (member.getPhotographer() != null) {
			throw ExceptionFactory.of(PhotographerErrorCode.ALREADY_PHOTOGRAPHER);
		}

		// 3. Photographer 프로필 생성 (active = 'Y')
		Photographer photographer = Photographer.from(member);
		photographerRepository.save(photographer);
		member.updateRole(Role.PHOTOGRAPHER);

		// 작가 분위기 키워드
		createPhotoMoods(createPhotographerRequest.getPhotoMoods(), photographer);
		// 작가 주 촬영지
		createActiveAreas(createPhotographerRequest.getActiveAreas(), photographer);
		// 작가 촬영 카메라
		createPhotographerCameras(createPhotographerRequest.getCameras(), photographer);

		return PhotographerDto.Detail.of(photographer);
	}

	/**
	 * 닉네임으로 작가 검색
	 *
	 * @param keyword 검색창에 입력한 단어
	 * @return 검색된 작가 정보 리스트
	 */
	@Transactional(readOnly = true)
	public Page<PhotographerSearchDto> searchPhotographers(String keyword, Pageable pageable) {

		Page<Photographer> photographerPage =
				photographerRepository.findByMember_NicknameContaining(keyword, pageable);

		return photographerPage.map(PhotographerSearchDto::of);
	}

	/**
	 * 작가 상세 정보 반환
	 * @param photographerId 조회하려는 작가 정보
	 * @param memberId 조회를 시도하는 회원 정보(팔로우 여부를 위해)
	 * @return 작가 상세 정보
	 */
	@Transactional(readOnly = true)
	public PhotographerDto.Detail getPhotographerDetail(Long photographerId, Long memberId) {
		Photographer photographer = getPhotographerByPhotographerId(photographerId);
		int followersCount = getFollowers(photographer);
		YesNo isFollowing = isFollowing(photographer, memberId);
		return PhotographerDto.Detail.of(photographer, followersCount, isFollowing);
	}

	/**
	 * 작가의 분위기 키워드 추가
	 * @param addPhotoMoodDto 분위기 키워드 1개
	 * @param memberId 작가 member Id(토큰에서 파싱)
	 */
	@Transactional
	public void addPhotoMood(PhotoMoodDto.PhotoMoodRequest addPhotoMoodDto, Long memberId) {
		Photographer photographer = getPhotographerByMemberId(memberId);
		photoMoodService.addPhotoMood(addPhotoMoodDto.getPhotoMood(), photographer);
	}

	/**
	 * 작가의 분위기 키워드 삭제
	 * (삭제하려는 분위기의 pk를 받아도 될 것 같긴 합니다. 그러려면 분위기 조회 응답값을 바꿔야합니다.)
	 * @param deletePhotoMoodDto 분위기 키워드 1개
	 * @param memberId 작가 member Id(토큰에서 파싱)
	 */
	@Transactional
	public void deletePhotoMood(PhotoMoodDto.PhotoMoodRequest deletePhotoMoodDto, Long memberId) {
		Photographer photographer = getPhotographerByMemberId(memberId);
		photoMoodService.deletePhotoMood(deletePhotoMoodDto.getPhotoMood(), photographer);
	}

	/**
	 * 현재 멤버의 위치 = 주 활동지역 작가 탐색
	 * @param memberId 조회를 진행한 멤버
	 * @return 작가 상세 정보
	 */
	@Transactional
	public List<PhotographerDto.Detail> getPhotographersByActiveArea(Long memberId) {
		Long areaId = webClientService.requestAreaIdByPoint(
				memberService.getMemberLocation(memberId)
		);

		return activeAreaRepository.findAllByAreaIdWithPhotographer(areaId).stream()
				.map(ActiveArea::getPhotographer)
				.map(photographer -> {
					int followersCount = getFollowers(photographer);
					YesNo isFollowing = isFollowing(photographer, memberId);
					return PhotographerDto.Detail.of(photographer, followersCount, isFollowing);
				})
				.toList();
	}

	/**
	 * 작가의 주 활동지역 변경(교체)
	 * @param memberId 변경이 필요한 멤버
	 * @param updateActiveAreaRequestDto 바꾸고자 하는 주 활동지역
	 * @return 변경된 주 활동지역
	 */
	@Transactional
	public PhotographerDto.UpdateActiveAreaResponse updateActiveArea(Long memberId, PhotographerDto.UpdateActiveAreaRequest updateActiveAreaRequestDto) {
		Photographer photographer = getPhotographerByMemberId(memberId);
		photographer.removeAllActiveArea();
		createActiveAreas(updateActiveAreaRequestDto.getAreas(), photographer);
		return PhotographerDto.UpdateActiveAreaResponse.from(photographer);
	}

	/**
	 * 작가 enitty 데이터 조회 메서드
	 * @param memberId 작가의 member pk
	 * @return 작가 엔티티
	 */
	public Photographer getPhotographerByMemberId(Long memberId) {
		return photographerRepository.findByMemberId(memberId)
				.orElseThrow(() -> ExceptionFactory.of(PhotographerErrorCode.PHOTOGRAPHER_NOT_FOUND));
	}

	public List<DefaultCameraDto.CameraInfo> getCameras() {
		return defaultCameraRepository.findAll().stream().map(DefaultCameraDto.CameraInfo::from).toList();
	}

	/**
	 * 작가의 분위기 키워드 목록 받아 1:N 관계 테이블 저장
	 * @param photoMoodContents 분위기 키워드
	 * @param photographer 작가
	 */
	private void createPhotoMoods(List<String> photoMoodContents, Photographer photographer) {
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

		photographer.addAllPhotoMoods(photoMoods);
	}

	/**
	 * 작가의 주 촬영지(법정동 데이터 기반) 목록을 받아 N:M 관계 테이블에 저장
	 * @param areaDtos 주 촬영지 목록
	 * @param photographer 작가
	 */
	private void createActiveAreas(List<PhotographerDto.ActiveAreaRequest> areaDtos, Photographer photographer) {

		List<ActiveArea> activeAreas = new ArrayList<>();

		for (PhotographerDto.ActiveAreaRequest dto : areaDtos) {
			Area area = areaRepository.findById(dto.getCode())
					.orElseThrow(() -> ExceptionFactory.of(AreaErrorCode.WRONG_AREA_CODE));

			ActiveArea activeArea = ActiveArea.builder()
					.photographer(photographer)
					.area(area)
					.priority(dto.getPriority())
					.build();

			activeAreas.add(activeArea);
		}
		photographer.addAllActiveAreas(activeAreas);
	}

	/**
	 * 작가의 촬영 기기 1:N 테이블 저장
	 * @param cameraDtos 작가의 촬영 기기
	 * @param photographer 작가
	 */
	private void createPhotographerCameras(List<PhotographerDto.PhotographerCameraRequest> cameraDtos, Photographer photographer) {
		List<PhotographerCamera> cameras = new ArrayList<>();

		for (PhotographerDto.PhotographerCameraRequest dto : cameraDtos) {
			PhotographerCamera camera = PhotographerCamera.builder()
					.photographer(photographer)
					.type(dto.getType())
					.brand(dto.getBrand())
					.name(dto.getName())
					.cameraType(dto.getCameraType())
					.build();
			cameras.add(camera);
		}

		photographer.addAllCameras(cameras);
	}

	/**
	 * 작가 entity 데이터 조회 메서드
	 * @param photographerId 작가 pk
	 * @return 작가 엔티티
	 */
	private Photographer getPhotographerByPhotographerId(Long photographerId) {
		return photographerRepository.findPhotographerWithMoods(photographerId)
				.orElseThrow(() -> ExceptionFactory.of(PhotographerErrorCode.PHOTOGRAPHER_NOT_FOUND));
	}

	/**
	 * 작가의 팔로워 수를 조회
	 * @param photographer 조회하려는 작가
	 * @return 작가의 팔로워 수
	 */
	private int getFollowers(Photographer photographer) {
		return Math.toIntExact(followingRepository.countByFollowingId(photographer.getMember().getId()));
	}

	/**
	 * 작가를 팔로우하고 있는지 확인
	 * @param photographer 작가
	 * @param memberId 로그인한 회원 pk
	 * @return 팔로우 여부
	 */
	private YesNo isFollowing(Photographer photographer, Long memberId) {
		return Boolean.TRUE.equals(
				followingRepository.existsByFollowingIdAndFollowerId(photographer.getMember().getId(), memberId)) ?
				YesNo.Y : YesNo.N;
	}
}
