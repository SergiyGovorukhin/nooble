package com.ghost.lucene;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@Component
@ConfigurationProperties(locations = "classpath:lucene.properties", prefix = "lucene")
public class LuceneProperties {

    @NotNull
    @Valid
    private Index index;

    @NotNull
    @Valid
    private Search search;

    public Index getIndex() {
        return index;
    }

    public void setIndex(Index index) {
        this.index = index;
    }

    public Search getSearch() {
        return search;
    }

    public void setSearch(Search search) {
        this.search = search;
    }

    // Index params
    public static class Index {

        private int depth;
        private int depthMax;
        private int threads;
        private String directoryWin;
        private String directoryUnix;
        private String directory;

        public int getDepth() {
            return depth;
        }

        public void setDepth(int depth) {
            this.depth = depth;
        }

        public int getDepthMax() {
            return depthMax;
        }

        public void setDepthMax(int depthMax) {
            this.depthMax = depthMax;
        }

        public int getThreads() {
            return threads;
        }

        public void setThreads(int threads) {
            this.threads = threads;
        }

        public String getDirectoryWin() {
            return directoryWin;
        }

        public void setDirectoryWin(String directoryWin) {
            this.directoryWin = directoryWin;
        }

        public String getDirectoryUnix() {
            return directoryUnix;
        }

        public void setDirectoryUnix(String directoryUnix) {
            this.directoryUnix = directoryUnix;
        }

        public String getDirectory() {
            return directory;
        }

        public void setDirectory(String directory) {
            this.directory = directory;
        }
    }

    // Search params
    public static class Search {

        private int max;
        private int perPage;
        private int FragmentSize;
        private int maxFragments;
        private String fragmentSeparator;
        private String preFormat;
        private String postFormat;

        public int getMax() {
            return max;
        }

        public void setMax(int max) {
            this.max = max;
        }

        public int getPerPage() {
            return perPage;
        }

        public void setPerPage(int perPage) {
            this.perPage = perPage;
        }

        public String getPreFormat() {
            return preFormat;
        }

        public void setPreFormat(String preFormat) {
            this.preFormat = preFormat;
        }

        public String getPostFormat() {
            return postFormat;
        }

        public void setPostFormat(String postFormat) {
            this.postFormat = postFormat;
        }

        public int getFragmentSize() {
            return FragmentSize;
        }

        public void setFragmentSize(int fragmentSize) {
            FragmentSize = fragmentSize;
        }

        public int getMaxFragments() {
            return maxFragments;
        }

        public void setMaxFragments(int maxFragments) {
            this.maxFragments = maxFragments;
        }

        public String getFragmentSeparator() {
            return fragmentSeparator;
        }

        public void setFragmentSeparator(String fragmentSeparator) {
            this.fragmentSeparator = fragmentSeparator;
        }
    }
}
