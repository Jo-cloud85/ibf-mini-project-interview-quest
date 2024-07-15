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
  -e FIREBASE_ADMINSDK_BASE64=${FIREBASE_ADMINSDK_BASE64} \
  -e S3_SECRET_KEY=${S3_SECRET_KEY} \
  -e S3_ACCESS_KEY=${S3_ACCESS_KEY} \
  -e OPENAI_API_KEY=${OPENAI_API_KEY} \
  -e SPRING_MAIL_USERNAME=${SPRING_MAIL_USERNAME} \
  -e SPRING_MAIL_PASSWORD=${SPRING_MAIL_PASSWORD} \
  -p 8080:8080 \
  joyoung/interviewapp:v1.1