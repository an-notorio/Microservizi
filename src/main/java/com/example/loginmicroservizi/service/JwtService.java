package com.example.loginmicroservizi.service;

import com.example.loginmicroservizi.model.ResetPsw;
import com.example.loginmicroservizi.model.User;
import com.example.loginmicroservizi.repository.ResetPswRepository;
import com.example.loginmicroservizi.repository.UsersRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class JwtService {

    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private ResetPswRepository resetPswRepository;

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;
    @Value("${application.security.jwt.expiration}")
    private long  jwtExpiration;
    @Value("${application.security.jwt.refresh-token.expiration}")
    private long  refreshExpiration;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaim(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(UserDetails userDetails) {
        return generateToken(new HashMap<>(), userDetails);
    }

    public String generateRefreshToken(
            UserDetails userDetails
    ){
        Claims claims = Jwts.claims().setSubject(userDetails.getUsername());
        User user = usersRepository.findAllByEmail(userDetails.getUsername()).get(0);
        List<SimpleGrantedAuthority> authorities = user.getRole().stream().map(role -> new SimpleGrantedAuthority("" + role.getRole())).collect(Collectors.toList());
        claims.put("roles", authorities);
        return buildToken(new HashMap<>(), userDetails, refreshExpiration);
    }

    public String generateToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails
    ){
        Claims claims = Jwts.claims().setSubject(userDetails.getUsername());
        User user = usersRepository.findAllByEmail(userDetails.getUsername()).get(0);
        List<SimpleGrantedAuthority> authorities = user.getRole().stream().map(role -> new SimpleGrantedAuthority("" + role.getRole())).collect(Collectors.toList());
        claims.put("roles", authorities);
        return buildToken(claims, userDetails, jwtExpiration);
    }

    public String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ){
        Claims claims = Jwts.claims().setSubject(userDetails.getUsername());
        User user = usersRepository.findAllByEmail(userDetails.getUsername()).get(0);
        List<SimpleGrantedAuthority> authorities = user.getRole().stream().map(role -> new SimpleGrantedAuthority("" + role.getRole())).collect(Collectors.toList());
        claims.put("roles", authorities);
        return Jwts
                .builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateTokenResetPsw(UserDetails userDetails) {
        return generateTokenResetPsw(new HashMap<>(), userDetails);
    }

    public String generateTokenResetPsw(
            Map<String, Object> extraClaims,
            UserDetails userDetails
    ){
        Claims claims = Jwts.claims().setSubject(userDetails.getUsername());
        User user = usersRepository.findAllByEmail(userDetails.getUsername()).get(0);
        List<SimpleGrantedAuthority> authorities = user.getRole().stream().map(role -> new SimpleGrantedAuthority("" + role.getRole())).collect(Collectors.toList());
        claims.put("roles", authorities);
        var jwts = Jwts
                .builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 30))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();

        List<ResetPsw> resetPswList = resetPswRepository.findAllByUser(user);
        for(ResetPsw i : resetPswList){
            if(!i.isExpired()){
                i.setExpireAt(LocalDateTime.now());
                resetPswRepository.save(i);
            }
        }

        var resetPsw = ResetPsw.builder()
                .resetToken(jwts)
                .expireAt(LocalDateTime.now().plusSeconds(60 * 24))
                .user(user)
                .build();

        resetPswRepository.save(resetPsw);

        return jwts;
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) { return extractExpiration(token).before(new Date()); }

    private Date extractExpiration(String token) { return extractClaim(token, Claims::getExpiration); }

    private Claims extractAllClaim(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Key getSignInKey(){
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}