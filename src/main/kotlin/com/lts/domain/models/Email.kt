package com.lts.domain.models

import java.time.Instant
import org.apache.commons.logging.Log
import javax.persistence.Column
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.Entity
import javax.persistence.Table

@Enyiyu
@Table(name = "email")
class Email (
    @Column(name = "sender", nullable = false, length = 100)
    var sender: String,

    @Column(name = "recipient", nullable = false, length = 100)
    var recipient: String,

    @Column(name = "subject", nullable = false, length = 50)
    var subject: String,

    @Column(name = "message", nullable = false, columnbDefinition = "TEXT")
    var message: String
) : AbstractAuditingEntity<Log>() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    override var id: Long? = null
}


