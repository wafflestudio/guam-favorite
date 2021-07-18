package waffle.guam.util

import org.slf4j.LoggerFactory
import org.springframework.web.multipart.MultipartFile
import waffle.guam.exception.InvalidRequestException

object TypeCheck {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun validImageFile(imageFile: MultipartFile) {
        logger.info("check image content type : ${imageFile.contentType}")

        if (imageFile.contentType?.startsWith("image") != true) {
            throw InvalidRequestException("이미지만 업로드 가능합니다. Content Type : ${imageFile.contentType}")
        }
    }

    fun validChatInput(content: String?, imageFiles: List<MultipartFile>?) {
        if (content.isNullOrBlank() && imageFiles.isNullOrEmpty()) throw InvalidRequestException("입력된 내용이 없습니다.")
        if (!imageFiles.isNullOrEmpty())
            for (imageFile in imageFiles) validImageFile(imageFile)
    }
}
