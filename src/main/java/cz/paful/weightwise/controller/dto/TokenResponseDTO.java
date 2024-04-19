package cz.paful.weightwise.controller.dto;

import java.util.Date;

public class TokenResponseDTO {

    public TokenResponseDTO(String token, Date expiresAt) {
        this.token = token;
        this.expiresAt = expiresAt;
    }

    private String token;
    private Date expiresAt;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Date getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Date expiresAt) {
        this.expiresAt = expiresAt;
    }
}
