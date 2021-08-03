package waffle.guam.config

import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.RootBeanDefinition
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar
import org.springframework.core.type.AnnotationMetadata
import org.springframework.util.MultiValueMap
import waffle.guam.annotation.ImportDatabaseResource

class ExtraDatabaseSourceRegistrar : ImportBeanDefinitionRegistrar {
    override fun registerBeanDefinitions(importingClassMetadata: AnnotationMetadata, registry: BeanDefinitionRegistry) {
        val attributes: MultiValueMap<String, Any>? = importingClassMetadata
            .getAllAnnotationAttributes(ImportDatabaseResource::class.java.getName(), false)
        val sqls = attributes!!["databaseResources"] as List<Array<String>>

        registry.registerBeanDefinition(
            "extraDatabaseSourceInfo",
            RootBeanDefinition(DatabaseSourceInfo::class.java) { DatabaseSourceInfo(sqls.first().toList()) }
        )
    }
}
