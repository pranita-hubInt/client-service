package com.hubinterior.client.Exception.dto;

import com.hubinterior.client.Exception.ErrorCode;

import java.time.LocalDateTime;
import java.util.Map;

public record ValidationErrorResponse(
        int status,
        ErrorCode errorCode,
        String message,
        Map<String, String> errors,
        LocalDateTime timestamp
) {}
