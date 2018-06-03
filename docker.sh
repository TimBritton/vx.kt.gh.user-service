#!/bin/bash
./gradlew clean build shadowjar
docker build -t vx.kt.gh.user-service .