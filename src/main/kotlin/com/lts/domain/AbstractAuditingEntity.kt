package com.lts.domain

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.io.Serializable
import java.time.Instant
import javax.persistence.Column

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate

/**
 * Base abstract class for entities which will hold definitions for created, last modified by, created by,
 * last modified by attributes.
 */
@JsonIgnoreProperties(value = [ "createdBy", "createdDate", "lastModifiedBy", "lastModifiedDate" ], allowGetters = true)
abstract class AbstractAuditingEntity<T>(
    @Column(name = "created_by", nullable = false, length = 50, updatable = false)
    open var createdBy: String? = null,

    @CreatedDate
    @Column(name = "created_date", updatable = false)
    open var createdDate: Instant? = Instant.now(),

    @Column(name = "last_modified_by", length = 50)
    open var lastModifiedBy: String? = null,

    @LastModifiedDate
    @Column(name = "last_modified_date")
    open var lastModifiedDate: Instant? = Instant.now()
) : Serializable {

    abstract val id: T?
    
    companion object {
        private const val serialVersionUID = 1L
    }
}
