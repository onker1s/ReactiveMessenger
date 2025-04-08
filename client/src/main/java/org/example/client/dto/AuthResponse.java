package org.example.client.dto;

import lombok.Data;

@Data
public class AuthResponse {
    private String status;
    private String token;


    public void confirm(String token) {
        this.status = "confirmed";
        this.token = token;
    }
    public void confirm() {
        this.status = "confirmed";
    }
    public void cancel() {
        this.status = "cancelled";
       // this.token = null;
    }
}

