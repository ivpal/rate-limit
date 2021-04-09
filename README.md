# RateLimit
This is a simple project that demonstrates rate limit for HTTP requests. It uses Apache Ignite 
for in-memory key-value store. 

## Build
```sh
./gradlew shadowJar
```

## Run
For run via Docker:
```sh
docker-compose up -d
```
Or in local (need Java 11):
```sh
java -jar ./build/libs/rate-limit-0.0.1-all.jar
```

Parameters are used by default is `limit = 100` (via env variable `RL_LIMIT`), `duration = 1` 
(via env variable `RL_DURATION`), `unit = MINUTES` (via env variable `RL_UNIT`). A token must 
be passed in each request (header `X-Auth-Token`), and limits will be count for each distinct token.

Request:
```sh
curl --location --request GET 'http://localhost:8000/route' \
--header 'X-Auth-Token: token_1'
```

Response will contain headers: `X-RateLimit-Remaining` - remaining count of requests in time window, 
`X-RateLimit-Limit` - limit of requests in time window. If limit is exhausted, `X-RateLimit-Retry-After` 
will be return, showing seconds for reset counter for this token.