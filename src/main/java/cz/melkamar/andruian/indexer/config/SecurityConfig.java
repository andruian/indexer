package cz.melkamar.andruian.indexer.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.authentication.www.BasicAuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {


    @Qualifier("indexerConfiguration")
    @Autowired
    private IndexerConfiguration indexerConfiguration;
    
    

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/admin", "/admin/**").hasRole("ADMIN")
                .and()
                .formLogin()
                .loginPage("/login")
                .permitAll()
                .and()
                .logout()
                .permitAll();
        
        // api endpoints
        http
                .authorizeRequests()
                .antMatchers("/api/admin/**").hasRole("ADMIN")
                .and().httpBasic().realmName("indexer").authenticationEntryPoint(entryPoint());
    }

    @Bean
    @Override
    public UserDetailsService userDetailsService() {
        UserDetails user =
                User.withDefaultPasswordEncoder()
                        .username(indexerConfiguration.getAdminUsername())
                        .password(indexerConfiguration.getAdminPassword())
                        .roles("ADMIN")
                        .build();

        return new InMemoryUserDetailsManager(user);
    }
    
    @Bean
    public BasicAuthenticationEntryPoint entryPoint(){
        return new CustomBasicAuthenticationEntryPoint();
    }


    class CustomBasicAuthenticationEntryPoint extends BasicAuthenticationEntryPoint {

        @Override
        public void commence(final HttpServletRequest request,
                             final HttpServletResponse response,
                             final AuthenticationException authException) throws IOException, ServletException {
            //Authentication failed, send error response.
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.addHeader("WWW-Authenticate", "Basic realm=" + getRealmName() + "");

            PrintWriter writer = response.getWriter();
            writer.println("HTTP Status 401 : " + authException.getMessage());
        }

        @Override
        public void afterPropertiesSet() throws Exception {
            setRealmName("indexer");
            super.afterPropertiesSet();
        }
    }
}
