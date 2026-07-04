package com.AfriPass.afripass.DTOs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder@AllArgsConstructor
public class ApiResponse<T> {

    private LocalDateTime timeStamp;

    private int status;

    private String message;

    private T data;


    public static <T> ApiResponse<T> success(String message , T data){

        return ApiResponse.<T>builder()
                .timeStamp(LocalDateTime.now())
                .status(200)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse <T> error(String message , int status , T data){

        return ApiResponse.<T>builder()
                .timeStamp(LocalDateTime.now())
                .status(status)
                .message(message)
                .data(data)
                .build();
    }
    public static <T> ApiResponse<T> error(String message, int status) {
        return error(message, status, null);
    }
}