package com.novamaday.website.utils;

import spark.ModelAndView;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static spark.Spark.*;

public class SparkUtils {
    private static Map<String, Object> map() {
        Map m = new HashMap();
        m.put("loggedIn", false);
        m.put("year", LocalDate.now().getYear());
        return m;
    }

    public static void initSpark() {
        port(8080);

        staticFileLocation("/web/public"); // Main site location

        notFound(((request, response) -> {
            response.redirect("/", 301);
            return response.body();
        }));

        //Templates and pages...
        get("/", (rq, rs) -> new ModelAndView(map(), "pages/index"), new ThymeleafTemplateEngine());
        get("/home", (rq, rs) -> new ModelAndView(map(), "pages/index"), new ThymeleafTemplateEngine());
        get("/about", (rq, rs) -> new ModelAndView(map(), "pages/about"), new ThymeleafTemplateEngine());
        get("/contact", (rq, rs) -> new ModelAndView(map(), "pages/contact"), new ThymeleafTemplateEngine());

        //Art pages
        get("/art/ap-studio", (rq, rs) -> new ModelAndView(map(), "pages/art/ap-studio"), new ThymeleafTemplateEngine());
        get("/portfolio", (rq, rs) -> new ModelAndView(map(), "pages/art/portfolio"), new ThymeleafTemplateEngine());

        //Plugin pages
        get("/plugins/perworldchatplus", (rq, rs) -> new ModelAndView(map(), "pages/plugins/perworldchatplus"), new ThymeleafTemplateEngine());
        get("plugins/insane-warps", (rq, rs) -> new ModelAndView(map(), "pages/plugins/insanewarps"), new ThymeleafTemplateEngine());
        get("plugins/novalib", (rq, rs) -> new ModelAndView(map(), "pages/plugins/novalib"), new ThymeleafTemplateEngine());

        //Policy pages
        get("/policy/privacy", (rq, rs) -> new ModelAndView(map(), "pages/policy/privacy"), new ThymeleafTemplateEngine());
    }
}
