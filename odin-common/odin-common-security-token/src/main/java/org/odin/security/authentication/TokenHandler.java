package org.odin.security.authentication;

import org.odin.security.service.UserService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;

@Component
public final class TokenHandler {

  private final String secret;
  private final UserService userService;

  @Autowired
  public TokenHandler(@Value("${app.jwt.secret}") String secret, UserService userService) {
    this.secret = secret;
    this.userService = userService;
  }

  public Optional<OdinUserDetails> parseUserFromToken(String token) {
    String username = Jwts.parser()
      .setSigningKey(secret)
      .parseClaimsJws(token)
      .getBody()
      .getSubject();
    return Optional.ofNullable(userService.getByUsernameAsUserDetails(username));
  }

  public String createTokenForUser(org.springframework.security.core.userdetails.UserDetails user) {
    final ZonedDateTime afterOneWeek = ZonedDateTime.now()
      .plusWeeks(1);
    return Jwts.builder()
      .setSubject(user.getUsername())
      .signWith(SignatureAlgorithm.HS512, secret)
      .setExpiration(Date.from(afterOneWeek.toInstant()))
      .compact();
  }

}

