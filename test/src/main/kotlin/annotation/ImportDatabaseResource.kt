package waffle.guam.annotation

import org.springframework.context.annotation.Import
import waffle.guam.config.ExtraDatabaseSourceRegistrar

@Import(value = [ExtraDatabaseSourceRegistrar::class])
annotation class ImportDatabaseResource(val databaseResources: Array<String> = [])
