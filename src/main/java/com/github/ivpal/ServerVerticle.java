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
        router.get("/first").handler(this::handleFirst);
        router.get("/second").handler(this::handleSecond);

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
        var path = ctx.request().path();
        rateLimiter.check(path).onSuccess(result -> {
            if (result) {
                ctx.next();
            } else {
                logger.warn("Limit: " + path);
                ctx.response().setStatusCode(429).end();
            }
        });
    }

    private void handleFirst(RoutingContext ctx) {
        ctx.response().end("first");
    }

    private void handleSecond(RoutingContext ctx) {
        ctx.response().end("second");
    }
}