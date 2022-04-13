package com.example.pixelmanga.repositories

import com.example.pixelmanga.entities.User
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.test.annotation.Rollback

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Rollback(false)
class UserRepositoryTests {
    @Autowired
    private val entityManager: TestEntityManager? = null

    @Autowired
    private val repo: UserRepository? = null
    @Test
    fun testCreateUser() {
        val user = User()
        user.email = "ravikumar@gmail.com"
        user.password = "ravi2020"
        user.username = "Ravi"
        user.bornYear = "1999"
        val savedUser = repo!!.save(user)
        val existUser = entityManager!!.find(
            User::class.java, savedUser.id
        )
        Assertions.assertThat(user.email).isEqualTo(existUser.email)
    }
}