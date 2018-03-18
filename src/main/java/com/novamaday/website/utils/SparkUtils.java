package com.novamaday.website.utils;

import com.novamaday.website.account.AccountHandler;
import com.novamaday.website.objects.SiteSettings;
import spark.ModelAndView;

import static spark.Spark.*;

public class SparkUtils {
    @SuppressWarnings("ThrowableNotThrown")
    public static void initSpark() {
        port(Integer.valueOf(SiteSettings.PORT.get()));

        staticFileLocation("/web/public"); // Main site location

        notFound(((request, response) -> {
            response.redirect("/", 301);
            return response.body();
        }));

        //Register the API Endpoints
        before("/api/*", (request, response) -> {
            if (!request.requestMethod().equalsIgnoreCase("POST")) {
                System.out.println("Denied '" + request.requestMethod() + "' access from: " + request.ip());
                halt(405, "Method not allowed");
            }
            //Check authorization
            if (request.headers().contains("Authorization") && !request.headers("Authorization").equals("API_KEY")) {
                //TODO: Actually check auth!!! < Just lazy right now
                halt(401, "Unauthorized");
            }
            //Only accept json because its easier to parse and handle.
				/*
				if (!request.contentType().equalsIgnoreCase("application/json")) {
					halt(400, "Bad Request");
				}
				*/
        });

        //API endpoints
        path("/api/v1", () -> {
            before("/*", (q, a) -> System.out.println("Received API call from: " + q.ip() + "; Host:" + q.host()));
        });

        //Templates and pages...
        get("/", (rq, rs) -> new ModelAndView(AccountHandler.getHandler().getAccount(rq.session().id()), "pages/index"), new ThymeleafTemplateEngine());
        get("/home", (rq, rs) -> new ModelAndView(AccountHandler.getHandler().getAccount(rq.session().id()), "pages/index"), new ThymeleafTemplateEngine());
        get("/about", (rq, rs) -> new ModelAndView(AccountHandler.getHandler().getAccount(rq.session().id()), "pages/about"), new ThymeleafTemplateEngine());
        get("/contact", (rq, rs) -> new ModelAndView(AccountHandler.getHandler().getAccount(rq.session().id()), "pages/contact"), new ThymeleafTemplateEngine());

        get("/art/ap-studio", (rq, rs) -> new ModelAndView(AccountHandler.getHandler().getAccount(rq.session().id()), "pages/art/ap-studio"), new ThymeleafTemplateEngine());
        get("/portfolio", (rq, rs) -> new ModelAndView(AccountHandler.getHandler().getAccount(rq.session().id()), "pages/art/portfolio"), new ThymeleafTemplateEngine());

        get("/policy/privacy", (rq, rs) -> new ModelAndView(AccountHandler.getHandler().getAccount(rq.session().id()), "pages/policy/privacy"), new ThymeleafTemplateEngine());
    }
}