package com.lts.config

import java.util.*

import java.util.Arrays
import tech.jhipster.config.JHipsterConstants

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.support.DefaultSingletonBeanRegistry
import org.springframework.core.annotation.AnnotatedElementUtils

import org.springframework.test.context.ContextConfigurationAttributes
import org.springframework.test.context.ContextCustomizer
import org.springframework.test.context.ContextCustomizerFactory
import org.springframework.beans.factory.support.DefaultListableBeanFactory
import org.springframework.boot.test.util.TestPropertyValues
import org.testcontainers.containers.KafkaContainer

class TestContainersSpringContextCustomizerFactory: ContextCustomizerFactory {

    private val log = LoggerFactory.getLogger(TestContainersSpringContextCustomizerFactory::class.java)

    companion object {
        private var kafkaBean: KafkaTestContainer? = null
        private var devTestContainer: SqlTestContainer? = null
        private var prodTestContainer: SqlTestContainer? = null
    }    

    override fun createContextCustomizer(
        testClass: Class<*>,
        configAttributes: MutableList<ContextConfigurationAttributes>
    ): ContextCustomizer {
        return ContextCustomizer { context, _ ->
            val beanFactory = context.beanFactory
            var testValues = TestPropertyValues.empty()
            val sqlAnnotation = AnnotatedElementUtils.findMergedAnnotation(testClass, EmbeddedSQL::class.java)
            if (null != sqlAnnotation) {
                log.debug("detected the EmbeddedSQL annotation on class {}", testClass.name)
                log.info("Warming up the sql database")
                if (context.environment.activeProfiles.contains("test${JHipsterConstants.SPRING_PROFILE_DEVELOPMENT}")) {
                    if (null == devTestContainer) {
                        try {
                            val containerClass =  Class.forName("${javaClass.packageName}.MsSqlTestContainer") as (Class<out SqlTestContainer>)
                            devTestContainer = beanFactory.createBean(containerClass)
                            beanFactory.registerSingleton(containerClass.name, devTestContainer)
                            // (beanFactory as DefaultListableBeanFactory).registerDisposableBean(containerClass.name, devTestContainer)
                        } catch (e: ClassNotFoundException) {
                            throw RuntimeException(e)
                        }
                    }
                    devTestContainer?.let {
                        testValues = testValues.and("spring.r2dbc.url=" + it.getTestContainer().jdbcUrl.replace("jdbc", "r2dbc").replace("encrypt=false", "") + "")
                        testValues = testValues.and("spring.r2dbc.username=" + it.getTestContainer().username)
                        testValues = testValues.and("spring.r2dbc.password=" + it.getTestContainer().password)
                        testValues = testValues.and("spring.liquibase.url=" + it.getTestContainer().jdbcUrl + "" )
                    }
                }
                if (context.environment.activeProfiles.asList().contains("test${JHipsterConstants.SPRING_PROFILE_PRODUCTION}")) {
                    if (null == prodTestContainer) {
                        try {
                            val containerClass = Class.forName("${javaClass.packageName}.PostgreSqlTestContainer") as Class<out SqlTestContainer>
                            prodTestContainer = beanFactory.createBean(containerClass)
                            beanFactory.registerSingleton(containerClass.name, prodTestContainer)
                            // (beanFactory as (DefaultListableBeanFactory)).registerDisposableBean(containerClass.name, prodTestContainer)
                        } catch (e: ClassNotFoundException) {
                            throw RuntimeException(e)
                        }
                    }
                    prodTestContainer?.let {
                        testValues = testValues.and("spring.r2dbc.url=" + it.getTestContainer().jdbcUrl.replace("jdbc", "r2dbc").replace("encrypt=false", "") + "")
                        testValues = testValues.and("spring.r2dbc.username=" + it.getTestContainer().username)
                        testValues = testValues.and("spring.r2dbc.password=" + it.getTestContainer().password)
                        testValues = testValues.and("spring.liquibase.url=" + it.getTestContainer().jdbcUrl  + "")
                    }
                }
            }

    val kafkaAnnotation = AnnotatedElementUtils.findMergedAnnotation(testClass, EmbeddedKafka::class.java)
    if (null != kafkaAnnotation) {
        log.debug("detected the EmbeddedKafka annotation on class {}", testClass.name)
        log.info("Warming up the kafka broker")
        if (null == kafkaBean) {
            kafkaBean = beanFactory.createBean(KafkaTestContainer::class.java)
            beanFactory.registerSingleton(KafkaTestContainer::class.java.name, kafkaBean)
            // (beanFactory as (DefaultListableBeanFactory)).registerDisposableBean(KafkaTestContainer::class.java.name, kafkaBean)
        }
        kafkaBean?.let {
            testValues = testValues.and("spring.cloud.stream.kafka.binder.brokers=" + it.getKafkaContainer().host + ':' + it.getKafkaContainer().getMappedPort(KafkaContainer.KAFKA_PORT))
        }   
    }
            testValues.applyTo(context)
        }
    }

}