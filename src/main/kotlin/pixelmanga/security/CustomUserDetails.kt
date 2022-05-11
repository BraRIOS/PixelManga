package pixelmanga.security

import pixelmanga.entities.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails


class CustomUserDetails(user: User) :UserDetails {

    private val user: User

    init {
        this.user = user
    }

    override fun getAuthorities(): MutableCollection< GrantedAuthority> {
        /* roles: Set<Role> = user.getRoles()
        val authorities: MutableList<SimpleGrantedAuthority> = ArrayList()

        for (role in roles) {
            authorities.add(SimpleGrantedAuthority(role.getName()))
        }*/
        val authorities = ArrayList<GrantedAuthority>()
        authorities.add(SimpleGrantedAuthority("USER"))
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