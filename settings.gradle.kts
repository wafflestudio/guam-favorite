rootProject.name = "favorite"

include(
    "app:model",
    "app:api",
    "app:batch",
    "service",
)

include("client")
project(":client").projectDir = file("app/client")

include("data")
include(":data-r2dbc")
project(":data-r2dbc").projectDir = file("data/r2dbc")
include(":data-redis")
project(":data-redis").projectDir = file("data/redis")
