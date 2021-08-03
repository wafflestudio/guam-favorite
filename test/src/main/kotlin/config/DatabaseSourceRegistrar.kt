package waffle.guam.config

import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.RootBeanDefinition
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar
import org.springframework.core.type.AnnotationMetadata
import org.springframework.util.MultiValueMap
import waffle.guam.annotation.DatabaseTest

class DatabaseSourceRegistrar : ImportBeanDefinitionRegistrar {
    override fun registerBeanDefinitions(importingClassMetadata: AnnotationMetadata, registry: BeanDefinitionRegistry) {
        val attributes: MultiValueMap<String, Any>? = importingClassMetadata
            .getAllAnnotationAttributes(DatabaseTest::class.java.name, false)

        val sqls = attributes!!["databaseResources"] as List<Array<String>>

        registry.registerBeanDefinition(
            "databaseSourceInfo",
            RootBeanDefinition(DatabaseSourceInfo::class.java) { DatabaseSourceInfo(sqls.first().toList()) }
        )
    }
}
