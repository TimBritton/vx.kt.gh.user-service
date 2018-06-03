package com.mtt.guildhome.userservice

import com.mtt.guildhome.userservice.domain.UserProfile
import com.mtt.guildhome.userservice.domain.mongo.MongoUserRepository
import com.sun.net.httpserver.HttpServer
import io.netty.handler.codec.http.HttpHeaderValues
import io.vertx.config.ConfigRetriever
import io.vertx.core.AbstractVerticle
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.mongo.MongoClient
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.kotlin.config.ConfigRetrieverOptions
import io.vertx.kotlin.config.ConfigStoreOptions
import io.vertx.kotlin.core.json.JsonObject
import io.vertx.kotlin.core.json.get
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj

class UserServiceVerticle : AbstractVerticle() {
    val log = LoggerFactory.getLogger(UserServiceVerticle::class.java)
    override fun start(startFuture: Future<Void>?) {
        //the user service crud operation service



        var fileStore = ConfigStoreOptions(
                type = "file",
                optional = true,
                config = json {
                    obj("path" to "./config/config.json")
                })


        var envPropsStore = ConfigStoreOptions(
                type = "env")

        var sysPropsStore = ConfigStoreOptions(
                type = "sys")

        var options = ConfigRetrieverOptions(
                stores = listOf(fileStore,envPropsStore, sysPropsStore))

        var retriever = ConfigRetriever.create(vertx, options)

        retriever.getConfig({ it ->

            if(it.succeeded()) {
                val externalConfig = it.result()
                val server = vertx.createHttpServer()
//                val mongoClient = MongoClient.createShared(vertx, createMongoConfig(externalConfig))
                val userProfileRepository = MongoUserRepository(vertx, createMongoConfig(externalConfig))
                val router = Router.router(vertx)

                //retrieve a singular user.
                //uses ?userId= or ?username=
                router.get("/v1/user").produces(HttpHeaderValues.APPLICATION_JSON.toString()).handler({ event ->

                    val userId: String? = if (event.queryParam("userId").size > 0) event.queryParam("userId").first() else null

                    log.info("UserID: $userId")
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
                            userProfileRepository.readByUserName(username, Handler { result: AsyncResult<UserProfile> ->
                                if (result.succeeded()) {
                                    event.response().setStatusCode(200).end(result.result().toJson().toBuffer())
                                } else {
                                    event.response().setStatusCode(500).end(JsonObject("exception" to result.cause()).toBuffer())
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
                            handler = Handler { response ->
                                if (response.succeeded()) {
                                    event.response().setStatusCode(200).end(response.result().toJson().toBuffer())
                                } else {
                                    event.response().setStatusCode(500).end(io.vertx.kotlin.core.json.JsonObject("error" to response.cause().message).toBuffer())
                                }
                            })

                })


                server.requestHandler({ router.accept(it) }).listen(8081)


            }
            else
            {
                log.error("Config Retrieval has failed.")
            }
        })
        super.start(startFuture)
    }

    private fun createMongoConfig(config: JsonObject): JsonObject = JsonObject("{\n" +
//        "  // Single Cluster Settings\n" +
            "  \"host\" : \"${config.getString("MONGO:HOST", "localhost")}\"," +
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