package com.lts

import com.lts.MsemailApp
import com.lts.config.AsyncSyncConfiguration
import com.lts.config.EmbeddedKafka
import com.lts.config.EmbeddedSQL

import org.springframework.test.annotation.DirtiesContext
import org.springframework.boot.test.context.SpringBootTest

/**
 * Base composite annotation for integration tests.
 */
@kotlin.annotation.Target(AnnotationTarget.CLASS)
@kotlin.annotation.Retention(AnnotationRetention.RUNTIME)
@SpringBootTest(classes = [MsemailApp::class, AsyncSyncConfiguration::class])
@EmbeddedKafka
@EmbeddedSQL
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
annotation class IntegrationTest {
      companion object {
        // 5s is the spring default https://github.com/spring-projects/spring-framework/blob/29185a3d28fa5e9c1b4821ffe519ef6f56b51962/spring-test/src/main/java/org/springframework/test/web/reactive/server/DefaultWebTestClient.java#L106
          const val DEFAULT_TIMEOUT: String = "PT5S"
          const val DEFAULT_ENTITY_TIMEOUT: String = "PT5S"
      }
}
