package org.entur.ror.ashur

import org.apache.camel.spring.boot.CamelSpringBootApplicationController
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
open class AshurApplication

fun main(args: Array<String>) {
    val ctx = runApplication<AshurApplication>(*args)
    ctx.getBean(CamelSpringBootApplicationController::class.java).run()
}
