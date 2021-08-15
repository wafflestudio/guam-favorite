package waffle.guam.image

import io.kotest.core.spec.style.FeatureSpec
import io.kotest.matchers.shouldBe
import waffle.guam.annotation.DatabaseTest
import waffle.guam.image.model.ImageType
import waffle.guam.image.model.ImageType.Companion.filter

@DatabaseTest()
class ImageTypeFilterTest : FeatureSpec() {
    init {
        feature("이미지 필터 기능") {
            scenario("특정 타입의 이미지들만 모델로 변환한다") {
                val result = ImageType.COMMENT.filter(
                    listOf(
                        ImageEntity(id = 1, type = "COMMENT", parentId = 10),
                        ImageEntity(id = 2, type = "COMMENT", parentId = 10),
                        ImageEntity(id = 3, type = "PROJECT", parentId = 30),
                        ImageEntity(id = 4, type = "COMMENT", parentId = 40),
                        ImageEntity(id = 5, type = "THREAD", parentId = 50),
                    )
                )
                result shouldBe listOf(
                    ImageEntity(id = 1, type = "COMMENT", parentId = 10),
                    ImageEntity(id = 2, type = "COMMENT", parentId = 10),
                    ImageEntity(id = 4, type = "COMMENT", parentId = 40),
                )
            }
        }
    }
}
