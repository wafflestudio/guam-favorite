package waffle.guam.util

import org.springframework.web.multipart.MultipartFile
import waffle.guam.exception.InvalidRequestException

object TypeCheck {
    fun validImageFile(imageFile: MultipartFile) {
        if (!imageFile.contentType!!.startsWith("image")) throw InvalidRequestException("이미지만 업로드 가능합니다.")
    }

    fun validChatInput(content: String?, imageFiles: List<MultipartFile>?) {
        if (content.isNullOrBlank() && imageFiles.isNullOrEmpty()) throw InvalidRequestException("입력된 내용이 없습니다.")
        if (!imageFiles.isNullOrEmpty())
            for (imageFile in imageFiles) validImageFile(imageFile)
    }
}
