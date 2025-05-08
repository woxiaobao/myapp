package org.start.app.config;

import org.apache.catalina.connector.ClientAbortException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 处理客户端中断异常
    @ExceptionHandler({ClientAbortException.class})
    public void handleClientAbortException(ClientAbortException e) {
        logger.debug("Client aborted the connection: {}", e.getMessage());
    }

    // 处理IO异常
    @ExceptionHandler({IOException.class})
    public void handleIOException(IOException e) {
        if ("Connection reset by peer".equals(e.getMessage())) {
            logger.debug("Client reset the connection: {}", e.getMessage());
        } else {
            logger.error("IO Exception occurred: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    // 处理参数校验异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage())
        );
        logger.warn("Validation failed: {}", errors);
        return ResponseEntity.badRequest().body(errors);
    }

    // 处理请求参数绑定异常
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ResponseEntity<Map<String, String>> handleBindException(BindException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage())
        );
        logger.warn("Parameter binding failed: {}", errors);
        return ResponseEntity.badRequest().body(errors);
    }

    // 处理参数类型不匹配异常
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ResponseEntity<String> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String error = String.format("Parameter '%s' should be of type %s", 
            ex.getName(), ex.getRequiredType().getSimpleName());
        logger.warn("Type mismatch: {}", error);
        return ResponseEntity.badRequest().body(error);
    }

    // 处理缺少必需参数异常
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ResponseEntity<String> handleMissingParams(MissingServletRequestParameterException ex) {
        String error = String.format("Missing required parameter '%s' of type %s", 
            ex.getParameterName(), ex.getParameterType());
        logger.warn("Missing parameter: {}", error);
        return ResponseEntity.badRequest().body(error);
    }

    // 处理请求方法不支持异常
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ResponseBody
    public ResponseEntity<String> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        String error = String.format("Method '%s' is not supported. Supported methods are: %s", 
            ex.getMethod(), String.join(", ", ex.getSupportedMethods()));
        logger.warn("Method not allowed: {}", error);
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(error);
    }

    // 处理请求体解析异常
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ResponseEntity<String> handleMessageNotReadable(HttpMessageNotReadableException ex) {
        logger.warn("Message not readable: {}", ex.getMessage());
        return ResponseEntity.badRequest().body("Invalid request body");
    }

    // 处理文件上传大小超限异常
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ResponseEntity<String> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
        logger.warn("File upload size exceeded: {}", ex.getMessage());
        return ResponseEntity.badRequest().body("File size exceeds maximum allowed size");
    }

    // 处理数据访问异常
    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ResponseEntity<String> handleDataAccessException(DataAccessException ex) {
        logger.error("Database error occurred: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("An error occurred while accessing the database");
    }

    // 处理约束违反异常
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ResponseEntity<Map<String, String>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> 
            errors.put(violation.getPropertyPath().toString(), violation.getMessage())
        );
        logger.warn("Constraint violation: {}", errors);
        return ResponseEntity.badRequest().body(errors);
    }

    // 处理其他未预期的异常
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ResponseEntity<String> handleUnexpectedException(Exception ex) {
        logger.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("An unexpected error occurred");
    }
} 