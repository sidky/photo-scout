package com.github.sidky.common.koin

import org.koin.dsl.module.module

val constantsModule = module {
    single("server") { "https://immense-tor-66837.herokuapp.com/graphql" }
}
