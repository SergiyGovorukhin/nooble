package com.ghost.source;

import com.ghost.NoobleApplication;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class JsoupPage extends AbstractPage {

    private Document document;

    public JsoupPage(URL url) {
        super(url);
        getContent();
    }

    protected void getContent() {
        try {
            Connection connection = Jsoup.connect(url.toString());
            connection.ignoreHttpErrors(true);
            document = connection.get();
        } catch (MalformedURLException e) {
            NoobleApplication.log.error("Bad URL {}", url.toString());
        } catch (SocketTimeoutException e) {
            NoobleApplication.log.error("Connection time is out!");
        } catch (IOException e) {
            NoobleApplication.log.error("Error getting source {}, exception: {}", url.toString(), e.getMessage());
        }
    }

    @Override
    public String getText() {
        return Jsoup.parse(document.toString()).text();
    }

    @Override
    public String getTitle() {
        return document.title();
    }

    @Override
    public Collection<URL> getLinks() {
        Elements linkElements = document.select("a[href]");
        Set<String> links = linkElements
                .stream()
                .map(link -> link.attr("abs:href"))
                .collect(Collectors.toSet());
        Collection<URL> urls = new HashSet<>();
        links
                .stream()
                .forEach(link -> {
                    try {
                        urls.add(new URL(link));
                    } catch (MalformedURLException e) {
                        NoobleApplication.log.error("Malformed link {}", link);
                    }
                });
        return urls;
    }
}
