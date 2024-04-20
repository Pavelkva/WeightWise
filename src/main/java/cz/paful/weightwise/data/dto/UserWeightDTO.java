package cz.paful.weightwise.data.dto;

import cz.paful.weightwise.data.jpa.UserWeight;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;

public class UserWeightDTO implements UserDetails {

    public UserWeightDTO() {
    }

    public UserWeightDTO(UserWeight userWeight) {
        setId(userWeight.getId());
        setPassword(userWeight.getPassword());
        setToken(userWeight.getToken());
        setUsername(userWeight.getUsername());
        setLastLogin(userWeight.getLastLogin());
        setLastImport(userWeight.getLastImport());
    }

    private Long id;
    private String username;
    private String password;
    private String token;
    private Instant lastLogin;
    private Instant lastImport;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Instant getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Instant lastLogin) {
        this.lastLogin = lastLogin;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public Instant getLastImport() {
        return lastImport;
    }

    public void setLastImport(Instant lastImport) {
        this.lastImport = lastImport;
    }
}
