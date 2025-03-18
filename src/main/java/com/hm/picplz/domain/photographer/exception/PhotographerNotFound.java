package com.hm.picplz.domain.photographer.exception;

import com.hm.picplz.global.error.BaseErrorException;

public class PhotographerNotFound extends BaseErrorException {

    public static final PhotographerNotFound EXCEPTION = new PhotographerNotFound();

    private PhotographerNotFound() {
        super(PhotographerErrorCode.PHOTOGRAPHER_NOT_FOUND);
    }
}
