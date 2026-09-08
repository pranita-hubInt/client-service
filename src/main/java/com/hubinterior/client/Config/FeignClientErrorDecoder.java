package com.hubinterior.client.Config;

import com.hubinterior.client.Exception.*;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Component
public class FeignClientErrorDecoder implements ErrorDecoder {

    private static final Logger log = LoggerFactory.getLogger(FeignClientErrorDecoder.class);
    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        String responseBody = extractBody(response);
        log.warn("Feign call failed on method [{}]. Status: {}, Body: {}", methodKey, response.status(), responseBody);

        String message = (responseBody != null && !responseBody.isBlank())
                ? responseBody
                : "Error occurred while calling core microservice (" + response.reason() + ")";

        return switch (response.status()) {
            case 400 -> new BusinessRuleException("Core Service Validation Error: " + message);
            case 404 -> new ResourceNotFoundException("Requested resource was not found in core service.");
            case 409 -> new DuplicateResourceException("Conflict in core service: " + message);
            case 401 -> new UnauthorizedException("Unauthorized access to core service.");
            case 403 -> new ForbiddenException("Forbidden access to core service resource.");
            case 500, 502, 503, 504 -> new DownstreamServiceException(
                    "Core service temporarily unavailable (status: " + response.status() + "). Please try again later."
            );
            default -> defaultDecoder.decode(methodKey, response);
        };
    }

    private String extractBody(Response response) {
        if (response.body() == null) return null;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body().asInputStream(), StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            log.error("Failed to read Feign response body", e);
            return null;
        }
    }
}
