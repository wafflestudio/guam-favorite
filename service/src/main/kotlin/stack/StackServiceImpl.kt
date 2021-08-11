package waffle.guam.stack

import org.springframework.stereotype.Service
import waffle.guam.DataNotFoundException
import waffle.guam.image.ImageEntity
import waffle.guam.image.ImageRepository
import waffle.guam.image.model.ImageType
import waffle.guam.stack.command.CreateStack
import waffle.guam.stack.command.UpdateStack
import waffle.guam.stack.event.StackCreated
import waffle.guam.stack.event.StackDeleted
import waffle.guam.stack.event.StackUpdated
import waffle.guam.stack.model.TechStack

@Service
class StackServiceImpl(
    private val stackRepository: StackRepository,
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

        val newStack = stackRepository.save(command.toEntity())

        return StackCreated(
            stackId = newStack.id,
            stackName = newStack.name,
            imageFiles = command.imageFiles
        )
    }

    override fun updateProject(command: UpdateStack, stackId: Long): StackUpdated {

        val stack = stackRepository.findById(stackId).orElseThrow(::DataNotFoundException)

        command.position?.run { stack.position = name }
        command.aliases?.run { stack.aliases = this }

        return StackUpdated(
            stackId = stackId,
            stackName = stack.name,
            imageFiles = command.imageFiles
        )
    }

    override fun deleteStack(stackId: Long): StackDeleted {

        stackRepository.deleteById(stackId)
        return StackDeleted(
            stackId = stackId
        )
    }

    // @PostConstruct
    fun init() {
        if (stackRepository.findAll().isEmpty()) {
            val stream = this.javaClass.getResourceAsStream("/stacks.csv")
            val reader = java.io.InputStreamReader(stream!!)
            val names = mutableMapOf<Long, String>()
            reader.forEachLine {
                val idx = it.split(";")
                stackRepository.save(
                    StackEntity(
                        name = idx[0],
                        aliases = (idx[1].drop(1)).dropLast(1),
                        position = idx[2]
                    )
                ).also {
                    entity ->
                    imageRepository.save(
                        ImageEntity(type = ImageType.STACK.name, parentId = entity.id)
                    )
                }
            }
        }
    }
}
