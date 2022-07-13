package pixelmanga.entities

import org.springframework.web.multipart.MultipartFile

interface Pathable {
    fun setPersonalizedImageName(image:MultipartFile)
    fun path(): String?
    fun imagePath(): String
    fun existsImage(): Boolean
}