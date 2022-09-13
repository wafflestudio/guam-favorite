rootProject.name = "favorite"

include(
    "app:api",
    "app:batch",
    "service",
    "client",
    "model"
)

project(":client").projectDir = file("app/client")
project(":model").projectDir = file("app/model")

include("data")
include(":data-r2dbc")
project(":data-r2dbc").projectDir = file("data/r2dbc")
include(":data-redis")
project(":data-redis").projectDir = file("data/redis")
