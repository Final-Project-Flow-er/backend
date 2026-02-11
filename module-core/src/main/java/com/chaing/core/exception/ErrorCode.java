package com.chaing.core.exception;

public interface ErrorCode {

    Integer getStatus();
    String getCode();
    String getMessage();
}
