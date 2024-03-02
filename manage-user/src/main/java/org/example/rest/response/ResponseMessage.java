package org.example.rest.response;

public class ResponseMessage {
    private String message;
    private String code;

    public ResponseMessage(String message, String code) {
        this.message = message;
        this.code = code;
    }

    public ResponseMessage() {
    }

    public String getMessage() {
        return message;
    }

    public String getCode() {
        return code;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
