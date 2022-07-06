package pixelmanga.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder


@Configuration
@EnableWebSecurity
class WebSecurityConfig : WebSecurityConfigurerAdapter() {
    @Bean
    override fun userDetailsService(): UserDetailsService {
        return CustomUserDetailsService()
    }

    @Bean
    fun passwordEncoder(): BCryptPasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun authenticationProvider(): DaoAuthenticationProvider {
        val authProvider = DaoAuthenticationProvider()
        authProvider.setUserDetailsService(userDetailsService())
        authProvider.setPasswordEncoder(passwordEncoder())
        return authProvider
    }

    @Throws(Exception::class)
    override fun configure(auth: AuthenticationManagerBuilder) {
        auth.authenticationProvider(authenticationProvider())
    }

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http.authorizeRequests()
            .antMatchers("/make_author").authenticated()
            .antMatchers("/register_sample").hasAnyAuthority("ADMIN","AUTHOR")
            .antMatchers("/perform_sample_register").hasAnyAuthority("ADMIN", "AUTHOR")
            .antMatchers("/upload_chapter/**").hasAnyAuthority("ADMIN", "AUTHOR")
            .antMatchers("/**/edit").hasAnyAuthority("ADMIN", "AUTHOR")
            .antMatchers("/**/delete").hasAnyAuthority("ADMIN", "AUTHOR")
            .antMatchers("/perform_sample_edit").hasAnyAuthority("ADMIN", "AUTHOR")
            .antMatchers("/create_user_sample_list").authenticated()
            .anyRequest().permitAll()
            .and()
            .formLogin().loginPage("/login")
            .loginProcessingUrl("/login")
            .permitAll()
            .and()
            .logout().logoutSuccessUrl("/").permitAll()
    }
}