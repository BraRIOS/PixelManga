package com.example.pixelmanga.controllers

import com.example.pixelmanga.entities.User
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

    @GetMapping("/users")
    fun listUsers(model: Model): String? {
        val listUsers = userRepo.findAll()
        model.addAttribute("listUsers", listUsers)
        return "users"
    }

    @GetMapping("/logout")
    fun logout(): String {
        return "redirect:/"
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