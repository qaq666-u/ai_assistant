package com.liys.ai_assistant.common;

/*
*
*   自定义异常
* */
public class ApiException extends RuntimeException {


    public ApiException(String message) {
        super(message);
    }
    public ApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
