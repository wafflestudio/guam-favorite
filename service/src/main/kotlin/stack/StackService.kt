package waffle.guam.stack

import waffle.guam.stack.command.CreateStack
import waffle.guam.stack.command.UpdateStack
import waffle.guam.stack.event.StackCreated
import waffle.guam.stack.event.StackDeleted
import waffle.guam.stack.event.StackUpdated
import waffle.guam.stack.model.TechStack

interface StackService {

    /**
     * Stack에서 CUD의 경우에는 admin이 관리해야 하는 도메인으로 보입니다.
     * 인증 절차를 붙이던지 해야 할 것 같습니다.
     */

    // READ
    fun getStack(stackId: Long): TechStack
    fun getAllStacks(): List<TechStack>

    // CREATE
    fun createStack(command: CreateStack, stackId: Long): StackCreated

    // UPDATE
    fun updateProject(command: UpdateStack, stackId: Long): StackUpdated

    // DELETE
    fun deleteStack(stackId: Long): StackDeleted
}
