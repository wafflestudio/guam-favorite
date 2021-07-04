package waffle.guam.service

import org.springframework.stereotype.Service
import waffle.guam.db.entity.Position
import waffle.guam.db.entity.TechStackEntity
import waffle.guam.db.repository.StackRepository
import waffle.guam.model.TechStack
import javax.annotation.PostConstruct
import javax.persistence.EntityNotFoundException

@Service
class StackService(
    private val stackRepository: StackRepository
) {

    private val searchEngine: SearchEngine = SearchEngine()

    @PostConstruct
    fun init() {
        val stream = this.javaClass.getResourceAsStream("/stacks.csv")
        val reader = java.io.InputStreamReader(stream)
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

    fun create(o: TechStack): Boolean {
        stackRepository.save(o.toEntity())
        return true
    }

    fun getAll(): List<TechStack> {
        val target = stackRepository.findAll()
        return target.map { TechStack.of(it) }
    }

    /*
     # 여러가지 키워드로 검색했을 경우: 검색어마다 OR 처리 -> AND 위주로 보여줌
     # case Insensitive
    */
    fun searchByKeyword(query: String): List<TechStack> {
        val map = mutableMapOf<TechStack, Int>()

        val devTypes = getAll()
        for (dev in devTypes) {
            val mappings = dev.aliases.split(", ")
            val cnt = searchEngine.search(mappings, query)
            if (cnt > 0) map[dev] = cnt
        }
        return map.toList().sortedWith(compareBy { -it.second }).map { it.first }
    }

    // fixme : throw an error? or create one?
    fun searchIdByDTO(o: TechStack): Long {
        val target = stackRepository.findAll().filter { TechStack.of(it) == o }
        return if (target.isNotEmpty()) target[0].id else {
            // make new entity
            // val res = o.toEntity()
            // stackRepository.save(res)
            // res.id
            throw EntityNotFoundException()
        }
    }

    fun searchById(id: Long): TechStackEntity {
        val res = stackRepository.findById(id)
        return res.get()
    }
}
