package waffle.guam.image

import com.amazonaws.services.s3.AmazonS3Client
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import waffle.guam.image.command.CreateImages
import waffle.guam.image.command.DeleteImages
import waffle.guam.image.event.ImagesCreated
import waffle.guam.image.event.ImagesDeleted
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

@Service
class ImageServiceImpl(
    private val imageRepository: ImageRepository,
    private val client: AmazonS3Client,
) : ImageService {
    companion object {
        private const val BUCKET_NAME = "guam"
        private val imageLocation = Paths.get("image")
    }

    @Transactional
    override fun createImages(command: CreateImages): ImagesCreated = command.run {
        val images = imageRepository.saveAll(files.indices.map { ImageEntity(type = type.name, parentId = parentId) })

        // FIXME: 테스트를 위해 주석처리
//        images.forEachIndexed { i, image ->
//            client.putObject(PutObjectRequest(BUCKET_NAME, image.getPath(), files[i].getFile()))
//        }

        ImagesCreated(
            imageIds = images.map { it.id },
            imageType = type,
            parentId = parentId
        )
    }

    @Transactional
    override fun deleteImages(command: DeleteImages): ImagesDeleted =
        when (command) {
            is DeleteImages.ById -> {
                imageRepository.findAllById(command.imageIds)
            }
            is DeleteImages.ByParentId -> {
                imageRepository.findByParentIdAndType(command.parentId, command.imageType.name)
            }
        }.let {
            imageRepository.deleteAllInBatch(it)
            ImagesDeleted(imageIds = it.map { it.id })
        }

    private fun ImageEntity.getPath() = "$type/$id"

    private fun MultipartFile.getFile(): File = inputStream.use { inputStream ->
        imageLocation.resolve(originalFilename).let {
            Files.copy(inputStream, it, StandardCopyOption.REPLACE_EXISTING)
            it.toFile()
        }
    }
}
