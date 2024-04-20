package cz.paful.weightwise.service;

import cz.paful.weightwise.WeightWiseConfig;
import cz.paful.weightwise.controller.dto.TokenResponseDTO;
import cz.paful.weightwise.controller.dto.UserRegistrationDTO;
import cz.paful.weightwise.data.dto.UserWeightDTO;
import cz.paful.weightwise.data.jpa.UserWeight;
import cz.paful.weightwise.data.jpa.UserWeightRepository;
import cz.paful.weightwise.util.JwtTokenUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;


@Service
public class UserService implements UserDetailsService {
    private final UserWeightRepository userWeightRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;
    private final CacheManager cacheManager;

    @Autowired
    public UserService(UserWeightRepository userWeightRepository, PasswordEncoder passwordEncoder, JwtTokenUtil jwtTokenUtil, CacheManager cacheManager) {
        this.userWeightRepository = userWeightRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenUtil = jwtTokenUtil;
        this.cacheManager = cacheManager;
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

    public TokenResponseDTO login(UserRegistrationDTO userRegistrationDTO) {
        UserWeight userWeight = userWeightRepository.findUserWeightByUsername(userRegistrationDTO.getUsername());

        if (userWeight == null || !passwordEncoder.matches(userRegistrationDTO.getPassword(), userWeight.getPassword())) {
            throw new BadCredentialsException("Wrong username or password");
        }

        TokenResponseDTO tokenResponseDTO = jwtTokenUtil.generateToken(userRegistrationDTO.getUsername());

        // Right password we can generate token
        userWeight.setLastLogin(new Date().toInstant());
        userWeight.setToken(tokenResponseDTO.getToken());
        userWeightRepository.save(userWeight);

        return tokenResponseDTO;
    }

    public String getUserNameByAuthorizationHeader(HttpServletRequest request) {
        return getUsernameByToken(getTokenFromAuthorizationHeader(request));
    }

    public String getUsernameByToken(String token) {
        if (token == null) {
            return null;
        }

        String username = jwtTokenUtil.getUsername(token);

        if (username != null && jwtTokenUtil.validateToken(token)) {
            return username;
        }

        return null;
    }

    @Cacheable(WeightWiseConfig.USER_WEIGHT_CACHE_KEY)
    @Override
    public UserWeightDTO loadUserByUsername(String username) throws UsernameNotFoundException {
        UserWeight userWeight = userWeightRepository.findUserWeightByUsername(username);
        if (userWeight == null) {
            throw new UsernameNotFoundException(username);
        }

        return new UserWeightDTO(userWeight);
    }

    public UserWeightDTO loadUserByAuthorizationHeader(HttpServletRequest request) throws UsernameNotFoundException {
        String username = jwtTokenUtil.getUsername(getTokenFromAuthorizationHeader(request));

        // Load from cache
        Cache cache = cacheManager.getCache(WeightWiseConfig.USER_WEIGHT_CACHE_KEY);
        if (cache != null) {
            UserWeightDTO userWeightDTO = cache.get(username, UserWeightDTO.class);
            if (userWeightDTO != null) {
                return userWeightDTO;
            }
        }

        return loadUserByUsername(username);
    }

    private String getTokenFromAuthorizationHeader(HttpServletRequest request) {
        final String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }

        return null;
    }
}
