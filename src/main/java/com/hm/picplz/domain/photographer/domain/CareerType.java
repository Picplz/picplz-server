package com.hm.picplz.domain.photographer.domain;

import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.hm.picplz.domain.photographer.exception.PhotographerErrorCode;
import com.hm.picplz.global.error.ExceptionFactory;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CareerType {
	PHOTO_MAJOR, EARN_PROFIT, SNS_MANAGE;

	@JsonValue
	public String toJson() {return name().toLowerCase();}

	@JsonCreator
	public static CareerType parse(String input) {
		return Stream.of(CareerType.values())
			.filter(careerType -> careerType.name().equalsIgnoreCase(input))
			.findFirst()
			.orElseThrow(() -> ExceptionFactory.of(PhotographerErrorCode.WRONG_CAREER_TYPE));
	}
}
