package io.vertx.intro.first;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.ServerSocket;

@RunWith(VertxUnitRunner.class)
public class MyFirstVerticleTest {

    private Vertx vertx;
    private int port = 8081;

    @Before
    public void setUp(TestContext context) throws IOException {
        vertx = Vertx.vertx();

        // Pick an available and random
        ServerSocket socket = new ServerSocket(0);
        port = socket.getLocalPort();
        socket.close();

        DeploymentOptions options = new DeploymentOptions()
            .setConfig(new JsonObject()
                .put("HTTP_PORT", port)
                .put("url", "jdbc:hsqldb:mem:test?shutdown=true")
                .put("driver_class", "org.hsqldb.jdbcDriver")
            );
        vertx.deployVerticle(MyFirstVerticle.class.getName(), options, context.asyncAssertSuccess());
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void testMyApplication(TestContext context) {
        final Async async = context.async();

        vertx.createHttpClient().getNow(port, "localhost", "/",
            response ->
                response.handler(body -> {
                    context.assertTrue(body.toString().contains("Hello"));
                    async.complete();
                }));
    }

    @Test
    public void checkThatTheIndexPageIsServed(TestContext context) {
        Async async = context.async();
        vertx.createHttpClient().getNow(port, "localhost", "/assets/index.html", response -> {
            context.assertEquals(response.statusCode(), 200);
            context.assertEquals(response.headers().get("Content-Type"), "text/html;charset=UTF-8");
            response.bodyHandler(body -> {
                context.assertTrue(body.toString().contains("<title>My Reading List</title>"));
                async.complete();
            });
        });
    }

    @Test
    public void checkThatWeCanAdd(TestContext context) {
        Async async = context.async();
        final String json = Json.encodePrettily(new Article("Some title", "Some url"));
        vertx.createHttpClient().post(port, "localhost", "/api/articles")
            .putHeader("Content-Type", "application/json")
            .putHeader("Content-Length", Integer.toString(json.length()))
            .handler(response -> {
                context.assertEquals(response.statusCode(), 201);
                context.assertTrue(response.headers().get("content-type").contains("application/json"));
                response.bodyHandler(body -> {
                    Article article = Json.decodeValue(body.toString(), Article.class);
                    context.assertEquals(article.getTitle(), "Some title");
                    context.assertEquals(article.getUrl(), "Some url");
                    context.assertNotNull(article.getId());
                    async.complete();
                });
            })
            .write(json)
            .end();
    }
}