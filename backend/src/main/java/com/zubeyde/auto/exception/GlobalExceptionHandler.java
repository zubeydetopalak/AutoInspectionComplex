package com.zubeyde.auto.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ProblemDetail> handleRuntimeException(RuntimeException ex, WebRequest request) {

        // Hata mesajını exception'dan alıyoruz
        String errorMessage = ex.getMessage();

        // Cevap nesnesini oluşturuyoruz
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, errorMessage);
        problemDetail.setTitle("Beklenmeyen Çalışma Zamanı Hatası");
        problemDetail.setProperty("path", request.getDescription(false));

        // ResponseEntity ile dönüş yapıyoruz
        return new ResponseEntity<>(problemDetail, HttpStatus.BAD_REQUEST);
    }
}