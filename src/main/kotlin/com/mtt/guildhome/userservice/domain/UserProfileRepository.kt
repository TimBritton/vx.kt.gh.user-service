package com.mtt.guildhome.userservice.domain

import io.vertx.core.AsyncResult
import io.vertx.core.Handler

interface UserProfileRepository{

    fun create(username: String, handle: String, email: String, guildIds: List<String>, handler: Handler<AsyncResult<UserProfile>>)
    fun readByUserName(userName: String, handler: Handler<AsyncResult<UserProfile>>)
    fun readByUserProfileId(userProfileId: String, handler: Handler<AsyncResult<UserProfile>>)
    fun deleteUserByUserName(userName: String, handler: Handler<AsyncResult<Unit>>)
    fun deleteUserByUserId(userId: String, handler: Handler<AsyncResult<Unit>>)
    fun updateOrInsert(userProfile: UserProfile, handler: Handler<AsyncResult<UserProfile>>)
}