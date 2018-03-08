package io.vertx.intro.first;

import io.vertx.config.ConfigRetriever;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;

import java.util.LinkedHashMap;
import java.util.Map;

public class MyFirstVerticle extends AbstractVerticle {

    // Store our readingList
    private Map<Integer, Article> readingList = new LinkedHashMap<>();

    @Override
    public void start(Future<Void> fut) {
        // Populate our set of article
        createSomeData();

        // Create a router object.
        Router router = Router.router(vertx);

        // Bind "/" to our hello message - so we are still compatible.
        router.route("/").handler(routingContext -> {
            HttpServerResponse response = routingContext.response();
            response
                .putHeader("content-type", "text/html")
                .end("<h1>Hello from my first Vert.x 3 application</h1>");
        });
        // Serve static resources from the /assets directory
        router.route("/assets/*").handler(StaticHandler.create("assets"));
        router.get("/api/articles").handler(this::getAll);
        router.get("/api/articles/:id").handler(this::getOne);
        router.route("/api/articles*").handler(BodyHandler.create());
        router.post("/api/articles").handler(this::addOne);
        router.delete("/api/articles/:id").handler(this::deleteOne);
        router.put("/api/articles/:id").handler(this::updateOne);


        ConfigRetriever retriever = ConfigRetriever.create(vertx);
        retriever.getConfig(
            config -> {
                if (config.failed()) {
                    fut.fail(config.cause());
                } else {
                    // Create the HTTP server and pass the "accept" method to the request handler.
                    vertx
                        .createHttpServer()
                        .requestHandler(router::accept)
                        .listen(
                            // Retrieve the port from the configuration,
                            // default to 8080.
                            config.result().getInteger("HTTP_PORT", 8080),
                            result -> {
                                if (result.succeeded()) {
                                    fut.complete();
                                } else {
                                    fut.fail(result.cause());
                                }
                            }
                        );
                }
            }
        );
    }


    // Create a readingList
    private void createSomeData() {
        Article article1 = new Article("Fallacies of distributed computing", "https://en.wikipedia.org/wiki/Fallacies_of_distributed_computing");
        readingList.put(article1.getId(), article1);
        Article article2 = new Article("Reactive Manifesto", "https://www.reactivemanifesto.org/");
        readingList.put(article2.getId(), article2);
    }

    private void getAll(RoutingContext routingContext) {
        routingContext.response()
            .putHeader("content-type", "application/json; charset=utf-8")
            .end(Json.encodePrettily(readingList.values()));
    }

    private void addOne(RoutingContext routingContext) {
        Article article = routingContext.getBodyAsJson().mapTo(Article.class);
        readingList.put(article.getId(), article);
        routingContext.response()
            .setStatusCode(201)
            .putHeader("content-type", "application/json; charset=utf-8")
            .end(Json.encodePrettily(article));
    }

    private void deleteOne(RoutingContext routingContext) {
        String id = routingContext.request().getParam("id");
        try {
            Integer idAsInteger = Integer.valueOf(id);
            readingList.remove(idAsInteger);
            routingContext.response().setStatusCode(204).end();
        } catch (NumberFormatException e) {
            routingContext.response().setStatusCode(400).end();
        }
    }


    private void getOne(RoutingContext routingContext) {
        String id = routingContext.request().getParam("id");
        try {
            Integer idAsInteger = Integer.valueOf(id);
            Article article = readingList.get(idAsInteger);
            if (article == null) {
                // Not found
                routingContext.response().setStatusCode(404).end();
            } else {
                routingContext.response()
                    .setStatusCode(200)
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(Json.encodePrettily(article));
            }
        } catch (NumberFormatException e) {
            routingContext.response().setStatusCode(400).end();
        }
    }

    private void updateOne(RoutingContext routingContext) {
        String id = routingContext.request().getParam("id");
        try {
            Integer idAsInteger = Integer.valueOf(id);
            Article article = readingList.get(idAsInteger);
            if (article == null) {
                // Not found
                routingContext.response().setStatusCode(404).end();
            } else {
                JsonObject body = routingContext.getBodyAsJson();
                article.setTitle(body.getString("title")).setUrl(body.getString("url"));
                readingList.put(idAsInteger, article);
                routingContext.response()
                    .setStatusCode(200)
                    .putHeader("content-type", "application/json; charset=utf-8")
                    .end(Json.encodePrettily(article));
            }
        } catch (NumberFormatException e) {
            routingContext.response().setStatusCode(400).end();
        }

    }

}