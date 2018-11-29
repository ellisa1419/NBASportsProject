package com.se.termproject;

import java.security.Principal;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@EnableOAuth2Sso
@RestController
public class Login extends WebSecurityConfigurerAdapter{
	@Autowired
	private UserRepository userRepository;
	
	
	
	
	@RequestMapping("/user")
	public Principal user(Principal principal, HttpSession session ) {
		
		Authentication authentication	= SecurityContextHolder.getContext().getAuthentication();
		Optional<UserMaster> user  = userRepository.findById((String)authentication.getPrincipal());
		if(!user.isPresent()) {
			UserMaster newuser = new UserMaster();	
			newuser.setName((String)authentication.getPrincipal());
			Map<String, Object> details = (Map<String, Object>) ((OAuth2Authentication) principal).getUserAuthentication().getDetails();
			newuser.setFull_name((String) details.get("name"));
			newuser.setBlocked(false);
			userRepository.save(newuser);
			session.setAttribute("favTeams", new HashSet<String>());
			
		}
		else {
			UserMaster um =user.get();
//			final Calendar cal = Calendar.getInstance();
//			cal.add(Calendar.DATE, -5);
//			um.setLastLogin(cal.getTime());
			um.setLastLogin(new Date());
			userRepository.save(um);
		}
		return principal;
	}
	
	
	@Override
	  protected void configure(HttpSecurity http) throws Exception {
	    http
	      .antMatcher("/**")
	      .authorizeRequests()
	        .antMatchers("/", "/login**", "/webjars/**", "/error**")
	        .permitAll()
	      .anyRequest()
	        .authenticated()
	        .and().logout().logoutSuccessUrl("/").permitAll()
	        .and().csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()).and().httpBasic();;
	  }
	
	
}