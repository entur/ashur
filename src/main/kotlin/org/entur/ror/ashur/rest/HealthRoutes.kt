package org.entur.ror.ashur.rest

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HealthRoutes {
    @GetMapping("/actuator/health/liveness")
    fun liveness(): String {
        return "OK"
    }

    @GetMapping("/actuator/health/readiness")
    fun readiness(): String {
        return "OK"
    }
}