package com.hm.picplz.domain.portfolio.exception;

import com.hm.picplz.global.error.BaseErrorCode;
import com.hm.picplz.global.error.ErrorReason;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Getter
@AllArgsConstructor
public enum PortfolioErrorCode implements BaseErrorCode {

    /* Portfolio */
    PORTFOLIO_NOT_FOUND(NOT_FOUND, "PORTFOLIO_404_1", "해당 포트폴리오가 존재하지 않습니다."),

    /* Scrap */
    SCRAP_ALREADY_EXISTS(CONFLICT, "SCRAP_409_1", "해당 스크랩이 이미 존재합니다."),
    SCRAP_NOT_FOUND(NOT_FOUND, "SCRAP_404_1", "해당 스크랩이 존재하지 않습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String reason;

    @Override
    public ErrorReason getErrorReason() {
        return ErrorReason.of(status.value(), code, reason);
    }
}
