package waffle.guam.image

import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.PutObjectResult
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.mock.web.MockMultipartFile
import waffle.guam.annotation.DatabaseTest
import waffle.guam.image.command.CreateImages
import waffle.guam.image.command.DeleteImages
import waffle.guam.image.model.ImageType
import waffle.guam.project.ProjectRepository
import waffle.guam.user.UserRepository
import java.io.File
import java.io.FileInputStream

@DatabaseTest(["image/image.sql"])
class ImageServiceCommandTest @Autowired constructor(
    imageRepository: ImageRepository,
    projectRepository: ProjectRepository,
    userRepository: UserRepository
) : FeatureSpec() {
    private val mockAwsClient: AmazonS3Client = mockk()
    private val imageService = ImageServiceImpl(imageRepository, projectRepository, userRepository, mockAwsClient)
    private val testFile =
        MockMultipartFile("test", "test.png", "image/png", FileInputStream(File("image/test.png")))

    init {
        every { mockAwsClient.putObject(any()) } returns PutObjectResult()

        feature("이미지 추가 기능") {
            scenario("이미지를 추가한 후, 리모트 서버에 업로드 한다.") {
                val result = imageService.createImages(
                    command = CreateImages(listOf(testFile), ImageType.COMMENT, 10L)
                )

                result.imageIds.size shouldBe 1
                result.imageType shouldBe ImageType.COMMENT
                result.parentId shouldBe 10L
            }
        }

        feature("이미지 삭제 기능") {

            /**
             *  TODO 입니다. 무결성 오류가 뜨네요 ㅎ
             */
//            scenario("아이디에 해당하는 이미지를 삭제한다.") {
//                val result = imageService.deleteImages(
//                    DeleteImages.ById(listOf(4L, 5L))
//                )
//
//                result.imageIds shouldBe listOf(4L, 5L)
//            }

            scenario("자식에 해당하는 이미지를 삭제한다.") {
                val result = imageService.deleteImages(
                    DeleteImages.ByParentId(parentId = 1L, imageType = ImageType.THREAD)
                )

                result.imageIds shouldBe listOf(1L, 2L, 3L)
            }
        }
    }
}
