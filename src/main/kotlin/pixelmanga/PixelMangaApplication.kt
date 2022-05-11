package pixelmanga

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PixelMangaApplication

fun main(args: Array<String>) {
    runApplication<PixelMangaApplication>(*args)
}
