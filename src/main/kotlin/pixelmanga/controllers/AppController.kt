package pixelmanga.controllers

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
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
import pixelmanga.entities.*
import pixelmanga.repositories.*
import pixelmanga.security.CustomUserDetails
import java.nio.file.Files
import java.nio.file.Paths
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

    @Autowired
    private lateinit var listRepo: UserSamplesListRepository

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
            ra.addFlashAttribute("error", "Debes iniciar sesión para poder editar tu perfil")
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

        val id = sample.id as Long

        saveSampleCover(type, id, image, sample)

        ra.addFlashAttribute("message", "${sample.name} registrado correctamente")
        return "redirect:/home"
    }

    @GetMapping("/library")
    fun showLibrary(model: Model, @RequestParam("page", required = false) pageNumber: Int?, @RequestParam("title", required = false) title: String?,
                    @RequestParam("type", required = false) type: String?, @RequestParam("demography", required = false) demography: String?,
                    @RequestParam("genres[]", required = false) genres: Array<String>?, @RequestParam("order") orderBy: String?,
                    @RequestParam("order_dir") orderDir: String?): String {
        val page: Int = pageNumber?.minus(1) ?:  0
        val pageable = PageRequest.of(page, 10)
        val samplePages: Page<Sample>
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
            samplePages = PageImpl(samples.subList(start, end), pageable, samples.size.toLong())
            val totalPage = samplePages.totalPages
            if (totalPage > 0){
                val pages = IntStream.rangeClosed(1, totalPage).toList()
                model.addAttribute("pages", pages)
            }
            model.addAttribute("list_samples", samplePages.content)
            model.addAttribute("list_samples_id", samplePages.content.map { it.id as Long })
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

    @GetMapping("/library/{type}/{id}/{name}")
    fun showSample(model: Model, @PathVariable type: String, @PathVariable id: Long, @PathVariable name: String): String {
        val sample = sampleRepo.findById(id).get()
        val chapters = chapterRepo.findAllBySampleId(id)
        val average = getSampleAverageRate(sample.id as Long)
        val urlSampleName = URLSampleName(sample)

        val authentication: Authentication? = SecurityContextHolder.getContext().authentication
        if (authentication != null || authentication !is AnonymousAuthenticationToken) {
            model.addAttribute("user_sample_lists", listRepo.findByUser_Username((authentication as Authentication).name))
        }
        model.addAttribute("average", average.body)
        model.addAttribute("sample", sample)
        model.addAttribute("type", type)
        model.addAttribute("demography", sample.attributes.find { it.type?.name == "demografía"})
        model.addAttribute("genres",sample.attributes.filter { attribute -> attribute.type?.name == "género" }.map { attribute -> attribute.name })
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
        sampleRepo.save(sampleToUpdate)
        saveSampleCover(type, id, image, sample)

        val urlSampleName = URLSampleName(sample)
        ra.addFlashAttribute("message", "Se han guardado los cambios de ${sample.name}")
        return "redirect:/library/$type/${sample.id}/$urlSampleName"
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

            if (sample.cover != null) {
                val oldCover = Paths.get(sample.coverPath() as String)
                if (oldCover.exists()) {
                    oldCover.toFile().delete()
                }
            }

            sampleRepo.updateCoverById(name, id)

            val imagePath = uploadPath.resolve(name)
            Files.copy(image.inputStream, imagePath)
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
    fun saveChapter(@RequestParam("fileImage") image: MultipartFile,
                    @RequestParam("sample_id") sampleId: Long, ra: RedirectAttributes): String {
        if (image.isEmpty) {
            ra.addFlashAttribute("error", "No se ha seleccionado una imagen")
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

        chapterRepo.save(chapter)
        val uploadDir = "./resources/images/samples/$type/${sample.id}/chapters/${chapter.number}"

        val uploadPath = Paths.get(uploadDir)
        if (!uploadPath.exists()) {
            uploadPath.toFile().mkdirs()
        }
        val imagePath = uploadPath.resolve(name)
        image.transferTo(imagePath)
        val urlSampleName = URLSampleName(sample)
        ra.addFlashAttribute("message", "El capítulo ${chapter.number} se ha registrado correctamente")
        return "redirect:/library/$type/${sample.id}/$urlSampleName"

    }

    @GetMapping("/view/{type}/{sampleId}/{sampleName}/chapters/{number}")
    fun showChapter(model: Model, @PathVariable sampleId: Long, @PathVariable number:Long): String {
        val chapter = chapterRepo.findBySampleIdAndNumber(sampleId, number)
        val chapters = chapterRepo.findAllBySampleId(sampleId)
        model.addAttribute("chapter", chapter)
        model.addAttribute("chapters", chapters)
        model.addAttribute("urlSampleName", URLSampleName(chapter.sample as Sample))
        return "chapter_view"
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

    @GetMapping("/register_list")
    fun ListForm(model: Model, authentication: Authentication?, ra: RedirectAttributes): String {
        if (authentication == null || authentication is AnonymousAuthenticationToken) {
            ra.addFlashAttribute("error", "Debes iniciar sesión para poder crear una lista")
            return "redirect:/login"
        }
        model.addAttribute("username", authentication.name)
        model.addAttribute("list", UserSamplesList())
        model.addAttribute("is_register", true)
        return "list_form"
    }

    @PostMapping("/create_user_sample_list")
    fun createUserSampleList(@RequestParam("user_name") userName: String,
                             @RequestParam("listName") listName: String,
                             @RequestParam("listDescription") listDescription: String,
                             ra: RedirectAttributes): String {
        val user = userRepo.findByUsername(userName)
        val list = UserSamplesList()
        list.name= listName
        list.description= listDescription
        list.user= user
        listRepo.save(list)
        ra.addFlashAttribute("message", "Se ha creado la lista ${list.name} correctamente")
        return "redirect:/lists"
    }

    @GetMapping("/lists")
    fun showListPage(model: Model, authentication: Authentication?, ra: RedirectAttributes): String {
        if (authentication == null || authentication is AnonymousAuthenticationToken) {
            ra.addFlashAttribute("error", "Debes iniciar sesión para poder ver tus listas")
            return "redirect:/login"
        }
        val list = listRepo.findByUser_Username(authentication.name)
        model.addAttribute("list", list)
        model.addAttribute("is_lists", true)
        return "lists"
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

    @GetMapping("/lists/{id}")
    fun showList(@PathVariable id: Long, model: Model) :String{
        val list = listRepo.findById(id).get()
        model.addAttribute("list",list)
        model.addAttribute("list_samples_id", list.samples.map { it.id })
        return "list_view"
    }

    @PostMapping("/add_sample_to_favorite")
    fun addSampleToUserFavorite(@RequestParam("sample_id") sample_id: Long, authentication: Authentication?,ra: RedirectAttributes):String{
        if (authentication == null || authentication is AnonymousAuthenticationToken) {
            ra.addFlashAttribute("error", "Debes iniciar sesión para poder agregar a favoritos")
            return "redirect:/login"
        }
        val user = userRepo.findByUsername(authentication.name) as User
        val sample = sampleRepo.findById(sample_id).get()
        if (user.favoriteSamples.contains(sample)){
            ra.addFlashAttribute("info", "${sample.name} ya está en tus favoritos")
            return "redirect:/favorite"
        }
        user.favoriteSamples.add(sample)
        userRepo.save(user)
        ra.addFlashAttribute("message", "Se ha agregado ${sample.name} a favoritos")
        return "redirect:/favorite"
    }

    @GetMapping("/favorite")
    fun showFavorite(authentication: Authentication?, model: Model, ra: RedirectAttributes): String {
        if (authentication == null || authentication is AnonymousAuthenticationToken) {
            ra.addFlashAttribute("error", "Debes iniciar sesión para poder ver tus favoritos")
            return "redirect:/login"
        }
        val user = userRepo.findByUsername(authentication.name) as User
        val list = user.favoriteSamples
        model.addAttribute("list", list)
        model.addAttribute("list_samples_id", list.map { it.id })
        model.addAttribute("is_following", true)
        return "favorite_view"
    }

    @PostMapping("/failed_login")
    fun failedLogin(ra: RedirectAttributes): String {
        ra.addFlashAttribute("error", "Usuario o contraseña incorrectos")
        return "redirect:/login"
    }
}