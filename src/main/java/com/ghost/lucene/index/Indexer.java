package com.ghost.lucene.index;

import com.ghost.NoobleApplication;
import com.ghost.lucene.LuceneConstants;
import com.ghost.lucene.LuceneProperties;
import com.ghost.utility.OSValidator;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *  Indexes row data files
 */
@Component
public class Indexer {

    @Autowired
    private LuceneProperties luceneProperties;

    private IndexWriter indexWriter;
    private Analyzer analyzer;

    public Indexer() {}

    public IndexWriter getIndexWriter() {
        return indexWriter;
    }

    public void setAnalyzer(Analyzer analyzer) {
        if (analyzer != null) {
            this.analyzer = analyzer;
        }
    }

    /**
     * Retrieves index directory depending on OS (win, unix etc) specified in lucene.properties
     * @return os specific index directory or default
     */
    public String getIndexPath() {
        // TODO: may use System.getProperty("java.io.tmpdir") instead?
        switch (OSValidator.getOSType()) {
            case WIN: return luceneProperties.getIndex().getDirectoryWin();
            case UNIX: return luceneProperties.getIndex().getDirectoryUnix();
        }
        return luceneProperties.getIndex().getDirectory();
    }

    /**
     * Creates if not and registers file system directory for indexing
     * @return FSDirectory object mapped to specified path
     * @throws IOException
     */
    public Directory getIndexDirectory() throws IOException {
        String path = getIndexPath();
        Path indexPath;
        try {
            indexPath = Paths.get(path);
        } catch (InvalidPathException e) {
            NoobleApplication.log.error("Invalid index path: {}", path);
            throw new RuntimeException(e);
        }
        NoobleApplication.log.info("Index path: {}", indexPath);
        return FSDirectory.open(indexPath);
    }

    /**
     * Creates new IndexWriter instance. Locks the index directory, so one cant provide parallel search
     */
    @PostConstruct
    public void init() throws IOException {
        Directory indexDirectory;
        try {
            indexDirectory = getIndexDirectory();
        } catch (IOException e) {
            NoobleApplication.log.error("Error initializing index directory: {}", e);
            throw new RuntimeException(e);
        }
        analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        indexWriter = new IndexWriter(indexDirectory, config);
    }

    @PreDestroy
    public void close() throws IOException{
        indexWriter.close();
    }

    /**
     * Forms field type for contents. Added IndexOptions and term vectors for retrieving and highlighting content fragments.
     * @return Field type for contents field
     */
    private FieldType getContentsFieldType() {
        FieldType fieldType = new FieldType();
        fieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
        fieldType.setStored(true);
        fieldType.setStoreTermVectors(true);
        fieldType.setTokenized(true);
        fieldType.setStoreTermVectorOffsets(true);
        return fieldType;
    }

    /**
     * Builds the Lucene Document from a raw contents by adding contents, name, link and title fields.
     * @param contents of raw data (plain text) - stored, indexed, tokenized, term vector
     * @param name Document name - stored, indexed
     * @param path Document path - stored, indexed
     * @param title Document title - stored, indexed
     * @return ready to analyze Document
     */
    private Document getDocument(String contents, String name, String path, String title) {
        Document document = new Document();
        document.add(new Field(LuceneConstants.CONTENTS, contents, getContentsFieldType()));
        document.add(new StringField(LuceneConstants.SOURCE_TITLE, title, Field.Store.YES));
        document.add(new StringField(LuceneConstants.SOURCE_NAME, name, Field.Store.YES));
        document.add(new StringField(LuceneConstants.SOURCE_PATH, path, Field.Store.YES));
        return document;
    }

    /**
     * Indexes plain text source with given params
     * Uses document update method, first deleting docs with searched terms
     * @param contents plain text of the source to be indexed
     * @param name of the source
     * @param path source path (url etc)
     * @param title source title
     * @throws IOException
     */
    public void indexSource(String contents, String name, String path, String title) throws IOException{
        NoobleApplication.log.info("Indexing: {}", path);
        Document document = getDocument(contents, name, path, title);
        // TODO: avoid document duplicating while indexing or searching?
//        indexWriter.updateDocument(new Term(name), document);
        indexWriter.addDocument(document);
    }

    /**
     * Builds the Document from a raw contents file. Indexes file contents, name and path.
     * @param file with raw data (plain text file)
     * @return ready to analyze Document
     * @throws IOException
     */
    private Document getDocument(File file) throws IOException{
        Document document = new Document();
        if (file.exists() && !file.isDirectory()) {
            document.add(new TextField(LuceneConstants.CONTENTS, new FileReader(file)));
            document.add(new StringField(LuceneConstants.SOURCE_NAME, file.getName(), Field.Store.YES));
            document.add(new StringField(LuceneConstants.SOURCE_PATH, file.getCanonicalPath(), Field.Store.YES));
        } else {
            NoobleApplication.log.error("File {} Not Found", file);
        }
        return document;
    }

    /**
     * Starts file indexing process
     * @param file to be indexed
     * @throws IOException
     */
    private void indexFile(File file) throws IOException{
        NoobleApplication.log.info("Indexing file: {}", file.getCanonicalPath());
        Document document = getDocument(file);
        indexWriter.addDocument(document);
    }
}
