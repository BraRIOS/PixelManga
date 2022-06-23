package pixelmanga.security

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import pixelmanga.entities.User
import pixelmanga.repositories.UserRepository


class CustomUserDetailsService : UserDetailsService {
    @Autowired
    private lateinit var userRepo: UserRepository

    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(username: String): UserDetails {
        val user: User = userRepo.findByUsername(username) ?: throw UsernameNotFoundException("User not found")
        return CustomUserDetails(user)
    }
}