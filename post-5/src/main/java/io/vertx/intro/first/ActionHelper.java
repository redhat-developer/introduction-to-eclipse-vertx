package io.vertx.intro.first;

import io.reactivex.functions.Action;
import io.reactivex.functions.BiConsumer;
import io.reactivex.functions.Consumer;
import io.vertx.core.AsyncResult;
import io.vertx.core.json.Json;
import io.vertx.reactivex.ext.web.RoutingContext;

import java.util.NoSuchElementException;

/**
 * Some helper code.
 */
public class ActionHelper {

    /**
     * Returns a bi-consumer writing the received {@link AsyncResult} to the routing context and setting
     * the HTTP status to the given status.
     *
     * @param context the routing context
     * @param status  the status
     * @return the bi-consumer
     */
    private static <T> BiConsumer<T, Throwable> writeJsonResponse(RoutingContext context, int status) {
        return (res, err) -> {
            if (err != null) {
                if (err instanceof NoSuchElementException) {
                    context.response().setStatusCode(404).end(err.getMessage());
                } else {
                    context.fail(err);
                }
            } else {
                context.response().setStatusCode(status)
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(Json.encodePrettily(res));
            }
        };
    }

    static <T> BiConsumer<T, Throwable> ok(RoutingContext rc) {
        return writeJsonResponse(rc, 200);
    }

    static <T> BiConsumer<T, Throwable> created(RoutingContext rc) {
        return writeJsonResponse(rc, 201);
    }

    static Action noContent(RoutingContext rc) {
        return () -> rc.response().setStatusCode(204).end();
    }

    static Consumer<Throwable> onError(RoutingContext rc) {
        return err -> {
            if (err instanceof NoSuchElementException) {
                rc.response().setStatusCode(404).end(err.getMessage());
            } else {
                rc.fail(err);
            }
        };
    }

}
