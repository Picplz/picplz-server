package com.hm.picplz.global.common.response;

import com.hm.picplz.global.error.ErrorReason;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@Slf4j
@RestControllerAdvice
public class ResponseWrapper implements ResponseBodyAdvice<Object> {

    private static final List<String> SWAGGER_EXCLUDED_PATHS = List.of(
            "/api/v1/v3/api-docs",
            "/swagger-ui",
            "/swagger-resources",
            "/webjars"
    );

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // 모든 타입 반환 가능
        return true;
    }

    /**
     *  HttpMessageConverter가 선택된 후 해당 write 메서드가 호출되기 직전에 호출
     */
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType,
                                  MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {
        // HttpServletResponse로 상태 코드 가져오기
        HttpServletRequest servletRequest = ((ServletServerHttpRequest) request).getServletRequest();
        HttpServletResponse servletResponse = ((ServletServerHttpResponse) response).getServletResponse();
        int statusCode = servletResponse.getStatus();
        String uri = servletRequest.getRequestURI();

        // Swagger 및 OpenAPI 관련 엔드포인트는 래핑하지 않고 그대로 반환
        if (isSwaggerRequest(uri)) {
            return body;
        }

        // response fail
        if (statusCode >= 400) {
            ErrorReason errorReason = (ErrorReason) servletRequest.getAttribute("errorReason");
            if (errorReason != null) {
                return CommonResponse.ErrorResponse.from(errorReason);
            } else {
                return CommonResponse.ErrorResponse.builder()
                        .statusCode(statusCode)
                        .message("fail")
                        .build();
            }
        }

        // response success
        return CommonResponse.SuccessResponse.builder()
                .statusCode(statusCode)
                .message("success")
                .data(body)
                .build();
    }

    private boolean isSwaggerRequest(String uri) {
        return SWAGGER_EXCLUDED_PATHS.stream().anyMatch(uri::startsWith);
    }

}