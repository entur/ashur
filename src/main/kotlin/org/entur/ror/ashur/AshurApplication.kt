package org.entur.ror.ashur

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class AshurApplication

fun main(args: Array<String>) {
    runApplication<AshurApplication>(*args)
}
