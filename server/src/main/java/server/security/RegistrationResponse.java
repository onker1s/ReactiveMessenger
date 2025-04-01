package server.security;

import lombok.Data;

@Data
public class RegistrationResponse {
    private String status;
    public void confirm() {this.status = "confirmed";}
    public void cancel() {this.status = "cancelled";}

}
