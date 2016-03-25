package com.ghost.lucene.index;

import com.ghost.NoobleApplication;
import com.ghost.source.AbstractPage;
import com.ghost.source.JsoupPage;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IndexTask implements Callable<Integer> {

    // patterns form unique link names, based on URL
    private static final String URL_FILENAME_PATTERN = "[^a-zA-Z0-9-_\\.]";
    private static final String FILENAME_PARTS_SEPARATOR = "_";

    private int numberOfThreads;
    private Indexer indexer;
    private URL link;
    private int depth;

    public IndexTask(URL link, Indexer indexer, int depth, int numberOfThreads) {
        this.link = link;
        this.indexer = indexer;
        this.depth = depth;
        this.numberOfThreads = numberOfThreads;
    }

    /**
     * Called by ExecutorService
     * @return number of indexed pages
     * @throws Exception
     */
    @Override
    public Integer call() throws Exception {
        AbstractPage page = new JsoupPage(link);
        indexer.indexSource(page.getText(), buildFileName(link), link.toString(),page.getTitle());
        return depth > 0 ? index(page) : 1;
    }

    /**
     * Runs multithreading recursive page indexing process using newFixedThreadPool Executor.
     * @param page to index
     * @return number of indexed pages
     * @throws IOException
     */
    private int index(AbstractPage page) throws IOException, InterruptedException {
        depth--;
        Collection<URL> links = page.getLinks();
        NoobleApplication.log.info("Link count: " + links.size());
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        Collection<Callable<Integer>> tasks = new HashSet<>();
        links
                .stream()
                .forEach(link -> tasks.add(new IndexTask(link, indexer, depth, numberOfThreads)));

        final int[] errors = {0};
        int indexed = executorService.invokeAll(tasks)
                .stream()
                .mapToInt(future -> {
                    Integer indexedCount = 0;
                    try {
                        indexedCount = future.get();
                    } catch (Exception e) {
                        errors[0]++;
                    }
                    return indexedCount == null ? 0: indexedCount;
                })
                .sum();

        executorService.shutdown();
        NoobleApplication.log.info("Not indexed (errors): {}", errors[0]);
        NoobleApplication.log.info("Indexed: {}", indexed);
        return indexed;
    }

    /**
     * Converts url to unique file name, replacing all symbols in url with "_" except letters and numbers
     * @param url to convert
     * @return url based unique file name
     */
    private String buildFileName(URL url) {
        return url.toString().replaceAll(URL_FILENAME_PATTERN, FILENAME_PARTS_SEPARATOR);
    }
}
