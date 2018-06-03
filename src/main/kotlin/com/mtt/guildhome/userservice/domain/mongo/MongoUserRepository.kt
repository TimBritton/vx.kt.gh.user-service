package com.mtt.guildhome.userservice.domain.mongo

import com.mtt.guildhome.userservice.domain.UserProfile
import com.mtt.guildhome.userservice.domain.UserProfileRepository
import com.mtt.guildhome.userservice.domain.createNewUser
import com.mtt.guildhome.userservice.domain.parseUserProfileFromJson
import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.mongo.MongoClient
import io.vertx.kotlin.core.json.JsonObject

class MongoUserRepository(vertx: Vertx, config: JsonObject): UserProfileRepository {
    val mongoClient: MongoClient

    init {
        mongoClient = MongoClient.createShared(vertx, config)
    }

    override fun create(username: String, handle: String, email: String, guildIds: List<String>, handler: Handler<AsyncResult<UserProfile>>) {
        val userProfile = createNewUser(username, handle, email, guildIds)
        mongoClient.insert("user", userProfile.toJson(), Handler { event ->  handleUnit(event, handler, userProfile)})
    }

    override fun readByUserName(userName: String, handler: Handler<AsyncResult<UserProfile>>) {
        mongoClient.findOne("user", JsonObject("username" to userName), null, Handler {response ->
            if (response.succeeded())
            {
                val userProfile = parseUserProfileFromJson(response.result())
                handler.handle(Future.succeededFuture(userProfile))
            }
            else
            {
                handler.handle(Future.failedFuture(response.cause()))
            }
        })
    }

    override fun readByUserProfileId(userProfileId: String, handler: Handler<AsyncResult<UserProfile>>) {
        mongoClient.findOne("user", JsonObject("id" to userProfileId), null, Handler {response ->
            if (response.succeeded())
            {
                val userProfile = parseUserProfileFromJson(response.result())
                handler.handle(Future.succeededFuture(userProfile))
            }
            else
            {
                handler.handle(Future.failedFuture(response.cause()))
            }
        })

    }

    override fun deleteUserByUserName(userName: String, handler: Handler<AsyncResult<Unit>>) {
        mongoClient.findOneAndDelete("user",  JsonObject("username" to userName),  Handler {it ->
            if(it.succeeded())
            {
                handler.handle(Future.succeededFuture())
            }
            else
            {
                handler.handle(Future.failedFuture(it.cause()))
            }
        })
    }

    override fun deleteUserByUserId(userId: String, handler: Handler<AsyncResult<Unit>>) {

        mongoClient.findOneAndDelete("user",  JsonObject("userId" to userId), Handler {it ->
            if(it.succeeded())
            {
                handler.handle(Future.succeededFuture())
            }
            else
            {
                handler.handle(Future.failedFuture(it.cause()))
            }
        })
    }

    override fun updateOrInsert(userProfile: UserProfile, handler: Handler<AsyncResult<UserProfile>>) {
        mongoClient.findOneAndUpdate("user", JsonObject("id" to userProfile.id), null, Handler {response ->
            if (response.succeeded())
            {
                val userProfile = parseUserProfileFromJson(response.result())
                handler.handle(Future.succeededFuture(userProfile))
            }
            else
            {
                handler.handle(Future.failedFuture(response.cause()))
            }
        })
    }
    //TODO: Rename
    private fun handleUnit(asyncResult: AsyncResult<String>, handler: Handler<AsyncResult<UserProfile>>, user: UserProfile){
        if(asyncResult.succeeded())
        {
            handler.handle(Future.succeededFuture(user))
        }
        else {
            handler.handle(Future.failedFuture(asyncResult.cause()))
        }
    }

}