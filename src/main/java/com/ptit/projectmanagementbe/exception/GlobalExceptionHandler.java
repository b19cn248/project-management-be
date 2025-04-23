package com.ptit.projectmanagementbe.exception;

import com.ptit.projectmanagementbe.constant.ErrorCode;
import com.ptit.projectmanagementbe.dto.common.ApiResponse;
import com.ptit.projectmanagementbe.dto.common.ErrorResponse;
import com.ptit.projectmanagementbe.util.MessageUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageUtil messageUtil;
    private final MessageSource messageSource;

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            HttpServletRequest request,
            Locale locale
    ) {
        log.error("Resource not found exception: {}", ex.getMessage());
        String message = messageUtil.getMessage(ex.getMessage(), locale, ex.getArgs());


        return ApiResponse.error(ex.getCode(), message);
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<ErrorResponse> handleBadRequestException(
            BadRequestException ex,
            HttpServletRequest request,
            Locale locale
    ) {
        log.error("Bad request exception: {}", ex.getMessage());
        String message = messageUtil.getMessage(ex.getMessage(), locale, ex.getArgs());


        return ApiResponse.error(ex.getCode(), message);
    }

    @ExceptionHandler(ForbiddenException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResponse<ErrorResponse> handleForbiddenException(
            ForbiddenException ex,
            HttpServletRequest request,
            Locale locale
    ) {
        log.error("Forbidden exception: {}", ex.getMessage());
        String message = messageUtil.getMessage(ex.getMessage(), locale, ex.getArgs());


        return ApiResponse.error(ex.getCode(), message);
    }

    @ExceptionHandler(ApplicationException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<ErrorResponse> handleApplicationException(
            ApplicationException ex,
            HttpServletRequest request,
            Locale locale
    ) {
        log.error("Application exception: {}", ex.getMessage());
        String message = messageUtil.getMessage(ex.getMessage(), locale, ex.getArgs());


        return ApiResponse.error(ex.getCode(), message);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request,
            Locale locale
    ) {
        log.error("Validation exception: {}", ex.getMessage());


        String message = messageUtil.getMessage("validation.error", locale);


        return ApiResponse.error(ErrorCode.VALIDATION_ERROR, message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException ex,
            HttpServletRequest request,
            Locale locale
    ) {
        log.error("Constraint violation exception: {}", ex.getMessage());

        List<ErrorResponse.ValidationError> validationErrors = ex.getConstraintViolations()
                .stream()
                .map(violation -> createValidationError(violation, locale))
                .collect(Collectors.toList());

        String message = messageUtil.getMessage("validation.error", locale);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .code(ErrorCode.VALIDATION_ERROR)
                .message(message)
                .path(request.getRequestURI())
                .errors(validationErrors)
                .build();

        return ApiResponse.error(ErrorCode.VALIDATION_ERROR, message);
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<ErrorResponse> handleBindException(
            BindException ex,
            HttpServletRequest request,
            Locale locale
    ) {
        log.error("Bind exception: {}", ex.getMessage());

        List<ErrorResponse.ValidationError> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> createValidationError(fieldError, locale))
                .collect(Collectors.toList());

        String message = messageUtil.getMessage("validation.error", locale);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .code(ErrorCode.VALIDATION_ERROR)
                .message(message)
                .path(request.getRequestURI())
                .errors(validationErrors)
                .build();

        return ApiResponse.error(ErrorCode.VALIDATION_ERROR, message);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<ErrorResponse> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request,
            Locale locale
    ) {
        log.error("Method argument type mismatch exception: {}", ex.getMessage());

        String message = messageUtil.getMessage(
                "method.argument.type.mismatch",
                locale,
                ex.getName(),
                ex.getValue(),
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown"
        );

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .code(ErrorCode.BAD_REQUEST)
                .message(message)
                .path(request.getRequestURI())
                .build();

        return ApiResponse.error(ErrorCode.BAD_REQUEST, message);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<ErrorResponse> handleMaxUploadSizeExceededException(
            MaxUploadSizeExceededException ex,
            HttpServletRequest request,
            Locale locale
    ) {
        log.error("Max upload size exceeded exception: {}", ex.getMessage());

        String message = messageUtil.getMessage("file.size.exceeded", locale);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .code(ErrorCode.FILE_SIZE_EXCEEDED)
                .message(message)
                .path(request.getRequestURI())
                .build();

        return ApiResponse.error(ErrorCode.FILE_SIZE_EXCEEDED, message);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiResponse<ErrorResponse> handleGlobalException(
            Exception ex,
            HttpServletRequest request,
            Locale locale
    ) {
        log.error("Unhandled exception: ", ex);

        String message = messageUtil.getMessage("internal.server.error", locale);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .code(ErrorCode.INTERNAL_SERVER_ERROR)
                .message(message)
                .path(request.getRequestURI())
                .build();

        return ApiResponse.error(ErrorCode.INTERNAL_SERVER_ERROR, message);
    }

    private ErrorResponse.ValidationError createValidationError(FieldError fieldError, Locale locale) {
        String message = messageSource.getMessage(fieldError, locale);
        return ErrorResponse.ValidationError.builder()
                .field(fieldError.getField())
                .message(message)
                .build();
    }

    private ErrorResponse.ValidationError createValidationError(ConstraintViolation<?> violation, Locale locale) {
        String propertyPath = violation.getPropertyPath().toString();
        String field = propertyPath.substring(propertyPath.lastIndexOf('.') + 1);
        return ErrorResponse.ValidationError.builder()
                .field(field)
                .message(violation.getMessage())
                .build();
    }
}
