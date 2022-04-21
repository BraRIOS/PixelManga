package com.example.pixelmanga.controllers

import com.example.pixelmanga.entities.Sample
import com.example.pixelmanga.entities.User
import com.example.pixelmanga.repositories.SampleRepository
import com.example.pixelmanga.repositories.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping


@Controller
class AppController {

    @Autowired
    private lateinit var userRepo: UserRepository

    @Autowired
    private lateinit var sampleRepo: SampleRepository

    @GetMapping("")
    fun viewHomePage(): String {
        return "index"
    }

    @GetMapping("/register")
    fun showRegistrationForm(model: Model): String {
        model.addAttribute("user", User())
        return "signup_form"
    }

    @GetMapping("/login")
    fun showLoginForm(model: Model): String {
        model.addAttribute("user", User())
        return "login"
    }

    @GetMapping("/home")
    fun showHomePage(model: Model): String {
        return "home"
    }

    @GetMapping("/users")
    fun listUsers(model: Model): String? {
        val listUsers = userRepo.findAll()
        model.addAttribute("listUsers", listUsers)
        return "users"
    }

    @GetMapping("/samples")
    fun listSamples(model: Model): String? {
        val listSamples = sampleRepo.findAll()
        model.addAttribute("listSamples", listSamples)

        return "samples"
    }

    @GetMapping("/register_sample")
    fun showSampleRegistrationForm(model: Model): String {
        model.addAttribute("sample", Sample())
        return "sample_form"
    }

    @PostMapping("/register_sample")
    fun registerSample(sample: Sample): String {
        sampleRepo.save(sample)
        return "samples"
    }

    @PostMapping("/process_register")
    fun registerUser(user: User): String {
        val passwordEncoder = BCryptPasswordEncoder()
        val encodedPassword = passwordEncoder.encode(user.password)
        user.password = encodedPassword

        userRepo.save(user)
        return "register_success"
    }
}