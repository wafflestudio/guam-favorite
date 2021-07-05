package waffle.guam.service

import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.CannedAccessControlList
import com.amazonaws.services.s3.model.PutObjectRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import waffle.guam.db.entity.ImageEntity
import waffle.guam.db.entity.ImageType
import waffle.guam.db.repository.ImageRepository
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

interface ImageService {
    fun upload(multipartFile: MultipartFile, imageInfo: ImageInfo): ImageEntity
}

@Service
class ImageServiceImpl(
    private val imageRepository: ImageRepository,
    private val client: AmazonS3Client
) : ImageService {
    private val imageLocation = Paths.get("image")
    private val bucketName = "guam"

    init {
        ImageType.values().forEach { Files.createDirectories(imageLocation.resolve(it.name)) }
    }

    @Transactional
    override fun upload(multipartFile: MultipartFile, imageInfo: ImageInfo): ImageEntity =
        imageRepository.save(ImageEntity(type = imageInfo.type, parentId = imageInfo.parentId)).also { savedImage ->
            internalUpLoad(multipartFile, savedImage).let { req ->
                client.putObject(req.withCannedAcl(CannedAccessControlList.PublicRead))
                req.file.delete()
            }
        }

    private fun internalUpLoad(multipartFile: MultipartFile, imageEntity: ImageEntity): PutObjectRequest =
        imageLocation.resolve(imageEntity.path).let { filePath ->
            multipartFile.inputStream.use { inputStream ->
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING)
            }
            PutObjectRequest(bucketName, imageEntity.path, filePath.toFile())
        }
}

data class ImageInfo(
    val parentId: Long,
    val type: ImageType,
)
