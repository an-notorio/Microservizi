package com.example.loginmicroservizi.exception;

public class BadRequestException extends RuntimeException{
    private static final long serialVersionUID=1L;
    private String message = "Inserimento Dati Errato";

    public BadRequestException(){
        super();
    }
    public BadRequestException(String message){
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
