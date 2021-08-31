package waffle.guam.stack

import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service
import waffle.guam.stack.command.CreateStack
import waffle.guam.stack.command.UpdateStack
import waffle.guam.stack.event.StackCreated
import waffle.guam.stack.event.StackDeleted
import waffle.guam.stack.event.StackUpdated
import waffle.guam.stack.model.TechStack
import waffle.guam.util.GuamCache
import java.time.Duration

@Primary
@Service
class StackServiceCacheImpl(
    private val impl: StackServiceImpl,
) : StackService {
    companion object {
        private const val STACKS_CACHE_KEY = "STACKS_CACHE_KEY"
    }

    val stackListCache = GuamCache<String, List<TechStack>>(
        maximumSize = 1,
        duration = Duration.ofMinutes(10),
        loader = { impl.getAllStacks() }
    )

    override fun getStack(stackId: Long): TechStack =
        stackListCache.get(STACKS_CACHE_KEY).find { it.id == stackId } ?: throw RuntimeException("")

    override fun getAllStacks(): List<TechStack> =
        stackListCache.get(STACKS_CACHE_KEY)

    override fun createStack(command: CreateStack, stackId: Long): StackCreated = impl.createStack(command, stackId)

    override fun updateProject(command: UpdateStack, stackId: Long): StackUpdated = impl.updateProject(command, stackId)

    override fun deleteStack(stackId: Long): StackDeleted = impl.deleteStack(stackId)
}
