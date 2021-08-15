package waffle.guam.test

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.ClassPathResource
import org.springframework.mock.web.MockMultipartFile
import org.springframework.transaction.annotation.Transactional
import waffle.guam.DatabaseTest
import waffle.guam.db.entity.ImageType
import waffle.guam.db.repository.StackRepository
import waffle.guam.service.ImageInfo
import waffle.guam.service.ImageService

/**
 * Caution
 * 사실 테스트가 아님
 */
@DatabaseTest
class StackImgUpload @Autowired constructor(
    private val stackRepository: StackRepository,
    private val imageService: ImageService
) {

    @Transactional
    @Test
    fun uploadFiles() {
        stackRepository.findAll().map { entity ->
            val file = ClassPathResource("stacks/${entity.name}.png").file
            val mock = MockMultipartFile("imageFiles", "${entity.name}.png", "multipart/form-data", file.inputStream())
            val img = imageService.upload(multipartFile = mock, imageInfo = ImageInfo(entity.id, ImageType.STACK))
            entity.thumbnail = img
        }
    }
}
