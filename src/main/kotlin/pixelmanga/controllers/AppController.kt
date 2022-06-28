package pixelmanga.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
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
import pixelmanga.entities.Rate
import pixelmanga.entities.Sample
import pixelmanga.entities.User
import pixelmanga.repositories.*
import pixelmanga.security.CustomUserDetails
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.stream.Collectors
import java.util.stream.IntStream
import kotlin.io.path.exists
import kotlin.streams.toList


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

    @Autowired
    private lateinit var rateRepo: RateRepository

    @GetMapping("")
    fun root(): String {
        return "redirect:/home"
    }

    @GetMapping("/home")
    fun showHomePage(model: Model): String {
        val randomSamples = sampleRepo.findAll().sortedBy { it.name }.shuffled().take(10)
        val latestSamples = sampleRepo.findAll().sortedBy { it.publicationDate }.reversed().take(10)
        model.addAttribute("randomSamples", randomSamples)
        model.addAttribute("latest_samples", latestSamples)
        return "home"
    }

    @GetMapping("/register")
    fun showRegistrationForm(model: Model): String {
        model.addAttribute("user", User())
        val authentication: Authentication? = SecurityContextHolder.getContext().authentication
        if (!(authentication == null || authentication is AnonymousAuthenticationToken))
            return "redirect:/home"

        return "signup_form"
    }

    @GetMapping("/check_username")
    fun checkUsername(@RequestParam username: String): Boolean {
        if (userRepo.findByUsername(username) != null)
            return false
        return true
    }

    @GetMapping("/check_email")
    fun checkEmail(@RequestParam email: String): Boolean {
        if (userRepo.findByEmail(email) != null)
            return false
        return true
    }

    @PostMapping("/process_register")
    fun registerUser(user: User, redirectAttributes: RedirectAttributes): String {
        val passwordEncoder = BCryptPasswordEncoder()
        val encodedPassword = passwordEncoder.encode(user.password)
        user.password = encodedPassword
        user.roles.add(roleRepo.findByName("USER"))
        userRepo.save(user)

        return "redirect:/login"
    }

    @GetMapping("/login")
    fun showLoginForm(): String {
        val authentication: Authentication? = SecurityContextHolder.getContext().authentication
        return if (authentication == null || authentication is AnonymousAuthenticationToken) {
            "login_form"
        } else "redirect:/"
    }

    @GetMapping("/profile")
    fun showProfile(model: Model, ra: RedirectAttributes): String {
        val authentication: Authentication? = SecurityContextHolder.getContext().authentication
        if (authentication == null || authentication is AnonymousAuthenticationToken) {
            ra.addFlashAttribute("error", "Debes iniciar sesión para poder ver tu perfil")
            return "redirect:/login"
        }
        val user = userRepo.findByUsername(authentication.name)
        model.addAttribute("user", user)
        return "profile"
    }

    @GetMapping("/profile/edit")
    fun showProfileEdit(model: Model, ra: RedirectAttributes): String {
        val authentication: Authentication? = SecurityContextHolder.getContext().authentication
        if (authentication == null || authentication is AnonymousAuthenticationToken) {
            ra.addAttribute("error", "Debes iniciar sesión para poder editar tu perfil")
            return "redirect:/login"
        }
        val user = userRepo.findByUsername(authentication.name)
        model.addAttribute("user", user)
        return "profile_edit"
    }

    @PostMapping("/make_author")
    fun makeAuthor(authentication: Authentication): String {
        val user = userRepo.findByUsername(authentication.name) as User
        user.roles.add(roleRepo.findByName("AUTHOR"))

        val actualAuthorities : MutableSet<GrantedAuthority>? =
            user.roles.stream().map { role ->  SimpleGrantedAuthority(role.name) }.collect(Collectors.toSet())
        val newAuth: Authentication = UsernamePasswordAuthenticationToken(CustomUserDetails(user), user.password, actualAuthorities)
        SecurityContextHolder.getContext().authentication = newAuth

        userRepo.save(user)

        return "redirect:/register_sample"
    }
    @GetMapping("/register_sample")
    fun showSampleRegistrationForm(model: Model): String {
        model.addAttribute("sample", Sample())
        model.addAttribute("is_register", true)
        model.addAttribute("all_types", attributeRepo.findByType_Name("tipo de libro").sortedBy { it.name })
        model.addAttribute("all_genres", attributeRepo.findByType_Name("género").sortedBy { it.name })
        model.addAttribute("all_demographics", attributeRepo.findByType_Name("demografía").sortedBy { it.name })

        return "sample_form"
    }

    @PostMapping("/perform_sample_register")
    fun saveSample(sample: Sample, @RequestParam("type") type: String,
                   @RequestParam("demographic") demographic:String, @RequestParam("fileImage") image: MultipartFile,
                   @RequestParam("genres[]") genres: Array<String>, ra: RedirectAttributes): String {

        sample.attributes.addAll(genres.map { attributeRepo.findByName(it) })
        sample.attributes.add(attributeRepo.findByName(type))
        sample.attributes.add(attributeRepo.findByName(demographic))

        if (sampleRepo.findByName(sample.name as String) != null && sampleRepo.findByName(sample.name as String)!!.attributes.contains(attributeRepo.findByName(type))) {
            ra.addFlashAttribute("message", "${sample.name} ya existe")
            return "redirect:/register_sample"
        }
        sampleRepo.save(sample)

        val id = sample.id as Long

        saveSampleCover(type, id, image, sample)

        ra.addAttribute("message", "${sample.name} registrado correctamente")
        return "redirect:/home"
    }

    @GetMapping("/library")
    fun showLibrary(model: Model, @RequestParam("page", required = false) pageNumber: Int?, @RequestParam("type", required = false) type: String): String{
        val page = pageNumber ?: 0

        val samplePage = sampleRepo.findAll(PageRequest.of(page, 20))
        val totalPage = samplePage.totalPages
        if (totalPage > 0){
            val pages = IntStream.rangeClosed(1, totalPage).toList()
            model.addAttribute("pages", pages)
        }
        model.addAttribute("list_samples", samplePage.content)

        return "library"
    }

    @GetMapping("/library/{type}/{id}/{name}")
    fun showSample(model: Model, @PathVariable type: String, @PathVariable id: Long, @PathVariable name: String): String {
        val sample = sampleRepo.findById(id).get()
        val chapters = chapterRepo.findAllBySampleId(id)
        val average = getSampleAverageRate(sample.id as Long)
        model.addAttribute("average", average)
        model.addAttribute("sample", sample)
        model.addAttribute("type", type)
        model.addAttribute("demography", sample.attributes.find { it.type?.name == "demografía" })
        model.addAttribute("genres",sample.attributes.filter { attribute -> attribute.type?.name == "género" }.map { attribute -> attribute.name })
        model.addAttribute("chapters", chapters)
        return "sample_view"
    }

    @GetMapping("/library/{type}/{id}/{name}/edit")
    fun showSampleEditForm(model: Model, @PathVariable type: String, @PathVariable id: Long, @PathVariable name: String): String {
        val sample = sampleRepo.findById(id).get()
        model.addAttribute("sample", sample)
        model.addAttribute("is_register", false)
        model.addAttribute("sample_type", type)
        model.addAttribute("sample_demography", sample.attributes.find { it.type?.name == "demografía" }?.name)
        model.addAttribute("sample_genres",sample.attributes.filter { attribute -> attribute.type?.name == "género" }.map { attribute -> attribute.name })
        model.addAttribute("all_genres",attributeRepo.findByType_Name("género").sortedBy { it.name })
        model.addAttribute("all_demographics",attributeRepo.findByType_Name("demografía").sortedBy { it.name })
        return "sample_form"
    }

    @PostMapping("/perform_sample_edit")
    fun saveSampleEdit(sample: Sample, @RequestParam("id") id: Long,
                       @RequestParam("type") type: String,
                       @RequestParam("demographic") demographic:String, @RequestParam("fileImage") image: MultipartFile,
                       @RequestParam("genres[]") genres: Array<String>, ra: RedirectAttributes): String {
        sample.id = id
        sample.attributes.addAll(genres.map { attributeRepo.findByName(it) })
        sample.attributes.add(attributeRepo.findByName(type))
        sample.attributes.add(attributeRepo.findByName(demographic))

        sampleRepo.save(sample)

        saveSampleCover(type, id, image, sample)

        ra.addAttribute("message", "Se han guardado los cambios de ${sample.name}")
        return "redirect:/library/$type/$id/${sample.name}"
    }

    @PostMapping("/user_rating_sample")
    fun rateSample(@RequestParam("sample_id") sampleId: Long, @RequestParam("rating") rating: Int,
                   ra: RedirectAttributes): String {
        val sample = sampleRepo.findById(sampleId).get()
        val authentication: Authentication? = SecurityContextHolder.getContext().authentication
        if (authentication == null || authentication is AnonymousAuthenticationToken) {
            ra.addFlashAttribute("error", "Debes iniciar sesión para poder valorar: ${sample.name}")
            return "redirect:/login"
        }
        val user = userRepo.findByUsername(SecurityContextHolder.getContext().authentication.name) as User
        val ratingSample = rateRepo.findByUser_IdAndSample_Id(user.id as Long, sampleId)
        if (ratingSample != null) {
            ratingSample.rating = rating
            rateRepo.save(ratingSample)
        } else {
            val newRate = Rate()
            newRate.user = user
            newRate.sample = sample
            newRate.rating = rating
            rateRepo.save(newRate)
        }
        val type = sample.attributes.find { it.type?.name == "tipo de libro" }?.name
        ra.addFlashAttribute("message", "Valoración añadida")
        return "redirect:/library/$type/${sample.id}/${sample.name}"
    }

    @GetMapping("/sample_average_rate")
    fun getSampleAverageRate(sampleId: Long): ResponseEntity<String> {
        return try {
            val sample = sampleRepo.findById(sampleId).get()
            val rates = rateRepo.findAllBySample_Id(sample.id as Long)
            if (rates.isEmpty()) {
                ResponseEntity.ok("0.00")
            } else {
                val sum = rates.sumOf { it.rating }
                val average = sum / rates.size.toDouble()
                ResponseEntity.ok(String.format("%.2f", average))
            }
        } catch (e: Exception) {
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error")
        }
    }

    private fun saveSampleCover(
        type: String,
        id: Long,
        image: MultipartFile,
        sample: Sample
    ) {
        val uploadDir = "./resources/images/samples/$type/$id"

        val uploadPath = Paths.get(uploadDir)
        if (!uploadPath.exists()) {
            uploadPath.toFile().mkdirs()
        }

        if (!image.isEmpty) {
            val regex = """\s|\*|"|\?|\\|>|/|<|:|\|""".toRegex()
            val name = "${regex.replace(sample.name as String, "_")}-cover.${image.contentType?.split("/")?.last()}"
            sampleRepo.updateCoverById(name, id)

            val imagePath = uploadPath.resolve(name)
            Files.copy(image.inputStream, imagePath, StandardCopyOption.REPLACE_EXISTING)
        }
    }

    @GetMapping("/library/{type}/{id}/{name}/delete")
    fun deleteSample(@PathVariable type: String, @PathVariable id: Long, @PathVariable name: String, ra: RedirectAttributes): String {
        val sample = sampleRepo.findById(id).get()
        chapterRepo.deleteAll(chapterRepo.findAllBySampleId(id))
        sampleRepo.delete(sample)
        val uploadDir = sample.samplePath() as String
        val uploadPath = Paths.get(uploadDir)
        if (uploadPath.exists()) {
            uploadPath.toFile().deleteRecursively()
        }

        ra.addAttribute("message", "Se ha eliminado ${sample.name}")
        return "redirect:/home"
    }

    @GetMapping("/upload_chapter/{sampleId}")
    fun showUploadChapterForm(model: Model, @PathVariable sampleId: Long): String {
        val sample = sampleRepo.findById(sampleId).get()
        model.addAttribute("sample", sample)
        return "chapter_form"
    }

    @PostMapping("/perform_chapter_upload")
    fun saveChapter(@RequestParam("fileImage") image: MultipartFile,
                    @RequestParam("sample_id") sampleId: Long, ra: RedirectAttributes): String {
        if (image.isEmpty) {
            ra.addAttribute("message", "No se ha seleccionado una imagen")
            return "redirect:/upload_chapter/${sampleId}"
        }
        val chapter = Chapter()
        val sample = sampleRepo.findById(sampleId).get()

        chapter.number = chapterRepo.countBySampleId(sample.id as Long) + 1
        chapter.sample = sample

        val type = sample.attributes.first { attribute -> attribute.type?.name == "tipo de libro" }.name
        val regex = """\s|\*|"|\?|\\|>|/|<|:|\|""".toRegex()
        val name = "${regex.replace(sample.name as String,"_")}_capítulo_${chapter.number}.${image.contentType?.split("/")?.last()}"

        chapter.image= name

        val uploadDir = "./resources/images/samples/$type/${sample.id}/chapters/${chapter.number}"
        chapterRepo.save(chapter)

        val uploadPath = Paths.get(uploadDir)
        if (!uploadPath.exists()) {
            uploadPath.toFile().mkdirs()
        }
        val imagePath = uploadPath.resolve(name)
        image.transferTo(imagePath)

        ra.addAttribute("message", "El capítulo ${chapter.number} se ha registrado correctamente")
        return "redirect:/library/$type/${sample.id}/${sample.name}"

    }

    @GetMapping("/view/{type}/{sampleId}/{sampleName}/chapters/{number}")
    fun showChapter(model: Model, @PathVariable sampleId: Long, @PathVariable number:Long): String {
        val chapter = chapterRepo.findBySampleIdAndNumber(sampleId, number)
        val chapters = chapterRepo.findAllBySampleId(sampleId)
        model.addAttribute("chapter", chapter)
        model.addAttribute("chapters", chapters)
        return "chapter_view"
    }

    @GetMapping("/index")
    fun showIndexPage(): String {
        return "index"
    }

    @GetMapping("/images/samples/{id}")
    fun getSampleImage(@PathVariable id: Long): ResponseEntity<ByteArray> {
        val sample = sampleRepo.findById(id).get()
        return if (sample.coverPath() != null) {
            val imagePath = Paths.get(sample.coverPath() as String)
            val image = Files.readAllBytes(imagePath)
            ResponseEntity.ok().body(image)
        } else{
            val image = Files.readAllBytes(Paths.get("./resources/images/samples/default.png"))
            ResponseEntity.ok().body(image)
        }
    }

    @GetMapping("/images/samples/{id}/chapters/{number}")
    fun getChapterImage(@PathVariable id: Long, @PathVariable number: Long): ResponseEntity<ByteArray> {
        val chapter = chapterRepo.findBySampleIdAndNumber(id, number)
        return if (chapter.imagePath() != null) {
            val imagePath = Paths.get(chapter.imagePath() as String)
            val image = Files.readAllBytes(imagePath)
            ResponseEntity.ok().body(image)
        } else{
            val image = Files.readAllBytes(Paths.get("./resources/images/samples/default.png"))
            ResponseEntity.ok().body(image)
        }
    }
}