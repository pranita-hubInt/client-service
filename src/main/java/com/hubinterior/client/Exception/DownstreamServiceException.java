package com.hubinterior.client.Exception;

import org.springframework.http.HttpStatus;

public class DownstreamServiceException extends ApiException {

    public DownstreamServiceException(String message) {
        super(message, HttpStatus.BAD_GATEWAY, ErrorCode.INTERNAL_SERVER_ERROR);
    }

    public DownstreamServiceException(String message, HttpStatus status) {
        super(message, status, ErrorCode.INTERNAL_SERVER_ERROR);
    }
}
