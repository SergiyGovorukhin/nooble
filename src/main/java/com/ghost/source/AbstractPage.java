package com.ghost.source;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;

public abstract class AbstractPage {

    protected URL url;

    public AbstractPage() {}

    public URL getUrl() {
        return url;
    }

    /**
     * Constructor downloads content, it could be slow!
     * @param url to page with links
     * @throws IOException
     */
    public AbstractPage(URL url) {
        this.url = url;
    }

    /**
     * Extracts plain text from the source
     * @return
     */
    public abstract String getText();

    /**
     * Extracts title from the source
     * @return
     */
    public abstract String getTitle();

    /**
     * Extracts all links from the page like <a href={link}>text</a>
     * @return
     */
    public abstract Collection<URL> getLinks() throws IOException;

}
