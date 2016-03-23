package com.ghost.lucene.search;

import com.fasterxml.jackson.annotation.JsonView;
import com.ghost.json.View;

/**
 *  Holds main fields of searched documents and used to translate via json
 */
public class SearchDocument {

    @JsonView(View.Public.class)
    private String title;

    @JsonView(View.Public.class)
    private String fragment;

    @JsonView(View.Public.class)
    private String path;

    public SearchDocument(String title, String fragment, String path) {
        this.title = title;
        this.fragment = fragment;
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public String getFragment() {
        return fragment;
    }

    public String getPath() {
        return path;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setFragment(String fragment) {
        this.fragment = fragment;
    }

    public void setPath(String path) {
        this.path = path;
    }
}