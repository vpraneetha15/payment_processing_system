package com.example.dto;

public class ErrorCodeCountDTO {

    private String errorCode;
    private long count;

    public ErrorCodeCountDTO() {
    }

    public ErrorCodeCountDTO(String errorCode, long count) {
        this.errorCode = errorCode;
        this.count = count;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }
}
