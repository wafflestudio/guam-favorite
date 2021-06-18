package waffle.guam.service

import org.springframework.stereotype.Service
import waffle.guam.db.DevType.TechStack
import waffle.guam.db.DevType.TechStackDTO
import waffle.guam.repository.StackRepository
import javax.persistence.EntityNotFoundException

@Service
class StackService(
    private val stackRepository: StackRepository
) {

    private val searchEngine: SearchEngine = SearchEngine()

    fun init() {
        val stream = this.javaClass.getResourceAsStream("/stacks.csv")
        val reader = java.io.InputStreamReader(stream)
        reader.forEachLine {
            val idx = it.indexOf(",")
            stackRepository.save( (TechStackDTO( it.dropLast( it.length - (idx) ), (it.drop(idx+2)).dropLast(1), "")).toEntity() )
        }
    }

    fun create(o: TechStackDTO): Boolean{
        stackRepository.save(o.toEntity())
        return true
    }

    fun getAll(): List<TechStackDTO> {
        val target = stackRepository.findAll()
        return target.map { TechStackDTO.of(it) }
    }

    /*
     # 여러가지 키워드로 검색했을 경우: 검색어마다 OR 처리 -> AND 위주로 보여줌
     # case Insensitive
    */
    fun searchByKeyword(query: String): List<TechStackDTO> {
        val map = mutableMapOf<TechStackDTO, Int>()

        val devTypes = getAll()
        for (dev in devTypes) {
            val mappings = dev.aliases.split(", ")
            val cnt = searchEngine.search(mappings, query)
            if ( cnt > 0 ) map[dev] = cnt
        }
        return map.toList().sortedWith(compareBy { -it.second }).map {it.first}
    }

    // fixme : throw an error? or create one?
    fun searchIdByDTO(o: TechStackDTO): Long {
        val target = stackRepository.findAll().filter { TechStackDTO.of(it) == o }
        return if (target.isNotEmpty()) target[0].id else {
            //make new entity
            //val res = o.toEntity()
            //stackRepository.save(res)
            //res.id
            throw EntityNotFoundException()
        }
    }

    fun searchById(id: Long): TechStack {
        val res = stackRepository.findById(id)
        return res.get()
    }


}