package com.cos.crud.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.cos.crud.handler.MyLogoutSuccessHandler;

@Configuration
@EnableWebSecurity //스프링 시큐리티 필터에 등록하는 어노테이션
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	// 1. Bean 어노테이션은 메서드에 붙여서 객체 생성시 사용
	// 스프링 컨테이너로 들어감 autowired 해서 사용하면된다.
	@Bean
	public BCryptPasswordEncoder encodePWD() {
		return new BCryptPasswordEncoder();
	}
	
	//2. 필터링
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		// 테스트를위해 csrf를 disable해둔다
		// 테스트가 끝나면 enable해줘서 활성화시키면된다.
		// 전에 관리자님 보세요 해서 링크공격했던거 생각하면된다.
		http.csrf().disable();
		
		
		http.authorizeRequests()
		//해당주소를 타는 것은 전부 시큐리티가 막는다.
		// ex) board/접근을 세분화해서 막고싶으면 먼저 추가해주고 밑에 다막으면된다 순서 잘지켜라!!
		.antMatchers("/board/list").permitAll()
		.antMatchers("/admin/**", "/board/**").authenticated()
		//위의 주소를 타는 것을 제외한 접근은 시큐리티가 막지 않는다.
		.anyRequest().permitAll()
		.and()
		.formLogin()
		//내 모델의 아이디와 패스워드 변수명이 username과 password가 아니라면 꼭 해줘라
		//우리는 지금 필요없지만 나중에 다르게 쓰게된다면 보게될 설정 코드다
		.usernameParameter("username")
		.passwordParameter("password")
		// 시큐리티가 기본 제공하는 로그인페이지가 아니라 내가 커스텀한 페이지 사용한다.
		.loginPage("/user/loginForm")
		//이 주소를 타고 로그인이 된다 (로그인폼에서 아래의 주소로 액션을 타게 해야한다.)
		.loginProcessingUrl("/user/loginProcess")
		//로그인이 성공 할 때 이동
		.defaultSuccessUrl("/home")
		.and()
		.logout()
		//로그아웃에 성공했을 때
		//이 것은 핸들러를 사용한 것이고
		//로그인처럼 url을 사용해도된다.
		//.logoutSuccessUrl("/home")
		.logoutSuccessHandler(new MyLogoutSuccessHandler());
		

	}
	
	//로그인 실행될 때 여기서 폼으로 넘겨받은 정보를 받고
	@Autowired
	private UserDetailsService userDetailsService;
	
	//내가 인코딩 하는게 아니라, 어떤 인코딩으로 패스워드가 만들어졌는지 알려주는 거야
	//로그인 실행될 때 주입받은 유저정보의 패스워드를 인코드 해준다.
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsService).passwordEncoder(encodePWD());
	}

}