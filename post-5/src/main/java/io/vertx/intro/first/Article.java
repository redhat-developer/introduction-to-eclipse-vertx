package io.vertx.intro.first;


import io.vertx.core.json.JsonObject;

public class Article {

    private long id = -1;

    private String title;

    private String url;

    public Article(String title, String url) {
        this.title = title;
        this.url = url;
    }

    public Article(long id, String title, String url) {
        this.id = id;
        this.title = title;
        this.url = url;
    }

    public Article() {
        
    }

    public Article(JsonObject json) {
        this(
            json.getInteger("id", -1),
            json.getString("title"),
            json.getString("url")
        );
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Article setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public Article setUrl(String url) {
        this.url = url;
        return this;
    }
}
