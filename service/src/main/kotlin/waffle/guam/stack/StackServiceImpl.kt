package waffle.guam.stack

import org.springframework.stereotype.Service
import waffle.guam.db.entity.ImageType
import waffle.guam.db.entity.Position
import waffle.guam.db.entity.TechStackEntity
import waffle.guam.db.repository.ImageRepository
import waffle.guam.db.repository.StackRepository
import waffle.guam.exception.DataNotFoundException
import waffle.guam.service.ImageInfo
import waffle.guam.service.ImageService
import waffle.guam.stack.command.CreateStack
import waffle.guam.stack.command.UpdateStack
import waffle.guam.stack.event.StackCreated
import waffle.guam.stack.event.StackDeleted
import waffle.guam.stack.event.StackUpdated
import waffle.guam.stack.model.TechStack

@Service
class StackServiceImpl(
    private val stackRepository: StackRepository,
    private val imageService: ImageService,
    private val imageRepository: ImageRepository
) : StackService {

    override fun getStack(stackId: Long): TechStack =
        stackRepository.findById(stackId).orElseThrow(::DataNotFoundException).let {
            TechStack.of(it)
        }

    override fun getAllStacks(stackIds: List<Long>): List<TechStack> =
        stackRepository.findAllById(stackIds).map {
            TechStack.of(it)
        }

    override fun createStack(command: CreateStack, stackId: Long): StackCreated {

        val newStack = command.toEntity().also {
            command.imageFiles?.let {
                img ->
                it.thumbnail = imageService.upload(img, ImageInfo(it.id, ImageType.STACK))
            }
        }

        return StackCreated(
            stackId = newStack.id,
            stackName = newStack.name
        )
    }

    override fun updateProject(command: UpdateStack, stackId: Long): StackUpdated {

        val stack = stackRepository.findById(stackId).orElseThrow(::DataNotFoundException)

        stack.position = command.position ?: stack.position
        stack.aliases = command.aliases ?: stack.aliases
        // TODO : 이런 류의 업데이트는 이미지서비스와 컴캐 필요해 보인다.
        command.imageFiles?.let {
            img ->
            imageRepository.deleteByParentIdAndType(stack.id, ImageType.STACK)
            stack.thumbnail = imageService.upload(img, ImageInfo(stack.id, ImageType.STACK))
        }

        return StackUpdated(
            stackId = stackId,
            stackName = stack.name
        )
    }

    override fun deleteStack(stackId: Long): StackDeleted {

        imageRepository.deleteByParentIdAndType(stackId, ImageType.STACK)
        return StackDeleted(
            stackId = stackId
        )
    }

    // @PostConstruct
    fun init() {
        val stream = this.javaClass.getResourceAsStream("/stacks.csv")
        val reader = java.io.InputStreamReader(stream!!)
        reader.forEachLine {
            val idx = it.split(";")
            stackRepository.save(
                TechStackEntity(
                    name = idx[0],
                    aliases = (idx[1].drop(1)).dropLast(1),
                    position = Position.valueOf(idx[2])
                )
            )
        }
    }
}
