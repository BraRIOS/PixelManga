package pixelmanga.controllers

import com.stripe.Stripe
import com.stripe.model.checkout.Session
import com.stripe.param.checkout.SessionCreateParams
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.mvc.support.RedirectAttributes
import pixelmanga.entities.*
import pixelmanga.repositories.*
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.sql.Date
import java.time.LocalDate
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

    @Autowired
    private lateinit var listRepo: UserSamplesListRepository

    @Autowired
    private lateinit var authorRequestRepo: AuthorRequestRepository

    @GetMapping("")
    fun root(): String {
        return "redirect:/home"
    }

    @GetMapping("/home")
    fun showHomePage(model: Model): String {
        val randomSamples = sampleRepo.findAll().sortedBy { it.name }.shuffled().take(12)
        val latestSamples = sampleRepo.findAll().sortedBy { it.publicationDate }.reversed().take(12)
        model.addAttribute("random_samples", randomSamples)
        model.addAttribute("latest_samples", latestSamples)
        model.addAttribute("is_home", true)
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
    fun checkUsernameAvailable(@RequestParam username: String): Boolean {
        if (userRepo.findByUsername(username) != null)
            return false
        return true
    }

    @GetMapping("/check_email")
    fun checkEmailAvailable(@RequestParam email: String): Boolean {
        if (userRepo.findByEmail(email) != null)
            return false
        return true
    }

    @PostMapping("/process_register")
    fun registerUser(user: User, redirectAttributes: RedirectAttributes): String {
        if (!checkUsernameAvailable(user.username as String) && !checkEmailAvailable(user.email as String)){
            redirectAttributes.addFlashAttribute("error", "El nombre de usuario: '${user.username}' y el email: '${user.email}' ya estan en uso")
            return "redirect:/register"
        }
        if (!checkEmailAvailable(user.email as String)){
            redirectAttributes.addFlashAttribute("error", "El email: '${user.email}' ya esta en uso")
            return "redirect:/register"
        }
        if (!checkUsernameAvailable(user.username as String)){
            redirectAttributes.addFlashAttribute("error", "El nombre de usuario: '${user.username}' ya esta en uso")
            return "redirect:/register"
        }
        val passwordEncoder = BCryptPasswordEncoder()
        val encodedPassword = passwordEncoder.encode(user.password)
        user.password = encodedPassword
        user.roles.add(roleRepo.findByName("USER"))
        userRepo.save(user)

        redirectAttributes.addFlashAttribute("message", "Te has registrado correctamente")
        return "redirect:/login"
    }

    @GetMapping("/login")
    fun showLoginForm(ra: RedirectAttributes): String {
        val authentication: Authentication? = SecurityContextHolder.getContext().authentication
        return if (authentication == null || authentication is AnonymousAuthenticationToken) {
            "login_form"
        } else {
            ra.addFlashAttribute("info", "Ya estas logueado")
            "redirect:/"
        }
    }

    @GetMapping("/profile")
    fun showProfile(model: Model, ra: RedirectAttributes): String {
        val authentication: Authentication? = SecurityContextHolder.getContext().authentication
        if (authentication == null || authentication is AnonymousAuthenticationToken) {
            ra.addFlashAttribute("info", "Debes iniciar sesión para poder ver tu perfil")
            return "redirect:/login"
        }
        val user = userRepo.findByUsername(authentication.name) as User
        val roles = user.roles.map{
            role -> when(role.name){
                "ADMIN" -> "Administrador"
                "USER" -> "Lector"
                "AUTHOR" -> "Autor"
                else -> "Desconocido"
            }
        }
        if (!user.roles.contains(roleRepo.findByName("ADMIN")) && !user.roles.contains(roleRepo.findByName("AUTHOR"))) {
            val authorRequests = authorRequestRepo.findByUsernameOrderByIdDesc(user.username as String)
            if (authorRequests.isNotEmpty()) {
                val lastAuthorRequest = authorRequests.first()
                model.addAttribute("isUnderReview", lastAuthorRequest.status == "PENDIENTE")
                model.addAttribute("status", lastAuthorRequest.status)
                if (lastAuthorRequest.status == "RECHAZADO") {
                    model.addAttribute("rejectReason", lastAuthorRequest.rejectReason)
                }
            } else model.addAttribute("isUnderReview", false)
        }
        var userLists = listRepo.findAllByUser_Username(user.username as String)
        var followedLists = listRepo.findAllByFollowersContaining(user.username as String)
        if (userLists.size > 6){
            userLists = userLists.subList(0, 6)
        }
        if (followedLists.size > 6){
            followedLists = followedLists.subList(0, 6)
        }
        model.addAttribute("user", user)
        model.addAttribute("user_lists", userLists)
        model.addAttribute("roles", roles)
        model.addAttribute("followed_lists",followedLists)

        return "profile"
    }

    @GetMapping("/profile/edit")
    fun showProfileEdit(model: Model, ra: RedirectAttributes): String {
        val authentication: Authentication? = SecurityContextHolder.getContext().authentication
        if (authentication == null || authentication is AnonymousAuthenticationToken) {
            ra.addFlashAttribute("error", "Debes iniciar sesión para poder editar tu perfil")
            return "redirect:/login"
        }
        val user = userRepo.findByUsername(authentication.name)
        model.addAttribute("user", user)
        return "profile_edit"
    }

    @PostMapping("/profile/edit")
    fun editProfile(@RequestParam username: String, @RequestParam email:String, @RequestParam password:String,
                    @RequestParam birthDate: String, @RequestParam("fileImage") image: MultipartFile,
                    redirectAttributes: RedirectAttributes): String {
        val authentication: Authentication? = SecurityContextHolder.getContext().authentication
        if (authentication == null || authentication is AnonymousAuthenticationToken) {
            redirectAttributes.addFlashAttribute("error", "Debes iniciar sesión para poder editar tu perfil")
            return "redirect:/login"
        }
        val userDB = userRepo.findByUsername(authentication.name) as User
        if (!checkEmailAvailable(email) && email != userDB.email) {
            redirectAttributes.addFlashAttribute("error", "El email: '${email}' ya esta en uso")
            return "redirect:/profile"
        }
        if (!checkUsernameAvailable(username) && username != userDB.username) {
            redirectAttributes.addFlashAttribute("error", "El nombre de usuario: '${username}' ya esta en uso")
            return "redirect:/profile"
        }
        userDB.username = username
        userDB.email = email
        val passwordEncoder = BCryptPasswordEncoder()
        val encodedPassword = passwordEncoder.encode(password)
        userDB.password = encodedPassword
        if(birthDate=="") userDB.birthDate = null else userDB.birthDate = Date.valueOf(birthDate)
        saveImage(image, userDB)
        userRepo.save(userDB)
        redirectAttributes.addFlashAttribute("message", "Se ha editado correctamente")
        return "redirect:/profile"
    }

    @GetMapping("/profile/delete")
    fun deleteProfile(ra: RedirectAttributes): String {
        val authentication: Authentication? = SecurityContextHolder.getContext().authentication
        if (authentication == null || authentication is AnonymousAuthenticationToken) {
            ra.addFlashAttribute("error", "Debes iniciar sesión para poder eliminar tu perfil")
            return "redirect:/login"
        }
        val user = userRepo.findByUsername(authentication.name) as User
        userRepo.delete(user)
        ra.addFlashAttribute("message", "Se ha eliminado correctamente. Esperamos verte pronto")
        return "redirect:/login"
    }

    @PostMapping("/askAuthor")
    fun askAuthor(@RequestParam("message", required = false) message:String?, redirectAttributes: RedirectAttributes): String {
        val authentication: Authentication? = SecurityContextHolder.getContext().authentication
        if (authentication == null || authentication is AnonymousAuthenticationToken) {
            redirectAttributes.addFlashAttribute("error", "Debes iniciar sesión para poder solicitar una cuenta de autor")
            return "redirect:/login"
        }
        val userDB = userRepo.findByUsername(authentication.name) as User
        val authorRequest = AuthorRequest()

        authorRequest.username = userDB.username
        authorRequest.email = userDB.email
        authorRequest.message = message
        authorRequest.status = "PENDIENTE"
        authorRequest.createdAt = Date(System.currentTimeMillis())
        authorRequestRepo.save(authorRequest)

        redirectAttributes.addFlashAttribute("message", "Se ha solicitado correctamente. Esperamos que te acepten")
        return "redirect:/profile"
    }


    @GetMapping("/library")
    fun showLibrary(model: Model, @RequestParam("page", required = false) pageNumber: Int?, @RequestParam("title", required = false) title: String?,
                    @RequestParam("type", required = false) type: String?, @RequestParam("demography", required = false) demography: String?,
                    @RequestParam("genres[]", required = false) genres: Array<String>?, @RequestParam("order") orderBy: String?,
                    @RequestParam("order_dir") orderDir: String?): String {
        val page: Int = pageNumber?.minus(1) ?: 0
        val pageable = PageRequest.of(page, 10)
        val samplesPage: Page<Sample>
        var samples: List<Sample>
        var parameters=""

        if (title!=null && title!="") {
            samples = sampleRepo.findAllByNameContaining(title)
            parameters+="&title=$title"
            model.addAttribute("title", title)
        } else{
            samples = sampleRepo.findAll()
        }

        if (type!=null && type!="") {
            samples = samples.filter { it.attributes.contains(attributeRepo.findByName(type)) }
            parameters+="&type=$type"
            model.addAttribute("type", type)
        }

        if (demography!=null && demography!="") {
            samples = samples.filter { it.attributes.contains(attributeRepo.findByName(demography)) }
            parameters+="&demography=$demography"
            model.addAttribute("demography", demography)
        }

        if (genres!=null && genres.isNotEmpty()) {
            samples = samples.filter { sample -> sample.attributes.containsAll(genres.map { attributeRepo.findByName(it) }) }
            genres.forEach { parameters+="&genres%5B%5D=${it.replace(" ","+")}" }
            model.addAttribute("genres", genres)
        }

        if (orderDir==null || orderBy==null){
            samples = samples.sortedBy { it.name }
            parameters+="&order=alphabetically&order_dir=asc"
            model.addAttribute("order", "alphabetically")
            model.addAttribute("order_dir", "asc")
        }
        else {
            if (orderDir == "asc") {
                when (orderBy) {
                    "alphabetically" -> samples = samples.sortedBy { it.name }
                    "rating" -> samples = samples.sortedBy {
                            sample ->
                        val ratings = rateRepo.findAllBySample_Id(sample.id as Long)
                        if(ratings.isEmpty())
                            0.0
                        else
                            ratings.map { it.rating }.average() }
                    "popularity" -> samples = samples.sortedBy {
                            sample ->
                        val ratingsCount = rateRepo.findAllBySample_Id(sample.id as Long).size
                        if(ratingsCount == 0)
                            0
                        else
                            ratingsCount }
                    "publication_date" -> samples = samples.sortedBy { it.publicationDate }
                    "num_chapters" -> samples = samples.sortedBy { chapterRepo.countBySampleId(it.id as Long) }
                }
            } else {
                when (orderBy) {
                    "alphabetically" -> samples = samples.sortedByDescending { it.name }
                    "rating" -> samples = samples.sortedByDescending {
                            sample ->
                        val ratings = rateRepo.findAllBySample_Id(sample.id as Long)
                        if(ratings.isEmpty())
                            0.0
                        else
                            ratings.map { it.rating }.average() }
                    "popularity" -> samples = samples.sortedByDescending {
                            sample ->
                        val ratingsCount = rateRepo.findAllBySample_Id(sample.id as Long).size
                        if(ratingsCount == 0)
                            0
                        else
                            ratingsCount }
                    "publication_date" -> samples = samples.sortedByDescending { it.publicationDate }
                    "num_chapters" -> samples = samples.sortedByDescending { chapterRepo.countBySampleId(it.id as Long) }
                }
            }
            parameters+="&order=$orderBy&order_dir=$orderDir"
            model.addAttribute("order", orderBy)
            model.addAttribute("order_dir", orderDir)
        }

        if(samples.isEmpty()) {
            model.addAttribute("search_message", "No se encontraron resultados")
        }
        else {
            val start = pageable.offset.toInt()
            val end = (start + pageable.pageSize).coerceAtMost(samples.size)
            samplesPage = PageImpl(samples.subList(start, end), pageable, samples.size.toLong())
            val totalPage = samplesPage.totalPages
            if (totalPage > 0){
                val pages = IntStream.rangeClosed(1, totalPage).toList()
                model.addAttribute("pages", pages)
            }
            model.addAttribute("list_samples", samplesPage.content)
            model.addAttribute("list_samples_id", samplesPage.content.map { it.id as Long })
            model.addAttribute("last", totalPage)
            model.addAttribute("current", page+1)
        }

        model.addAttribute("next", page+2)
        model.addAttribute("prev", page)
        model.addAttribute("parameters", parameters)
        model.addAttribute("all_types", attributeRepo.findByType_Name("tipo de libro")
            .sortedBy { it.name }.map { it.name?.substring(0, 1)?.uppercase() + it.name?.substring(1) })
        model.addAttribute("all_genres", attributeRepo.findByType_Name("género")
            .sortedBy { it.name }.map { it.name?.substring(0, 1)?.uppercase() + it.name?.substring(1) })
        model.addAttribute("all_demographics", attributeRepo.findByType_Name("demografía")
            .sortedBy { it.name }.map { it.name?.substring(0, 1)?.uppercase() + it.name?.substring(1) })
        model.addAttribute("is_library", true)
        return "library"
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
                   @RequestParam("demography") demography:String, @RequestParam("fileImage") image: MultipartFile,
                   @RequestParam("genres[]") genres: Array<String>, ra: RedirectAttributes): String {

        sample.attributes.addAll(genres.map { attributeRepo.findByName(it) })
        sample.attributes.add(attributeRepo.findByName(type))
        sample.attributes.add(attributeRepo.findByName(demography))

        if (sampleRepo.findByName(sample.name as String) != null && sampleRepo.findByName(sample.name as String)!!.attributes.contains(attributeRepo.findByName(type))) {
            ra.addFlashAttribute("error", "${sample.name} ya existe")
            return "redirect:/register_sample"
        }
        sampleRepo.save(sample)
        saveImage(image, sample)
        sampleRepo.save(sample)

        ra.addFlashAttribute("message", "${sample.name} registrado correctamente")
        return "redirect:/home"
    }

    @GetMapping("/library/{type}/{id}/{name}")
    fun showSample(model: Model, @PathVariable type: String, @PathVariable id: Long, @PathVariable name: String,
                   authentication: Authentication?): String {
        val sample = sampleRepo.findById(id).get()
        val chapters = chapterRepo.findAllBySampleId(id).reversed()
        val average = getSampleAverageRate(sample.id as Long)
        val urlSampleName = URLSampleName(sample)

        if (authentication == null || authentication is AnonymousAuthenticationToken) {
            model.addAttribute("is_favorite", false)
            model.addAttribute("is_followed", false)
        }
        else {
            val user = userRepo.findByUsername(authentication.name) as User
            model.addAttribute("user_sample_lists", listRepo.findAllByUser_Username(authentication.name))
            model.addAttribute("viewedChapters", user.viewedChapters)
            model.addAttribute("is_favorite", user.favoriteSamples.contains(sample))
            model.addAttribute("is_followed", user.followedSamples.contains(sample))
            model.addAttribute("userRate", rateRepo.findByUser_IdAndSample_Id(user.id as Long, sample.id as Long)?.rating?:0)

        }
        model.addAttribute("average", average.body)
        model.addAttribute("sample", sample)
        model.addAttribute("type", type)
        model.addAttribute("demography", sample.attributes.find { it.type?.name == "demografía"})
        model.addAttribute("genres",sample.attributes.filter { attribute -> attribute.type?.name == "género" }.map
        { attribute -> (attribute.name as String).substring(0, 1).uppercase() + (attribute.name as String).substring(1) })
        model.addAttribute("chapters", chapters)
        model.addAttribute("urlSampleName", urlSampleName)
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
        model.addAttribute("urlSampleName", URLSampleName(sample))
        return "sample_form"
    }

    @PostMapping("/perform_sample_edit")
    fun saveSampleEdit(sample: Sample, @RequestParam("id") id: Long,
                       @RequestParam("type") type: String,
                       @RequestParam("demography") demography:String, @RequestParam("fileImage") image: MultipartFile,
                       @RequestParam("genres[]") genres: Array<String>, ra: RedirectAttributes): String {
        val sampleToUpdate = sampleRepo.findById(id).get()
        sampleToUpdate.name = sample.name
        sampleToUpdate.synopsis = sample.synopsis
        sampleToUpdate.attributes.clear()
        sampleToUpdate.attributes.addAll(genres.map { attributeRepo.findByName(it) })
        sampleToUpdate.attributes.add(attributeRepo.findByName(type))
        sampleToUpdate.attributes.add(attributeRepo.findByName(demography))
        saveImage(image, sampleToUpdate)
        sampleRepo.save(sampleToUpdate)

        val urlSampleName = URLSampleName(sample)
        ra.addFlashAttribute("message", "Se han guardado los cambios de ${sample.name}")
        return "redirect:/library/$type/${sample.id}/$urlSampleName"
    }

    private fun saveImage(
        image: MultipartFile,
        pathable: Pathable
    ) {
        val uploadDir = pathable.path() as String

        val uploadPath = Paths.get(uploadDir)
        if (!uploadPath.exists()) {
            uploadPath.toFile().mkdirs()
        }

        if (!image.isEmpty) {
            if (pathable.existsImage()) {
                val oldCover = Paths.get(pathable.imagePath())
                if (oldCover.exists()) {
                    oldCover.toFile().delete()
                }
            }
            pathable.setPersonalizedImageName(image)

            val imagePath = Paths.get(pathable.imagePath())
            Files.copy(image.inputStream, imagePath, StandardCopyOption.REPLACE_EXISTING)
        }
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
        val urlSampleName = URLSampleName(sample)
        ra.addFlashAttribute("message", "Valoración añadida correctamente")
        return "redirect:/library/$type/${sample.id}/$urlSampleName"
    }

    private fun URLSampleName(sample: Sample): String {
        val urlRegex = """\s|\\|/""".toRegex()
        val urlSampleName = urlRegex.replace(sample.name as String, "-").replace("?", "")
        return urlSampleName
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

    @GetMapping("/library/{type}/{id}/{name}/delete")
    fun deleteSample(@PathVariable type: String, @PathVariable id: Long, @PathVariable name: String, ra: RedirectAttributes): String {
        val sample = sampleRepo.findById(id).get()
        chapterRepo.deleteAll(chapterRepo.findAllBySampleId(id))
        sampleRepo.delete(sample)
        val uploadDir = sample.path() as String
        val uploadPath = Paths.get(uploadDir)
        if (uploadPath.exists()) {
            uploadPath.toFile().deleteRecursively()
        }

        ra.addFlashAttribute("message", "Se ha eliminado ${sample.name} correctamente")
        return "redirect:/home"
    }

    @GetMapping("/upload_chapter/{sampleId}")
    fun showUploadChapterForm(model: Model, @PathVariable sampleId: Long): String {
        val sample = sampleRepo.findById(sampleId).get()
        model.addAttribute("sample", sample)
        model.addAttribute("chapter_number", chapterRepo.findAllBySampleId(sampleId).size + 1)
        model.addAttribute("sample_type", sample.attributes.find { it.type?.name == "tipo de libro" }?.name)
        return "chapter_form"
    }

    @PostMapping("/perform_chapter_upload")
    fun saveChapter(@RequestParam("files[]") images: Array<MultipartFile>,
                    @RequestParam("sample_id") sampleId: Long, @RequestParam("chapter_title") chapterTitle:String?): ResponseEntity<String> {

        val chapter = Chapter()
        val sample = sampleRepo.findById(sampleId).get()

        chapter.number = chapterRepo.countBySampleId(sample.id as Long) + 1
        chapter.sample = sample
        if (chapterTitle != null) {
            chapter.title = chapterTitle
        } else {
            chapter.title = ""
        }

        val regex = """\s|\*|"|\?|\\|>|/|<|:|\|""".toRegex()
        for (i in images.indices) {
            val image = images[i]
            val name = "${regex.replace(sample.name as String, "_")}-chapter_${chapter.number}-${i + 1}.${image.contentType?.split("/")?.last()}"
            chapter.images.add(name)
        }
        val savedChapter = chapterRepo.save(chapter)

        val uploadDir = savedChapter.path()

        val uploadPath = Paths.get(uploadDir)
        if (!uploadPath.exists()) {
            uploadPath.toFile().mkdirs()
        }
        savedChapter.images.forEachIndexed { index, image ->
            val imagePath = uploadPath.resolve(image)
            Files.copy(images[index].inputStream, imagePath, StandardCopyOption.REPLACE_EXISTING)
        }
        return ResponseEntity.ok("El capítulo ${chapter.number} se ha creado correctamente. Redirigiendo a la página del libro...")

    }

    @GetMapping("/view/{type}/{sampleId}/{sampleName}/chapters/{number}")
    fun showChapter(model: Model, @PathVariable type: String, @PathVariable sampleId: Long, @PathVariable number:Long,
                    @PathVariable sampleName: String, ra: RedirectAttributes, authentication: Authentication?): String {
        if (authentication == null || authentication is AnonymousAuthenticationToken) {
            ra.addFlashAttribute("error", "Debes iniciar sesión para poder leer un capítulo")
            return "redirect:/login"
        }
        val user = userRepo.findByUsername(authentication.name) as User
        val chapter = chapterRepo.findBySampleIdAndNumber(sampleId, number)
        if (chapter == null) {
            ra.addFlashAttribute("error", "El capítulo no existe")
            return "redirect:/home"
        }
        if (user.favoriteSamples.contains(chapter.sample) || user.favoriteSamples.contains(chapter.sample)) {
            user.viewedChapters.add(chapter)
            userRepo.save(user)
        }
        val chapters = chapterRepo.findAllBySampleId(sampleId)
        if (!user.isPremium() && number > chapters.size - 3) {
            ra.addFlashAttribute("error", "Debes ser usuario premium para leer este capítulo")
            return "redirect:/home"
        }
        when(type){
            "manga" -> model.addAttribute("reading_direction", "rtl")
            else -> model.addAttribute("reading_direction", "ltr")
        }
        val prevChapter = chapters.find { it.number == number - 1 }?.number
        if (prevChapter != null){
            model.addAttribute("prev_chapter_url", "/view/$type/$sampleId/$sampleName/chapters/$prevChapter")
        }
        val nextChapter = chapters.find { it.number == number + 1 }?.number
        if (nextChapter != null){
            model.addAttribute("next_chapter_url", "/view/$type/$sampleId/$sampleName/chapters/$nextChapter")
        }
        model.addAttribute("imageNumberList", chapter.images.map { it.split("-").last().split(".").first().toInt()})
        model.addAttribute("chapter", chapter)
        model.addAttribute("currentUrl", "/view/$type/$sampleId/$sampleName/chapters/$number")
        model.addAttribute("sampleUrl", "/library/$type/$sampleId/$sampleName")
        return "chapter_view"
    }

    @GetMapping("/images/users/{userId}/avatar")
    fun getUserAvatar(@PathVariable userId: Long): ResponseEntity<ByteArray> {
        val user = userRepo.findById(userId).get()
        val imagePath = Paths.get(user.imagePath())
        return ResponseEntity.ok(Files.readAllBytes(imagePath))
    }

    @GetMapping("/images/users/current/avatar")
    fun getCurrentUserAvatar(authentication: Authentication): ResponseEntity<ByteArray> {
        val user = userRepo.findByUsername(authentication.name) as User
        val imagePath = Paths.get(user.imagePath())
        return ResponseEntity.ok(Files.readAllBytes(imagePath))
    }


    @GetMapping("/images/samples/{id}")
    fun getSampleCover(@PathVariable id: Long): ResponseEntity<ByteArray> {
        val sample = sampleRepo.findById(id).get()
        val imagePath = Paths.get(sample.imagePath())
        return ResponseEntity.ok().body(Files.readAllBytes(imagePath))
    }

    @GetMapping("/images/lists/{id}")
    fun getListCover(@PathVariable id: Long): ResponseEntity<ByteArray> {
        val list = listRepo.findById(id).get()
        val imagePath = Paths.get(list.imagePath())
        return ResponseEntity.ok().body(Files.readAllBytes(imagePath))
    }

    @GetMapping("/images/samples/{sampleId}/chapters/{number}/{imageNumber}")
    fun getChapterImages(@PathVariable sampleId: Long, @PathVariable number: Long,
                         @PathVariable imageNumber: Number): ResponseEntity<ByteArray> {
        val chapter = chapterRepo.findBySampleIdAndNumber(sampleId, number) as Chapter
        return if (chapter.imagesPathList().isNotEmpty()) {
            val images = chapter.imagesPathList().map { imagePath ->
                val image = Files.readAllBytes(Paths.get(imagePath))
                image
            }
           ResponseEntity.ok(images[imageNumber.toInt()-1])
        } else {
            val image = Files.readAllBytes(Paths.get("./static/images/loading.gif"))
            ResponseEntity.ok(image)
        }
    }

    @GetMapping("/lists/create")
    fun listForm(model: Model, authentication: Authentication?, ra: RedirectAttributes): String {
        if (authentication == null || authentication is AnonymousAuthenticationToken) {
            ra.addFlashAttribute("error", "Debes iniciar sesión para poder crear una lista")
            return "redirect:/login"
        }
        model.addAttribute("list", UserSamplesList())
        model.addAttribute("is_register", true)
        return "list_form"
    }

    @PostMapping("/lists/create")
    fun createUserSampleList(authentication: Authentication,
                             @RequestParam("listName") listName: String,
                             @RequestParam("listDescription", required = false) listDescription: String?,
                             ra: RedirectAttributes,@RequestParam fileImage:MultipartFile): String {
        val user = userRepo.findByUsername(authentication.name)
        val list = UserSamplesList()
        list.isPublic = false
        list.name= listName
        list.description= listDescription
        list.user= user
        listRepo.save(list)
        saveImage(fileImage, list)
        listRepo.save(list)
        ra.addFlashAttribute("message", "Se ha creado la lista ${list.name} correctamente")
        return "redirect:/lists/{id}".replace("{id}", list.id.toString())
    }

    @GetMapping("/profile/lists")
    fun showListPage(model: Model, authentication: Authentication?, ra: RedirectAttributes): String {
        if (authentication == null || authentication is AnonymousAuthenticationToken) {
            ra.addFlashAttribute("error", "Debes iniciar sesión para poder ver tus listas")
            return "redirect:/login"
        }
        val lists = listRepo.findAllByUser_Username(authentication.name)
        model.addAttribute("lists", lists)
        model.addAttribute("is_lists", true)
        return "user_lists"
    }

    @GetMapping("/lists")
    fun showListsPage(@RequestParam("page", required = false) pageNumber: Int?,model: Model,
                      authentication: Authentication?, ra: RedirectAttributes,@RequestParam title: String?): String {
        val page:Int = pageNumber?.minus(1) ?:0
        val pageable= PageRequest.of(page, 10)
        val listsPage: Page<UserSamplesList>
        var lists = listRepo.findAllByIsPublicTrue()
        if (title != null && title != ""){
            lists = lists.filter { (it.name as String).contains(title) }}
        if (lists.isEmpty()){
            model.addAttribute("search_message", "No se han encontrado listas")
        }else{
            val start = pageable.offset.toInt()
            val end = (start + pageable.pageSize).coerceAtMost(lists.size)
            listsPage = PageImpl(lists.subList(start, end), pageable, lists.size.toLong())
            val totalPage = listsPage.totalPages
            if (totalPage > 0) {
                val pages = IntStream.rangeClosed(1, totalPage).toList()
                model.addAttribute("pages", pages)
            }
            model.addAttribute("lists", listsPage.content)
            model.addAttribute("lists_id", listsPage.content.map { it.id })
            model.addAttribute("last", totalPage)
            model.addAttribute("current", page + 1)
        }
        model.addAttribute("next", page + 2)
        model.addAttribute("prev", page)
        model.addAttribute("title", title?:"")
        return "lists"
    }

    @GetMapping("/list_followers_count")
    fun getListFollowersCount(@RequestParam("listId") listId: Long): ResponseEntity<Int> {
        val list = listRepo.findById(listId).get()
        return ResponseEntity.ok(list.followers.size)
    }

    @GetMapping("/lists/{id}")
    fun showList(@PathVariable id: Long, model: Model,ra: RedirectAttributes, authentication: Authentication?) :String{
        val list = listRepo.findById(id).get()
        val followers = getListFollowersCount(list.id as Long)
        model.addAttribute("followers", followers.body)
        return if (list.isPublic as Boolean || list.user?.username == authentication?.name) {
            model.addAttribute("list",list)
            model.addAttribute("list_samples_id", list.samples.map { it.id })
            if (list.user?.username == authentication?.name) {
                model.addAttribute("is_user", true)
            }else{
                model.addAttribute("is_user", false)
            }
            if (authentication != null || authentication !is AnonymousAuthenticationToken) {
                model.addAttribute("is_followed", list.followers.contains(userRepo.findByUsername((authentication as Authentication).name)))

            }
            "list_view"
        } else {
            ra.addFlashAttribute("error", "Esta lista no es pública")
            "redirect:/home"
        }
    }

    @PostMapping("/public_list")
    fun makeListPublic(@RequestParam("list_id") sample_id: Long, authentication: Authentication?,ra: RedirectAttributes):String{
        if (authentication == null || authentication is AnonymousAuthenticationToken) {
            ra.addFlashAttribute("error", "Debes iniciar sesión para hacer la lista pública")
            return "redirect:/login"
        }
        if (listRepo.findById(sample_id).get().user?.username != authentication.name) {
            ra.addFlashAttribute("error", "Debes ser el creador de la lista para hacerla pública")
            return "redirect:/lists/${sample_id}"
        }
        val list = listRepo.findById(sample_id).get()
        list.isPublic = true
        listRepo.save(list)
        ra.addFlashAttribute("message", "${list.name} ahora es pública")
        return "redirect:/lists/${list.id}"
    }

    @PostMapping("/private_list")
    fun makeListPrivate(@RequestParam("list_id") sample_id: Long, authentication: Authentication?,ra: RedirectAttributes):String{
        if (authentication == null || authentication is AnonymousAuthenticationToken) {
            ra.addFlashAttribute("error", "Debes iniciar sesión para hacer la lista privada")
            return "redirect:/login"
        }
        if (listRepo.findById(sample_id).get().user?.username != authentication.name) {
            ra.addFlashAttribute("error", "Debes ser el creador de la lista para hacerla privada")
            return "redirect:/lists/${sample_id}"
        }
        val list = listRepo.findById(sample_id).get()
        list.isPublic = false
        listRepo.save(list)
        ra.addFlashAttribute("message", "${list.name} ahora es privada")
        return "redirect:/lists/${list.id}"
    }

    @PostMapping("/add_user_to_followed_list")
    fun addUserToFollowedList(@RequestParam("list_id") sample_id: Long, authentication: Authentication?,ra: RedirectAttributes):String{
        if (authentication == null || authentication is AnonymousAuthenticationToken) {
            ra.addFlashAttribute("error", "Debes iniciar sesión para poder agregar a listas seguidas")
            return "redirect:/login"
        }
        val user = userRepo.findByUsername(authentication.name) as User
        val list = listRepo.findById(sample_id).get()
        list.followers.add(user)
        listRepo.save(list)
        ra.addFlashAttribute("message", "Se ha agregado ${list.name} a listas seguidas")
        return "redirect:/lists/${list.id}"
    }

    @PostMapping("/remove_user_from_followed_list")
    fun removeUserFromFollowedList(@RequestParam("list_id") sample_id: Long, authentication: Authentication?,ra: RedirectAttributes):String{
        if (authentication == null || authentication is AnonymousAuthenticationToken) {
            ra.addFlashAttribute("error", "Debes iniciar sesión para poder remover a listas seguidas")
            return "redirect:/login"
        }
        val user = userRepo.findByUsername(authentication.name) as User
        val list = listRepo.findById(sample_id).get()
        list.followers.remove(user)
        listRepo.save(list)
        ra.addFlashAttribute("message", "Se ha eliminado ${list.name} de listas seguidas")
        return "redirect:/lists/${list.id}"
    }

    @GetMapping("/followed_lists")
    fun showFollowedListsPage(@RequestParam("page", required = false) pageNumber: Int?,model: Model,
                      authentication: Authentication?, ra: RedirectAttributes,@RequestParam title: String?): String {
        if (authentication == null || authentication is AnonymousAuthenticationToken) {
            ra.addFlashAttribute("error", "Debes iniciar sesión para poder ver tus listas seguidas")
            return "redirect:/login"
        }
        val page:Int = pageNumber?.minus(1) ?:0
        val pageable= PageRequest.of(page, 10)
        val listsPage: Page<UserSamplesList>
        val lists = listRepo.findAllByFollowersContaining(authentication.name)
        if (lists.isEmpty()){
            model.addAttribute("search_message", "No se han encontrado listas")
        }else{
            val start = pageable.offset.toInt()
            val end = (start + pageable.pageSize).coerceAtMost(lists.size)
            listsPage = PageImpl(lists.subList(start, end), pageable, lists.size.toLong())
            val totalPage = listsPage.totalPages
            if (totalPage > 0) {
                val pages = IntStream.rangeClosed(1, totalPage).toList()
                model.addAttribute("pages", pages)
            }
            model.addAttribute("lists", listsPage.content)
            model.addAttribute("lists_id", listsPage.content.map { it.id })
            model.addAttribute("last", totalPage)
            model.addAttribute("current", page + 1)
        }
        model.addAttribute("next", page + 2)
        model.addAttribute("prev", page)
        return "followed_list_view"
    }

    @PostMapping("/add_sample_to_list")
    fun addSampleToUserList(@RequestParam("sample_id") sample_id: Long, @RequestParam("list_id") list_id: Long, ra: RedirectAttributes):String{
        val list = listRepo.findById(list_id).get()
        val sample = sampleRepo.findById(sample_id).get()
        if (list.samples.contains(sample)) {
            ra.addFlashAttribute("info", "La lista '${list.name}' ya contiene: ${sample.name}")
            return "redirect:/lists/${list.id}"
        }
        list.samples.add(sample)
        listRepo.save(list)
        ra.addFlashAttribute("message", "Se ha agregado ${sample.name} a la lista ${list.name}")
        return "redirect:/lists/${list.id}"
    }

    @ResponseBody
    @PostMapping("/set_status/{sampleId}/{status}")
    fun setStatus(@PathVariable sampleId: Long, @PathVariable status: String,
                  authentication: Authentication) :Map<String, String>{
        var message = ""
        val user = userRepo.findByUsername(authentication.name) as User
        val sample = sampleRepo.findById(sampleId).get()
        val type = "success"
        when (status) {
            "follow" -> {
                user.followedSamples.add(sample)
                userRepo.save(user)
                message = "Has seguido a ${sample.name}"
            }
            "favorite" -> {
                user.favoriteSamples.add(sample)
                userRepo.save(user)
                message = "Has marcado ${sample.name} como favorito"
            }
            "uncheck_follow" -> {
                user.followedSamples.remove(sample)
                userRepo.save(user)
                message = "Has dejado de seguir a ${sample.name}"
            }
            "uncheck_favorite" -> {
                user.favoriteSamples.remove(sample)
                userRepo.save(user)
                message = "Has dejado de marcar ${sample.name} como favorito"
            }
        }
        if (!user.favoriteSamples.contains(sample) && !user.followedSamples.contains(sample)) {
            user.viewedChapters.removeAll(chapterRepo.findAllBySampleId(sampleId).toSet())
            userRepo.save(user)
        }

        return mapOf("message" to message, "type" to type)
    }

    @ResponseBody
    @PostMapping("/set_viewed/{chapterId}")
    fun setViewed(@PathVariable chapterId: Long, authentication: Authentication):Map<String, String>{
        val user = userRepo.findByUsername(authentication.name) as User
        val chapter = chapterRepo.findById(chapterId).get()
        val type:String
        val message:String
        if (!user.favoriteSamples.contains(chapter.sample) && !user.followedSamples.contains(chapter.sample)) {
            type = "info"
            message = "Debes seguir o guardar en favoritos esta obra para marcar los capítulos vistos"
        } else {
            if (user.viewedChapters.contains(chapter)) {
                user.viewedChapters.remove(chapter)
                userRepo.save(user)
                type = "success"
                message = "Has desmarcado el capítulo ${chapter.number}"
            }else {
                user.viewedChapters.add(chapter)
                userRepo.save(user)
                type = "success"
                message = "Has marcado el capítulo ${chapter.number} como visto"
            }
        }
        return mapOf("message" to message, "type" to type)
    }

    @ResponseBody
    @PostMapping("/{sampleId}/set_all_chapters_not_viewed")
    fun setAllNotViewed(@PathVariable sampleId: Long, authentication: Authentication):Map<String, String>{
        val user = userRepo.findByUsername(authentication.name) as User
        val type:String
        val message:String
        if (user.viewedChapters.isEmpty()) {
            type = "error"
            message = "No has marcado ningún capítulo"
        } else {
            user.viewedChapters.removeAll(chapterRepo.findAllBySampleId(sampleId).toSet())
            userRepo.save(user)
            type = "success"
            message = "Se han desmarcado todos los capítulos"
        }
        return mapOf("type" to type, "message" to message)
    }

    @GetMapping("/favorites")
    fun showFavorite(authentication: Authentication?, model: Model, ra: RedirectAttributes): String {
        if (authentication == null || authentication is AnonymousAuthenticationToken) {
            ra.addFlashAttribute("error", "Debes iniciar sesión para poder ver tus favoritos")
            return "redirect:/login"
        }
        val user = userRepo.findByUsername(authentication.name) as User
        val list = user.favoriteSamples
        model.addAttribute("list", list)
        model.addAttribute("list_samples_id", list.map { it.id })
        return "favorites_view"
    }

    @GetMapping("/followeds")
    fun showFolloweds(authentication: Authentication?, model: Model, ra: RedirectAttributes): String {
        if (authentication == null || authentication is AnonymousAuthenticationToken) {
            ra.addFlashAttribute("error", "Debes iniciar sesión para poder ver tus obras seguidas")
            return "redirect:/login"
        }
        val user = userRepo.findByUsername(authentication.name) as User
        val list = user.followedSamples
        model.addAttribute("list", list)
        model.addAttribute("list_samples_id", list.map { it.id })
        return "followeds_view"
    }

    @PostMapping("/failed_login")
    fun failedLogin(ra: RedirectAttributes): String {
        ra.addFlashAttribute("error", "Usuario o contraseña incorrectos")
        return "redirect:/login"
    }

    @GetMapping("/requests")
    fun showRequests(model: Model, @RequestParam("page", required = false) pageNumber: Int?, authentication: Authentication?, ra: RedirectAttributes): String {
        if (authentication == null || authentication is AnonymousAuthenticationToken) {
            ra.addFlashAttribute("error", "Debes iniciar sesión para poder ver las solicitudes")
            return "redirect:/login"
        }
        val user = userRepo.findByUsername(authentication.name) as User
        if (!user.roles.contains(roleRepo.findByName("ADMIN"))) {
            ra.addFlashAttribute("error", "No tienes permisos para ver esta página")
            return "redirect:/"
        }
        val page: Int = pageNumber?.minus(1) ?:  0
        val pageable = PageRequest.of(page, 10)
        val requests = authorRequestRepo.findAll(pageable)
        if(requests.isEmpty) {
            model.addAttribute("search_message", "No se encontraron solicitudes")
        }
        else {
            val totalPage = requests.totalPages
            if (totalPage > 0){
                val pages = IntStream.rangeClosed(1, totalPage).toList()
                model.addAttribute("pages", pages)
            }
            model.addAttribute("requests", requests.content)
            model.addAttribute("last", totalPage)
            model.addAttribute("current", page+1)
        }

        model.addAttribute("next", page+2)
        model.addAttribute("prev", page)
        return "requests"
    }

    @PostMapping("/make_author")
    fun makeAuthor(@RequestParam requestId: Long, authentication: Authentication, ra: RedirectAttributes): String {
        val request = authorRequestRepo.findById(requestId).get()
        val user = userRepo.findByUsername(request.username as String) as User
        user.roles.add(roleRepo.findByName("AUTHOR"))
        userRepo.save(user)
        request.status = "ACEPTADO"
        request.updatedAt = Date.valueOf(LocalDate.now())
        request.updatedBy = authentication.name
        authorRequestRepo.save(request)

        ra.addFlashAttribute("message", "Se ha aceptado la solicitud de ${request.username}")
        return "redirect:/requests"
    }

    @PostMapping("/reject_author")
    fun rejectAuthor(@RequestParam requestId: Long, @RequestParam rejectReason:String, authentication: Authentication, ra: RedirectAttributes): String {
        val request = authorRequestRepo.findById(requestId).get()
        request.status = "RECHAZADO"
        request.updatedAt = Date.valueOf(LocalDate.now())
        request.updatedBy = authentication.name
        request.rejectReason = rejectReason
        authorRequestRepo.save(request)
        ra.addFlashAttribute("message", "Se ha rechazado la solicitud de ${request.username}")
        return "redirect:/requests"
    }

    @PostMapping("/create-checkout-session")
    fun createCheckoutSession(): String {
        Stripe.apiKey = "sk_test_51LLdV2HCtZNMr4LMvyxwWpjWnUocMZ4UyZof7ojWoJp7EJoDck43VeAfZKKFuqGC3j2Z4tLE8fjG36WbV8oG6mqB00dBdGlQej";

        val YOUR_DOMAIN = "http://localhost:8080"
        val params: SessionCreateParams = SessionCreateParams.builder()
            .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
            .setSuccessUrl("$YOUR_DOMAIN/success?session_id={CHECKOUT_SESSION_ID}")
            .setCancelUrl("$YOUR_DOMAIN/home")
            .addLineItem(
                SessionCreateParams.LineItem.builder()
                    .setQuantity(1L) // Provide the exact Price ID (for example, pr_1234) of the product you want to sell
                    .setPrice("price_1LLdcbHCtZNMr4LMYNVriHrn")
                    .build()
            )
            .build()
        return "redirect:"+ Session.create(params).url
    }

    @GetMapping("/create-portal-session")
    fun createPortalSession(authentication: Authentication): String {
        Stripe.apiKey = "sk_test_51LLdV2HCtZNMr4LMvyxwWpjWnUocMZ4UyZof7ojWoJp7EJoDck43VeAfZKKFuqGC3j2Z4tLE8fjG36WbV8oG6mqB00dBdGlQej";

        val YOUR_DOMAIN = "http://localhost:8080"

        val session = (userRepo.findByUsername(authentication.name) as User).stripeId
        val customer = Session.retrieve(session).customer
        // Authenticate your user.
        val params = com.stripe.param.billingportal.SessionCreateParams.Builder()
            .setReturnUrl(YOUR_DOMAIN).setCustomer(customer).build()

        val portalSession = com.stripe.model.billingportal.Session.create(params)


        return "redirect:"+portalSession.url
    }

    @GetMapping("/success")
    fun success(@RequestParam session_id: String, ra: RedirectAttributes ,authentication: Authentication): String {
        val user = userRepo.findByUsername(authentication.name) as User
        user.stripeId = session_id
        userRepo.save(user)
        ra.addFlashAttribute("message", "Te has convertido en usuario premium correctamente")
        return "redirect:/profile"
    }

    @GetMapping("/isPremium")
    fun isPremium(authentication: Authentication?): ResponseEntity<Boolean> {
        if (authentication == null || authentication is AnonymousAuthenticationToken) {
            return ResponseEntity.ok(false)
        }
        val user = userRepo.findByUsername(authentication.name) as User
        return ResponseEntity.ok(user.isPremium())
    }

    @GetMapping("/pendings")
    fun showChaptersPendings(model: Model, authentication: Authentication?, ra: RedirectAttributes):String{
        if (authentication == null || authentication is AnonymousAuthenticationToken) {
            ra.addFlashAttribute("error", "Debes iniciar sesión para ver tus capítulos pendientes")
            return "redirect:/login"
        }
        val user = userRepo.findByUsername(authentication.name) as User
        val savedSamples: MutableSet<Sample> = mutableSetOf()
        savedSamples.addAll(user.followedSamples)
        savedSamples.addAll(user.favoriteSamples)

        val savedSamplesWithChaptersPending = savedSamples.filter { sample -> chapterRepo.findAllBySampleId(sample.id as Long).isNotEmpty() &&
                !chapterRepo.findAllBySampleId(sample.id as Long).containsAll(user.viewedChapters.filter { (it.sample as Sample) == sample})}.toMutableSet()
        val totalChapterForEachSavedSample = mutableListOf<Int>()
        totalChapterForEachSavedSample.addAll(savedSamplesWithChaptersPending.map { sample -> chapterRepo.findAllBySampleId(sample.id as Long).size })
        val lastViewedChapterNumberForEachSavedSample = mutableListOf<Long>()
        lastViewedChapterNumberForEachSavedSample.addAll(savedSamplesWithChaptersPending.map { sample -> user.viewedChapters.filter { (it.sample as Sample) == sample }.map { it.number }.maxOf { it as Long }})

        val followedSamplesWithChaptersPending = savedSamplesWithChaptersPending.filter { sample -> user.followedSamples.contains(sample) }.toMutableSet()

        model.addAttribute("followedSamples", followedSamplesWithChaptersPending)
        model.addAttribute("savedSamples", savedSamplesWithChaptersPending)
        model.addAttribute("savedSamplesId", savedSamplesWithChaptersPending.map { it.id })
        model.addAttribute("totalChapterSamples", totalChapterForEachSavedSample)
        model.addAttribute("lastViewedChapterSamples", lastViewedChapterNumberForEachSavedSample)

        return "pendings"
    }

    /*@PostMapping("/webhooks")
    fun handleStripeEvent(@RequestHeader("Stripe-Signature") sigHeader: String,@RequestBody payload:String, authentication: Authentication): String {
        val endpointSecret = "whsec_8a8bd3d32a03e57eb56ecba89571ff15cd13833dbf1012992bfd847d78859f05"

        if (sigHeader == null) {return ""}

        var event: Event

        try {
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret)
        } catch (e: SignatureVerificationException) {
            // Invalid signature
            println("⚠️  Webhook error while validating signature.")
            return ""
        }
        // Deserialize the nested object inside the event
        val dataObjectDeserializer = event.dataObjectDeserializer
        var stripeObject: StripeObject? = null
        if (dataObjectDeserializer.getObject().isPresent) {
            stripeObject = dataObjectDeserializer.getObject().get()
        } else {
            // Deserialization failed, probably due to an API version mismatch.
            // Refer to the Javadoc documentation on `EventDataObjectDeserializer` for
            // instructions on how to handle this case, or return an error here.
        }
        // Handle the event
        var subscription: Subscription
        when (event.type as String) {
            "customer.subscription.deleted" -> {
                println("Unhandled event type: ")
            }
            "customer.subscription.trial_will_end" -> {
                println("Unhandled event type: ")
            }
            "customer.subscription.created" -> {
                val user = userRepo.findByUsername(authentication.name) as User
                user.stripeId = "puton"
                userRepo.save(user)
            }
            "customer.subscription.updated" -> {
                println("Unhandled event type: ")
            }
            "payment_intent.succeeded" -> {
                println("Unhandled event type: ")
            }
            else -> println("Unhandled event type: ")
        }
        return ""
        }*/
}