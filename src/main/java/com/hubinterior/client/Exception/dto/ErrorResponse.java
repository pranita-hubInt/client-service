package com.hubinterior.client.Exception.dto;

import com.hubinterior.client.Exception.ErrorCode;

import java.time.LocalDateTime;

public record ErrorResponse(
        int status,
        ErrorCode errorCode,
        String message,
        LocalDateTime timestamp
) {}
