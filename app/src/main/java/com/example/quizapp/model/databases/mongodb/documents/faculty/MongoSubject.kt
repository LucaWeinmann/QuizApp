package com.example.quizapp.model.databases.mongodb.documents.faculty

import io.ktor.util.date.*
import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

@Serializable
data class MongoSubject(
    @BsonId var id: String = ObjectId().toHexString(),
    var abbreviation: String,
    var name: String,
    var lastModifiedTimestamp : Long = getTimeMillis()
)