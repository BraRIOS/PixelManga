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
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import pixelmanga.entities.Chapter
import pixelmanga.entities.Sample
import pixelmanga.entities.User
import pixelmanga.repositories.*
import pixelmanga.security.CustomUserDetails
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.stream.Collectors
import kotlin.io.path.exists


@Controller
class AppController {

    @Autowired
    private lateinit var userRepo: UserRepository

    @Autowired
    private lateinit var sampleRepo: SampleRepository

    @Autowired
    private lateinit var roleRepo: RoleRepository

    @Autowired
    private lateinit var attributeRepo: AttributeRepository

    @Autowired
    private lateinit var chapterRepo: ChapterRepository

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
    fun showHomePage(model: Model): String {
        model.addAttribute("samples", sampleRepo.findAll())
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
        model.addAttribute("sample_types", attributeRepo.findByType_Name("tipo de libro").sortedBy { it.name })
        model.addAttribute("sample_genres", attributeRepo.findByType_Name("género").sortedBy { it.name })
        model.addAttribute("sample_demographics", attributeRepo.findByType_Name("demografía").sortedBy { it.name })

        return "sample"
    }

    @PostMapping("/perform_sample_register")
    fun saveSample(sample: Sample, @RequestParam("type") type: String,
                   @RequestParam("demographic") demographic:String, @RequestParam("fileImage") image: MultipartFile,
                   @RequestParam("genres[]") genres: Array<String>, ra: RedirectAttributes): String {
        sample.attributes.addAll(genres.map { attributeRepo.findByName(it) })
        sample.attributes.add(attributeRepo.findByName(type))
        sample.attributes.add(attributeRepo.findByName(demographic))

        val type = sample.attributes.first { attribute -> attribute.type?.name == "tipo de libro" }.name
        val regex = """\*|\"|\?|\\|\>|/|<|:|\|""".toRegex()
        val name = "${regex.replace(sample.name as String,"_")}-cover.${image.contentType?.split("/")?.last()}"


        val savedSample = sampleRepo.save(sample)
        val id = savedSample.id as Long

        val uploadDir = "./src/main/resources/static/images/samples/$type/$id"
        sampleRepo.updateCoverPathById("/static/images/samples/$type/$id/$name", id)

        val uploadPath = Paths.get(uploadDir)
        if (!uploadPath.exists()) {
            uploadPath.toFile().mkdirs()
        }
        val imagePath = uploadPath.resolve(name)
        Files.copy(image.inputStream, imagePath, StandardCopyOption.REPLACE_EXISTING)

        ra.addAttribute("message", "${sample.name} registrado correctamente")
        return "redirect:/home"
    }
    @GetMapping("/library/{type}/{id}/{name}")
    fun showSample(model: Model, @PathVariable type: String, @PathVariable id: Long, @PathVariable name: String): String {
        val sample = sampleRepo.findById(id).get()
        val chapters = chapterRepo.findBySample_Id(id)
        model.addAttribute("sample", sample)
        model.addAttribute("type",sample.attributes.first { attribute -> attribute.type?.name == "tipo de libro" }.name)
        model.addAttribute("genres",sample.attributes.filter { attribute -> attribute.type?.name == "género" }.map { attribute -> attribute.name })
        model.addAttribute("chapters", chapters)
        return "sample_view"
    }

    @GetMapping("/upload_chapter/{id}")
    fun showUploadChapterForm(model: Model, @PathVariable id: Long): String {
        val sample = sampleRepo.findById(id).get()
        model.addAttribute("chapter",Chapter())
        model.addAttribute("sample", sample)
        return "chapter_form"
    }

    @GetMapping("/view/{sampleName}/{number}")
    fun showChapter(model: Model, @PathVariable sampleName: String, @PathVariable number:Long): String {
        val chapter = chapterRepo.findByNumber(number)
        val chapters = chapterRepo.findBySample_Name(sampleName)
        model.addAttribute("chapter", chapter)
        model.addAttribute("chapters", chapters)
        return "chapter_view"
    }

    @PostMapping("/perform_chapter_upload")
    fun saveChapter(chapter: Chapter, @RequestParam("fileImage") image: MultipartFile,
                    @RequestParam("sampleName") sampleName: String, ra: RedirectAttributes): String {
        val sample = sampleRepo.findByName(sampleName)

        chapter.number = chapterRepo.countBySample_Id(sample.id as Long) + 1
        chapter.sample = sample

        val type = sample.attributes.first { attribute -> attribute.type?.name == "tipo de libro" }.name
        val regex = """\*|\"|\?|\\|\>|/|<|:|\|""".toRegex()
        val name = "${regex.replace(sample.name as String,"_")}.${image.contentType?.split("/")?.last()}"

        val savedChapter = chapterRepo.save(chapter)
        val id = savedChapter.id as Long

        val uploadDir = "./src/main/resources/static/images/samples/$type/${sample.id}/chapter/$id"
        chapterRepo.updateImagePathById("/static/images/samples/$type/${sample.id}/chapter/$id/$name", id)

        val uploadPath = Paths.get(uploadDir)
        if (!uploadPath.exists()) {
            uploadPath.toFile().mkdirs()
        }
        val imagePath = uploadPath.resolve(name)
        image.transferTo(imagePath)

        ra.addAttribute("message", "El capítulo $id se ha registrado correctamente")
        return "redirect:/library/$type/${sample.id}/${sample.name}"

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

    @GetMapping("/profile")
    fun showProfile(model: Model): String {
        val authentication: Authentication? = SecurityContextHolder.getContext().authentication
        if (authentication == null || authentication is AnonymousAuthenticationToken) {
            return "redirect:/login"
        }
        val user = userRepo.findByUsername(authentication.name)
        model.addAttribute("user", user)
        return "profile"
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