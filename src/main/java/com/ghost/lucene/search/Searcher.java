package com.ghost.lucene.search;

import com.ghost.NoobleApplication;
import com.ghost.lucene.LuceneConstants;
import com.ghost.lucene.LuceneProperties;
import com.ghost.lucene.index.Indexer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexWriter;
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
    private Highlighter highlighter;
    private Formatter formatter;
    private Fragmenter fragmenter;

    public Searcher() {}

    public void setAnalyzer(Analyzer analyzer) {
        if (analyzer != null) {
            this.analyzer = analyzer;
        }
    }

    /**
     * Initializes directory reader from index writer. So it is possible to perform index and search ar one time.
     * Also by default initializes StandardAnalyzer, SimpleHTMLFormatter and SimpleFragmenter
     */
    @PostConstruct
    public void init() throws IOException {
        try {
            IndexWriter indexWriter = indexer.getIndexWriter();
            directoryReader = DirectoryReader.open(indexWriter);
        } catch (CorruptIndexException e) {
            NoobleApplication.log.error("Corrupt Index Exception!", e);
            throw new RuntimeException(e);
        } catch (IOException e) {
            NoobleApplication.log.error("IO Error open Index Reader!", e);
            throw new RuntimeException(e);
        }
        analyzer = new StandardAnalyzer();
/*
        formatter = new SimpleHTMLFormatter(luceneProperties.getSearch().getPreFormat(),
                luceneProperties.getSearch().getPostFormat());
*/
        formatter = new SimpleHTMLFormatter("<span class=\"term\">", "</span>");
        fragmenter = new SimpleFragmenter(luceneProperties.getSearch().getFragmentSize());
    }

    /**
     * Performs search of the given query.
     * As search results returns hits, collectors and highlighter.
     * Max search results count defined in lucene.properties
     * @param queryString search query
     * @throws IOException
     * @throws ParseException
     */
    public void search(String queryString) throws IOException, ParseException {
        DirectoryReader newDirectoryReader = DirectoryReader.openIfChanged(directoryReader);
        directoryReader = newDirectoryReader == null ? directoryReader : newDirectoryReader;
        indexSearcher = new IndexSearcher(directoryReader);
        QueryParser queryParser = new QueryParser(LuceneConstants.CONTENTS, analyzer);
        Query query = queryParser.parse(queryString);
        collector = TopScoreDocCollector.create(luceneProperties.getSearch().getMax());
        indexSearcher.search(query, collector);
        hits = collector.topDocs().scoreDocs;
        highlighter = new Highlighter(formatter, new QueryScorer(query));
        highlighter.setTextFragmenter(fragmenter);
    }

    /**
     * Retrieves of most scored fragments in contents and put them together using specified
     * separator. Number of fragments and separator defined in lucene.properties
     * @param id Found document id
     * @return contents fragment
     * @throws IOException
     */
    public String getFragment(int id) throws IOException {
        Document document = indexSearcher.doc(id);
        String text = document.get(LuceneConstants.CONTENTS);
        if (text == null) {
            NoobleApplication.log.error("Fragment is null! for doc: {}", document.get(LuceneConstants.SOURCE_NAME));
            return "";
        }
        Fields fields = directoryReader.getTermVectors(id);
        TokenStream tokenStream = TokenSources.getTermVectorTokenStreamOrNull(LuceneConstants.CONTENTS,
                fields, highlighter.getMaxDocCharsToAnalyze() - 1);
        try {
            return highlighter.getBestFragments(tokenStream, text,
                    luceneProperties.getSearch().getMaxFragments(),
                    luceneProperties.getSearch().getFragmentSeparator());
        } catch (InvalidTokenOffsetsException e) {
            NoobleApplication.log.error("Invalid Token Offsets for doc: {}", document.get(LuceneConstants.SOURCE_NAME));
        }
        return "";
    }

    /**
     * Forms new {@link SearchDocument}
     * @param id found Lucene document id
     * @return formed {@link SearchDocument}
     * @throws IOException
     */
    public SearchDocument getSearchDocument(int id) throws IOException {
        Document document = indexSearcher.doc(id);
        String title = document.get(LuceneConstants.SOURCE_TITLE);
        String fragment = getFragment(id);
        String path = document.get(LuceneConstants.SOURCE_PATH);
        return new SearchDocument(title, fragment, path);
    }

    /**
     * Returns relevant sorted collection of found documents
     * @return collection of found documents
     * @throws IOException
     */
    public Collection<SearchDocument> getSearchDocs() throws IOException {
        NoobleApplication.log.info("Docs found: {}", getTotalHits());
        Collection<SearchDocument> documents = new ArrayList<>();
        for (ScoreDoc hit : hits) {
            documents.add(getSearchDocument(hit.doc));
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
