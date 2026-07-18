package com.hm.picplz.global.common.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum YesNo {

    N("N"), Y("Y");

    @JsonValue
    private final String value;

    @JsonCreator
    public static YesNo parse(String inputValue) {
        return Stream.of(YesNo.values())
                .filter(confirm -> confirm.getValue().equals(inputValue))
                .findFirst()
                .orElse(null);
    }
}
