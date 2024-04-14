package cz.paful.weightwise.service;

import cz.paful.weightwise.controller.dto.UserRegistrationDTO;
import cz.paful.weightwise.data.dto.UserWeightDTO;
import cz.paful.weightwise.data.jpa.UserWeight;
import cz.paful.weightwise.data.jpa.UserWeightRepository;
import cz.paful.weightwise.util.JwtTokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoField;
import java.util.Date;


@Service
public class UserService implements UserDetailsService {
    private final UserWeightRepository userWeightRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;


    @Autowired
    public UserService(UserWeightRepository userWeightRepository, PasswordEncoder passwordEncoder, JwtTokenUtil jwtTokenUtil) {
        this.userWeightRepository = userWeightRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenUtil = jwtTokenUtil;
    }

    public void registerNewUser(UserRegistrationDTO userRegistrationDTO) {
        boolean exits = userWeightRepository.existsByUsername(userRegistrationDTO.getUsername());
        if (exits) {
            throw new DuplicateKeyException("Username already exists.");
        }

        UserWeight userWeight = new UserWeight();
        userWeight.setUsername(userRegistrationDTO.getUsername());
        userWeight.setPassword(passwordEncoder.encode(userRegistrationDTO.getPassword()));

        userWeightRepository.save(userWeight);
    }

    public String login(UserRegistrationDTO userRegistrationDTO) {
        UserWeight userWeight = userWeightRepository.findUserWeightByUsername(userRegistrationDTO.getUsername());

        if (userWeight == null || !passwordEncoder.matches(userRegistrationDTO.getPassword(), userWeight.getPassword())) {
            throw new BadCredentialsException("Wrong username or password");
        }

        // Right password we can generate token
        userWeight.setLastLogin(new Date().toInstant());
        userWeight.setToken(jwtTokenUtil.generateToken(userRegistrationDTO.getUsername()));
        userWeightRepository.save(userWeight);

        return userWeight.getToken();
    }

    public String getUserNameByAuthorizationHeader(HttpServletRequest request) {
        final String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String jwt = authorizationHeader.substring(7);

            return getUsernameByToken(jwt);
        }

        return null;
    }

    public String getUsernameByToken(String token) {
        String username = jwtTokenUtil.getUsername(token);

        if (username != null && jwtTokenUtil.validateToken(token)) {
            return username;
        }

        return null;
    }

    @Override
    public UserWeightDTO loadUserByUsername(String username) throws UsernameNotFoundException {
        UserWeight userWeight = userWeightRepository.findUserWeightByUsername(username);
        if (userWeight == null) {
            throw new UsernameNotFoundException(username);
        }

        return new UserWeightDTO(userWeight);
    }
}
