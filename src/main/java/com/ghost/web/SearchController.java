package com.ghost.web;

import com.ghost.NoobleApplication;
import com.ghost.lucene.search.SearchDocument;
import com.ghost.lucene.search.SearchService;
import org.apache.lucene.queryparser.classic.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Controller
@Scope("session")
@PropertySource("classpath:application.properties")
public class SearchController {

    @Autowired
    private Environment environment;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private SearchService searchService;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String searchForm(ModelMap map, Locale locale) {
        assert environment.getProperty("spring.application.name") == null;
        assert messageSource.getMessage("spring.application.name", null, locale) == null;
        NoobleApplication.log.info("App name: {}", environment.getProperty("spring.application.name"));
        map.put("appName", environment.getProperty("spring.application.name"));
        return "search";
    }

    @RequestMapping(value = "/search", method = RequestMethod.GET)
    public String search(@RequestParam("q") String query, ModelMap model, Locale locale) {
        NoobleApplication.log.info("Search query: {}", query);
        List<SearchDocument> list = new ArrayList<>();
        try {
            list.addAll(searchService.search(query));
        } catch (InvalidPathException e) {
            model.addAttribute("status.error", messageSource.getMessage("search.error.path", null, locale));
        } catch (ParseException e) {
            model.addAttribute("status.error", messageSource.getMessage("search.error.parse", null, locale));
        } catch (IOException e) {
            model.addAttribute("status.error", messageSource.getMessage("search.error.directory", null, locale));
            e.printStackTrace();
        }
        model.addAttribute("pages", list);
        model.addAttribute("appName", environment.getProperty("spring.application.name"));
        model.addAttribute("query", query);
        model.addAttribute("resultCount", searchService.getTotalHits());
        return "searchResult";
    }

}
