package com.lts.config

import org.testcontainers.containers.JdbcDatabaseContainer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.testcontainers.containers.MSSQLServerContainer
import org.testcontainers.containers.output.Slf4jLogConsumer

import java.util.Collections

class MsSqlTestContainer: SqlTestContainer {

    private val log = LoggerFactory.getLogger(javaClass)

    private var mSSQLServerContainer: MSSQLServerContainer<*>? = null

    override fun destroy() {
        if (null != mSSQLServerContainer && mSSQLServerContainer?.isRunning == true) {
            mSSQLServerContainer?.stop()
        }
    }

    override fun afterPropertiesSet() {
        if (null == mSSQLServerContainer) {
            mSSQLServerContainer = MSSQLServerContainer("mcr.microsoft.com/mssql/server:2019-CU16-GDR1-ubuntu-20.04")
                .withTmpFs(Collections.singletonMap("/testtmpfs", "rw"))
                .withLogConsumer(Slf4jLogConsumer(log))
                .withReuse(true)
        }
        if (mSSQLServerContainer?.isRunning != true) {
            mSSQLServerContainer?.start()
        }
    }

    override fun getTestContainer() = mSSQLServerContainer as JdbcDatabaseContainer<*>
}
