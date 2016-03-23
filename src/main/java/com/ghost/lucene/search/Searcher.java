package com.ghost.lucene.search;

import com.ghost.NoobleApplication;
import com.ghost.lucene.LuceneConstants;
import com.ghost.lucene.index.Indexer;
import com.ghost.lucene.LuceneProperties;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.highlight.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

@Component
public class Searcher {

    @Autowired
    private LuceneProperties luceneProperties;

    @Autowired
    private Indexer indexer;

    private IndexSearcher indexSearcher;
    private DirectoryReader directoryReader;
    private TopScoreDocCollector collector;
    private ScoreDoc[] hits;
    private Analyzer analyzer;
    private Query query;
    private Highlighter highlighter;

    public Searcher() {}

    /**
     * Initializes searcher. Locks the index directory, so you cant provide parallel index
     * Initializes directory reader from index writer. So it is possible to perform index and search ar one time.
     */
    @PostConstruct
    public void init() throws IOException {
        try {
            directoryReader = DirectoryReader.open(indexer.getIndexWriter());
        } catch (CorruptIndexException e) {
            NoobleApplication.log.error("Corrupt Index Exception!", e);
            throw new RuntimeException(e);
        } catch (IOException e) {
            NoobleApplication.log.error("IO Error open Index Reader!", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Provides search of the given search string. Results store in Collector. Max search results count defined in
     * lucene.properties
     * @param queryString to search
     * @throws IOException
     * @throws ParseException
     */
    public void search(String queryString) throws IOException, ParseException {
        if (directoryReader == null) {
            init();
        }
        DirectoryReader newDirectoryReader = DirectoryReader.openIfChanged(directoryReader);
        directoryReader = newDirectoryReader == null ? directoryReader : newDirectoryReader;
        indexSearcher = new IndexSearcher(directoryReader);
        analyzer = new StandardAnalyzer();
        QueryParser queryParser = new QueryParser(LuceneConstants.CONTENTS, analyzer);
        query = queryParser.parse(queryString);
        collector = TopScoreDocCollector.create(luceneProperties.getSearch().getMax());
        indexSearcher.search(query, collector);
        hits = collector.topDocs().scoreDocs;
    }

    public Collection<String> getFragments(String queryString) throws IOException {
        Collection<String> fragments = new ArrayList<>();
        Formatter htmlFormatter = new SimpleHTMLFormatter();
        highlighter = new Highlighter(htmlFormatter, new QueryScorer(query));
        for (ScoreDoc hit : hits) {
            fragments.add(getFragment(hit.doc));
        }
        NoobleApplication.log.info("Fragment size: {}", fragments.size());
        return fragments;
    }

    public String getFragment(int id) throws IOException {
        Document doc = indexSearcher.doc(id);
        String text = doc.get(LuceneConstants.FRAGMENT);
        Fields fields = directoryReader.getTermVectors(id);
        TokenStream tokenStream = TokenSources.getTermVectorTokenStreamOrNull(LuceneConstants.FRAGMENT,
                fields, highlighter.getMaxDocCharsToAnalyze() - 1);
        try {
            return highlighter.getBestFragments(tokenStream, text, 3, "...");
        } catch (InvalidTokenOffsetsException e) {
            NoobleApplication.log.error("Invalid Token Offsets for doc: {}", id);
        }
        return "";
    }

    /**
     * Returns relevant sorted collection of found documents
     * @return collection of found documents
     * @throws IOException
     */
    public Collection<Document> getDocs() throws IOException {
        NoobleApplication.log.info("Docs found: {}", getTotalHits());
        Collection<Document> documents = new ArrayList<>();
        for (ScoreDoc hit : hits) {
            documents.add(indexSearcher.doc(hit.doc));
        }
        return documents;
    }

    /**
     * @return Total count of found documents
     */
    public int getTotalHits() {
        return collector.getTotalHits();
    }
}
