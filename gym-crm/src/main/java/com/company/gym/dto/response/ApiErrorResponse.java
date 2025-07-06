package com.company.gym.dto.response;

import org.slf4j.MDC;

public class ApiErrorResponse {
    private String error;
    private String message;
    private String transactionId;

    public ApiErrorResponse(String error, String message) {
        this.error = error;
        this.message = message;
        this.transactionId = MDC.get("transactionId");
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
}
