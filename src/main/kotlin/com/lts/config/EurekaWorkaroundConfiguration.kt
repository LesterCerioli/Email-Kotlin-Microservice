// This is a workaround for
// https://github.com/jhipster/jhipster-registry/issues/537
// https://github.com/jhipster/generator-jhipster/issues/18533
// The original issue will be fixed with spring cloud 2021.0.4
// https://github.com/spring-cloud/spring-cloud-netflix/issues/3941
package com.lts.config

import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class EurekaWorkaroundConfiguration: HealthIndicator {

    companion object {
        private var applicationIsUp = false
    }
    
    @EventListener(ApplicationReadyEvent::class)
    fun onStartup() {
        applicationIsUp = true
    }

    override fun health(): Health {
        if (!applicationIsUp) {
            return Health.down().build()
        }
        return Health.up().build()
    }
}
