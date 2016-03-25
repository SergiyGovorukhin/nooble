<!DOCTYPE html>
<html lang="en">

<head>
    <title>${query!"search"} - ${appName!"nooble"}</title>
    <link rel="stylesheet" type="text/css" media="all" href="/css/styles.css" />
    <link rel="stylesheet" type="text/css" media="all" href="/webjars/bootstrap/3.3.6/css/bootstrap.min.css" />
</head>

<body>

    <script src="https://ajax.googleapis.com/ajax/libs/jquery/2.2.0/jquery.min.js"></script>
    <script src="webjars/bootstrap/3.3.6/js/bootstrap.min.js"></script>
    <script src="/js/script.js"></script>
    <div id="results-container">

        <div class="search-bg">
            <div class="logo-container">
                <label class="nooble-little" for="nooble-input">
                    <a href="/">${appName!"nooble"}</a>
                </label>
            </div>
            <div class="search-form-container">
                <form method="get" action="/search">
                    <div class="search-box">
                        <input class="uri" type="text" name="q" id="nooble-input" value="${query!""}" required/>
                        <button id="search-btn-little" class="search-btn-little" type="submit" value="">
                            <span class="search-btn-little-bg"></span>
                        </button>
                    </div>
                </form>
            </div>
        </div>
        <br>
        <div class="search-result" id="search-result">
            <div>
                <#if statusError??>
                    ${statusError}
                <#else>
                    <div class="result-text" id="result-text">
                        <#if pages??>${resultCount!"-"} results (${searchTime!"-"})</#if>
                    </div>
                </#if>
            </div>
            <#-- Container for more search results obtained by clicking button "View more ..." -->
            <div class="result-list" id="result-list">
                <#if pages??>
                    <#list pages as page>
                        <h3 class="page-title">
                            <a href="${page.path}">${page.title}</a>
                        </h3>
                        <div class="page-path">${page.path}</div>
                        <div class="page-fragment"><${page.fragment}</div>
                        <br>
                    </#list>
                </#if>
            </div>
            <#-- Button "View more ..." -->
            <#if (start > 0)>
                <div id="more-results-form">
                    <form>
                        <input type="hidden" id="param-start" value="${start!""}">
                        <input type="submit" id="view-more" value="View more ...">
                    </form>
                </div>
            </#if>
        </div>
    </div>
    <div id="edge-separator"></div>
    <script>var resultCount = ${resultCount};</script>
</body>

</html>