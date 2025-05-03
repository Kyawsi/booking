package com.booking.system.exception;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;

public class SystemException extends RuntimeException {
    private String errorCode;
    private Object response;
    private Object source;

    private String customMessage;

    public SystemException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public SystemException(String message) {
        super(message);
        this.errorCode = "400";
        this.customMessage = message;
    }


    public SystemException(String errorCode, String message, Throwable throwable) {
        super(message, throwable);
        throwable.printStackTrace();
        this.errorCode = errorCode;
    }

    public SystemException(String errorCode, Object response, String message) {
        super(message);
        this.errorCode = errorCode;
        this.response = response;
    }



    public SystemException(Throwable e) {
        if(e instanceof SystemException){
            this.errorCode = ((SystemException) e).getErrorCode(); // Extract errorCode from SystemException
           this.customMessage = ((SystemException) e).getCustomMessage();
        }else{
            this.errorCode = ((SystemException) e).getErrorCode(); // Extract errorCode from SystemException
            Throwable rootCause = ExceptionUtils.getRootCause(e);

            this.customMessage = (rootCause != null && rootCause.getMessage() != null)
                    ? rootCause.getMessage()
                    :e.getMessage();
        }

    }

    public SystemException(String errorCode,Throwable e) {
        if(e instanceof SystemException){
            this.errorCode = "400";
            this.customMessage = ((SystemException) e).getCustomMessage();
        } else if (errorCode != null) {
            this.errorCode = errorCode;
            this.customMessage = ((SystemException) e).getCustomMessage();
        } else{
            this.errorCode = "500";
            Throwable rootCause = ExceptionUtils.getRootCause(e);

            this.customMessage = (rootCause != null && rootCause.getMessage() != null)
                    ? rootCause.getMessage()
                    :e.getMessage();
        }

    }


    public SystemException(HttpStatus status,String customMessage) {
        super("");
        Assert.notNull(status, "HttpStatus is required");
        this.errorCode = String.valueOf(status.value());
        this.customMessage = customMessage;

    }


    public String getErrorCode() {
        return errorCode;
    }

    public Object getResponse() {
        return response;
    }

    public Object getSource() {
        return source;
    }

    public void setSource(Object source) {
        this.source = source;
    }

    public String getCustomMessage() {
        return customMessage;
    }
}

