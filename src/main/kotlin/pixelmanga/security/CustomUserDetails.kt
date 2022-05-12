package pixelmanga.security

import org.springframework.beans.factory.annotation.Autowired
import pixelmanga.entities.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import pixelmanga.entities.Role


class CustomUserDetails(user: User) :UserDetails {

    @Autowired
    private val user: User

    init {
        this.user = user
    }

    override fun getAuthorities(): MutableCollection<GrantedAuthority> {
        val roles: Set<Role> = user.roles
        val authorities: MutableCollection<GrantedAuthority> = ArrayList()

        for (role in roles) {
            authorities.add(SimpleGrantedAuthority(role.name))
        }
        return authorities
    }

    override fun getPassword(): String? {
        return user.password
    }

    override fun getUsername(): String? {
        return user.username
    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        return true
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    override fun isEnabled(): Boolean {
        return true
    }
}