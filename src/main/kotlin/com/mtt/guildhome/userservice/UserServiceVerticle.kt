package com.mtt.guildhome.userservice

import com.mtt.guildhome.userservice.domain.UserProfile
import com.mtt.guildhome.userservice.domain.mongo.MongoUserRepository
import com.sun.net.httpserver.HttpServer
import io.netty.handler.codec.http.HttpHeaderValues
import io.vertx.core.AbstractVerticle
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.kotlin.core.json.JsonObject
import io.vertx.kotlin.core.json.get

class UserServiceVerticle : AbstractVerticle() {

    override fun start(startFuture: Future<Void>?) {
        //the user service crud operation service

        val server = vertx.createHttpServer()
        val mongoClient = MongoClient.createShared(vertx, createMongoConfig())
        val userProfileRepository = MongoUserRepository(vertx, createMongoConfig())
        val router = Router.router(vertx)

        //retrieve a singular user.
        //uses ?userId= or ?username=
        router.get("/v1/user").produces(HttpHeaderValues.APPLICATION_JSON.toString()).handler({ event ->
            val userId: String? = if (event.queryParam("userId").size > 0) event.queryParam("userId").first() else null

            if (userId != null) {
                userProfileRepository.readByUserProfileId(userId, Handler { result: AsyncResult<UserProfile> ->
                    if (result.succeeded()) {
                        event.response().setStatusCode(200).end(result.result().toJson().toBuffer())
                    } else {
                        event.response().setStatusCode(500).end(JsonObject("exception" to result.cause()).toBuffer())
                    }
                })
            } else {
                val username: String? = if (event.queryParam("username").size > 0) event.queryParam("username").first() else null

                if (username != null) {
                    userProfileRepository.readByUserProfileId(username, Handler { result: AsyncResult<UserProfile> ->
                        if (result.succeeded()) {
                            event.response().end(result.result().toJson().toBuffer())
                        } else {
                            event.response().end(JsonObject("exception" to result.cause()).toBuffer())
                        }
                    })
                } else {
                    event.response().setStatusCode(500).end(JsonObject("exception" to "Nothing Found").toBuffer())

                }
            }


        })
        router.post().handler(BodyHandler.create())
        router.post("/v1/user").consumes(HttpHeaderValues.APPLICATION_JSON.toString()).produces(HttpHeaderValues.APPLICATION_JSON.toString()).handler({ event ->
            val jsonObject = event.bodyAsJson
            userProfileRepository.create(username = jsonObject["username"],
                                         handle = jsonObject["handle"],
                                         email = jsonObject["email"],
                                         guildIds = jsonObject.getJsonArray("guildIds").toList() as List<String>,
                    handler= Handler{ response ->
                        if(response.succeeded())
                        {
                            event.response().setStatusCode(200).end()
                        }
                        else
                        {
                            event.response().setStatusCode(500).end(response.cause().message)
                        }
                    })

        })


        server.requestHandler({ router.accept(it) }).listen(8080)
        super.start(startFuture)
    }

    private fun createMongoConfig(): JsonObject = JsonObject("{\n" +
//        "  // Single Cluster Settings\n" +
            "  \"host\" : \"192.168.72.107\"," +
            "  \"port\" : 27017,\n" +
            " \"db_name\":\"guildhome\"" +
//        "\n" +
//        "  // Multiple Cluster Settings\n" +
//        "  \"hosts\" : [\n" +
//        "    {\n" +
//        "      \"host\" : \"cluster1\", // string\n" +
//        "      \"port\" : 27000       // int\n" +
//        "    },\n" +
//        "    {\n" +
//        "      \"host\" : \"cluster2\", // string\n" +
//        "      \"port\" : 28000       // int\n" +
//        "    },\n" +
//        "    ...\n" +
//        "  ],\n" +
//        "  \"replicaSet\" :  \"foo\",    // string\n" +
//        "  \"serverSelectionTimeoutMS\" : 30000, // long\n" +
//        "\n" +
//        "  // Connection Pool Settings\n" +
//        "  \"maxPoolSize\" : 50,                // int\n" +
//        "  \"minPoolSize\" : 25,                // int\n" +
//        "  \"maxIdleTimeMS\" : 300000,          // long\n" +
//        "  \"maxLifeTimeMS\" : 3600000,         // long\n" +
//        "  \"waitQueueMultiple\"  : 10,         // int\n" +
//        "  \"waitQueueTimeoutMS\" : 10000,      // long\n" +
//        "  \"maintenanceFrequencyMS\" : 2000,   // long\n" +
//        "  \"maintenanceInitialDelayMS\" : 500, // long\n" +
//        "\n" +
//        "  // Credentials / Auth\n" +
//        "  \"username\"   : \"john\",     // string\n" +
//        "  \"password\"   : \"passw0rd\", // string\n" +
//        "  \"authSource\" : \"some.db\"   // string\n" +
//        "  // Auth mechanism\n" +
//        "  \"authMechanism\"     : \"GSSAPI\",        // string\n" +
//        "  \"gssapiServiceName\" : \"myservicename\", // string\n" +
//        "\n" +
//        "  // Socket Settings\n" +
//        "  \"connectTimeoutMS\" : 300000, // int\n" +
//        "  \"socketTimeoutMS\"  : 100000, // int\n" +
//        "  \"sendBufferSize\"    : 8192,  // int\n" +
//        "  \"receiveBufferSize\" : 8192,  // int\n" +
//        "  \"keepAlive\" : true           // boolean\n" +
//        "\n" +
//        "  // Heartbeat socket settings\n" +
//        "  \"heartbeat.socket\" : {\n" +
//        "  \"connectTimeoutMS\" : 300000, // int\n" +
//        "  \"socketTimeoutMS\"  : 100000, // int\n" +
//        "  \"sendBufferSize\"    : 8192,  // int\n" +
//        "  \"receiveBufferSize\" : 8192,  // int\n" +
//        "  \"keepAlive\" : true           // boolean\n" +
//        "  }\n" +
//        "\n" +
//        "  // Server Settings\n" +
//        "  \"heartbeatFrequencyMS\" :    1000 // long\n" +
//        "  \"minHeartbeatFrequencyMS\" : 500 // long\n" +
            "}")
}