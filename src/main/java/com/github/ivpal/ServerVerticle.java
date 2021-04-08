package com.github.ivpal;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerVerticle extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(ServerVerticle.class);

    private final RateLimiter rateLimiter;

    public ServerVerticle(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        var router = Router.router(vertx);
        router.get("/route").handler(this::handleRoute);

        var rateLimitRouter = Router.router(vertx);
        rateLimitRouter.get().handler(this::rateLimitingHandler);
        rateLimitRouter.mountSubRouter("/", router);

        vertx.createHttpServer()
            .requestHandler(rateLimitRouter)
            .listen(8000)
            .onSuccess(server -> {
                logger.info("Started");
                startPromise.complete();
            })
            .onFailure(startPromise::fail);
    }

    private void rateLimitingHandler(RoutingContext ctx) {
        var token = ctx.request().headers().get("X-Auth-Token");
        if (token == null) {
            ctx.response().setStatusCode(401).end();
            return;
        }

        rateLimiter.check(token).onSuccess(result -> {
            ctx.response().putHeader("X-RateLimit-Remaining", Long.toString(result.getRemaining()));
            ctx.response().putHeader("X-RateLimit-Limit", Long.toString(result.getLimit()));
            if (result.isSuccess()) {
                ctx.next();
            } else {
                logger.warn(String.format("Limit reached for: %s, %s", token, result));
                ctx.response()
                    .putHeader("X-RateLimit-Retry-After", Long.toString(result.getRetryAfter()))
                    .setStatusCode(429).end();
            }
        });
    }

    private void handleRoute(RoutingContext ctx) {
        ctx.response().end("OK");
    }
}
