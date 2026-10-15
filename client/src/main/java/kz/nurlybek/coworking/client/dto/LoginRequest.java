package kz.nurlybek.coworking.client.dto;

public class LoginRequest {
    public String email;
    public String password;

    public LoginRequest() {}

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }
}