package com.chrrubin.cherryrenderer.api;

public class HttpResponse {
    private int code;
    private String description = "";
    private String message = "";

    public HttpResponse(int code) {
        this.code = code;
        switch (code){
            case 200:
                this.description = "OK";
                break;
            case 400:
                this.description = "Bad Request";
                break;
            case 404:
                this.description = "Not Found";
                break;
            case 405:
                this.description = "Method not allowed";
                break;
            case 500:
                this.description = "Internal Server Error";
                break;
        }
    }

    public HttpResponse(int code, String message) {
        this(code);
        this.message = message;
    }
}
