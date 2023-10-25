package com.example.loginmicroservizi.exception;

public class NoSuchElementFoundException extends RuntimeException {

    private static final long serialVersionUID=1L;
    private String message = "Risorsa Non Trovata";

    public NoSuchElementFoundException(){
        super();
    }
    public NoSuchElementFoundException(String message){
        super(message);
        this.message=message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}

