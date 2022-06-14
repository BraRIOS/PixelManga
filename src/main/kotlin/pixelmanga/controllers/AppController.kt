package pixelmanga.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import pixelmanga.entities.Sample
import pixelmanga.entities.User
import pixelmanga.repositories.RoleRepository
import pixelmanga.repositories.SampleRepository
import pixelmanga.repositories.UserRepository
import pixelmanga.security.CustomUserDetails
import java.util.stream.Collectors


@Controller
class AppController {

    @Autowired
    private lateinit var userRepo: UserRepository

    @Autowired
    private lateinit var sampleRepo: SampleRepository

    @Autowired
    private lateinit var roleRepo: RoleRepository


    @GetMapping("")
    fun root(): String {
        return "redirect:/home"
    }

    @GetMapping("/register")
    fun showRegistrationForm(model: Model): String {
        model.addAttribute("user", User())
        val authentication: Authentication? = SecurityContextHolder.getContext().authentication
        if (!(authentication == null || authentication is AnonymousAuthenticationToken))
            return "redirect:/home"

        return "signup_form"
    }

    @GetMapping("/home")
    fun showHomePage(): String {
        return "home"
    }

    @GetMapping("/login")
    fun showLoginForm(): String {
        val authentication: Authentication? = SecurityContextHolder.getContext().authentication
        return if (authentication == null || authentication is AnonymousAuthenticationToken) {
            "login_form"
        } else "redirect:/"
    }

    @GetMapping("/users")
    fun listUsers(model: Model): String? {
        val listUsers = userRepo.findAll()
        model.addAttribute("listUsers", listUsers)
        return "users"
    }

    @GetMapping("/samples")
    fun listSamples(model: Model): String? {
        model.addAttribute("listSamples", sampleRepo.findAll())
        return "samples"
    }

    @GetMapping("/register_sample")
    fun showSampleRegistrationForm(model: Model): String {
        model.addAttribute("sample", Sample())
        return "sample"
    }

    @PostMapping("/process_register_sample")
    fun registerSample(sample: Sample): String {
        sampleRepo.save(sample)
        return "redirect:/samples"
    }

    @PostMapping("/process_register")
    fun registerUser(user: User): String {
        val passwordEncoder = BCryptPasswordEncoder()
        val encodedPassword = passwordEncoder.encode(user.password)
        user.password = encodedPassword
        user.roles.add(roleRepo.findByName("USER"))
        userRepo.save(user)

        return "redirect:/login"
    }

    @GetMapping("/index")
    fun showIndexPage(): String {
        return "index"
    }

    @PostMapping("/make_author")
    fun makeAuthor(authentication: Authentication): String {
        val user = userRepo.findByUsername(authentication.name)
        user.roles.add(roleRepo.findByName("AUTHOR"))

        val actualAuthorities : MutableSet<GrantedAuthority>? =
            user.roles.stream().map { role ->  SimpleGrantedAuthority(role.name) }.collect(Collectors.toSet())
        val newAuth: Authentication = UsernamePasswordAuthenticationToken(CustomUserDetails(user), user.password, actualAuthorities)
        SecurityContextHolder.getContext().authentication = newAuth

        userRepo.save(user)

        return "redirect:/register_sample"
    }
}