package com.ghost.lucene.search;

import com.ghost.lucene.Constants;
import com.ghost.lucene.exceptions.CreateDirectoryException;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;

@Component
public class Searcher {

    private IndexSearcher indexSearcher;
    private QueryParser queryParser;
    private TopDocs resultDocs;

    @Autowired
    public Searcher(@Value("${lucene.index.directory}") String indexDirectoryPath) throws CreateDirectoryException {
        try {
            Directory indexDirectory = FSDirectory.open(Paths.get(indexDirectoryPath));
            IndexReader indexReader = DirectoryReader.open(indexDirectory);
            indexSearcher = new IndexSearcher(indexReader);
            queryParser = new QueryParser(Constants.CONTENTS, new StandardAnalyzer());
        } catch (InvalidPathException e) {
            throw new CreateDirectoryException("Invalid path to index directory!", e);
        } catch (IOException e) {
            throw new CreateDirectoryException("Error open index directory!", e);
        }
    }

    public Collection<Document> search(String queryString) throws ParseException, IOException {
        Query query = queryParser.parse(queryString);
        resultDocs = indexSearcher.search(query, Constants.MAX_SEARCH);
        ScoreDoc[] hits = resultDocs.scoreDocs;
        Collection<Document> documents = new ArrayList<>();
        for (ScoreDoc hit : hits) {
            documents.add(indexSearcher.doc(hit.doc));
        }
        return documents;
    }

    public IndexSearcher getIndexSearcher() {
        return indexSearcher;
    }

    public int getTotalHits() {
        return resultDocs.totalHits;
    }
}
