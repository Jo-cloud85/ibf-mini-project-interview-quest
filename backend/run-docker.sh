#!/bin/bash

# Load environment variables from .env file
if [ -f .env ]; then
  export $(grep -v '^#' .env | xargs)
fi

# Run the Docker container
docker run -d \
  -e GOOGLE_CLIENT_ID=${GOOGLE_CLIENT_ID} \
  -e GOOGLE_CLIENT_SECRET=${GOOGLE_CLIENT_SECRET} \
  -e GOOGLE_REDIRECT_URI=${GOOGLE_REDIRECT_URI} \
  -e GOOGLE_CALENDAR_API_KEY=${GOOGLE_CALENDAR_API_KEY} \
  -e S3_SECRET_KEY=${S3_SECRET_KEY} \
  -e S3_ACCESS_KEY=${S3_ACCESS_KEY} \
  -e MYSQL_URL=${MYSQL_URL} \
  -e MYSQLUSER=${MYSQLUSER} \
  -e MYSQLPASSWORD=${MYSQLPASSWORD} \
  -e OPENAI_API_KEY=${OPENAI_API_KEY} \
  -e SPRING_MAIL_USERNAME=${SPRING_MAIL_USERNAME} \
  -e SPRING_MAIL_PASSWORD=${SPRING_MAIL_PASSWORD} \
  -e PORT=${PORT} \
  -p ${PORT}:${PORT} \
  joyoung/interviewapp:v1.1

# When you want to run your docker container WITH Railway
# docker run -d -p 5050:8080 \
#   -e MYSQL_URL='jdbc:mysql://root:SUVbNnslexhTNaKQwCnPTQUFaWGSvIOG@roundhouse.proxy.rlwy.net:13186/appliedJobs' \
#   -e MYSQLUSER=root \
#   -e MYSQLPASSWORD='SUVbNnslexhTNaKQwCnPTQUFaWGSvIOG' \
#   -e MONGO_URL='mongodb://mongo:mfgnxaCMqHViqSJCVoXvzHimBHboltYp@monorail.proxy.rlwy.net:29750/devjobs?authSource=admin' \
#   joyoung/devapp:v1.1

# When you want to run your docker container WITH local
# docker run -d -p 5050:8080 \
#   -e MYSQL_URL=jdbc:mysql://localhost:3306/appliedJobs \
#   -e MYSQLUSER=abcde \
#   -e MYSQLPASSWORD=abcde \
#   -e MONGO_URL=mongodb://localhost:27017 \
#   joyoung/devapp:v1.1
