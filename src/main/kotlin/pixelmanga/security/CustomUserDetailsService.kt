package pixelmanga.security

import pixelmanga.entities.User
import pixelmanga.repositories.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException


class CustomUserDetailsService : UserDetailsService {
    @Autowired
    private val userRepo: UserRepository? = null

    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(username: String): UserDetails {
        val user: User = userRepo?.findByUsername(username) ?: throw UsernameNotFoundException("User not found")
        return CustomUserDetails(user)
    }
}