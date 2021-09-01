package waffle.guam.image

import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.PutObjectResult
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.mock.web.MockMultipartFile
import org.springframework.transaction.annotation.Transactional
import waffle.guam.annotation.DatabaseTest
import waffle.guam.image.command.CreateImages
import waffle.guam.image.command.DeleteImages
import waffle.guam.image.model.ImageType
import waffle.guam.project.ProjectRepository
import waffle.guam.user.UserRepository
import java.io.File
import java.io.FileInputStream

@DatabaseTest(["image/data.sql"])
class ImageServiceCommandTest @Autowired constructor(
    imageRepository: ImageRepository,
    projectRepository: ProjectRepository,
    userRepository: UserRepository
) {
    private val mockAwsClient: AmazonS3Client = mockk()
    private val imageService = ImageServiceImpl(imageRepository, projectRepository, userRepository, mockAwsClient)
    private val testFile =
        MockMultipartFile("test", "test.png", "image/png", FileInputStream(File("image/test.png")))

    @Transactional
    @Test
    fun create() {
        every { mockAwsClient.putObject(any()) } returns PutObjectResult()

        val result = imageService.createImages(
            command = CreateImages(listOf(testFile), ImageType.COMMENT, 10L)
        )

        result.imageIds.size shouldBe 1
        result.imageType shouldBe ImageType.COMMENT
        result.parentId shouldBe 10L
    }

    @Transactional
    @Test
    fun delete() {
        val result1 = imageService.deleteImages(
            DeleteImages.ById(listOf(4L, 5L))
        )

        result1.imageIds shouldBe listOf(4L, 5L)

        val result2 = imageService.deleteImages(
            DeleteImages.ByParentId(parentId = 1L, imageType = ImageType.THREAD)
        )

        result2.imageIds shouldBe listOf(1L, 2L, 3L)
    }
}
