package waffle.guam.util

import org.springframework.web.multipart.MultipartFile
import waffle.guam.InvalidRequestException

object TypeCheck {
    fun validImageFile(imageFile: MultipartFile) {
        if (!imageFile.contentType!!.startsWith("image")) throw InvalidRequestException("이미지만 업로드 가능합니다.")
    }

    fun validChatInput(content: String?, imageFiles: List<MultipartFile>?) {
        if (content.isNullOrBlank() && imageFiles.isNullOrEmpty()) throw InvalidRequestException("입력된 내용이 없습니다.")
        if (!imageFiles.isNullOrEmpty())
            for (imageFile in imageFiles) validImageFile(imageFile)
    }

//    fun validStackInfo(stackInfo: String) {
//        try {
//            StackInfo.of(stackInfo)
//        } catch (e: Exception) {
//            throw InvalidRequestException("stackInfo type check error : ${e.message}")
//        }
//    }
}
