package com.novamaday.website.utils;

import com.novamaday.website.account.AccountHandler;
import com.novamaday.website.database.DatabaseManager;
import com.novamaday.website.endpoints.Account;
import com.novamaday.website.objects.SiteSettings;
import com.novamaday.website.objects.UserAPIAccount;
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
                Logger.getLogger().api("Denied '" + request.requestMethod() + "' access", request.ip());
                halt(405, "Method not allowed");
            }
            //Check authorization
            if (AccountHandler.getHandler().hasAccount(request.session().id())) {
                //User is logged in from website, no API key needed
                Logger.getLogger().api("API Call from website", request.ip());
            } else {
                //Requires "Authorization Header
                if (request.headers().contains("Authorization")) {
                    String key = request.headers("Authorization");
                    UserAPIAccount acc = DatabaseManager.getManager().getAPIAccount(key);
                    if (acc != null) {
                        if (acc.isBlocked()) {
                            Logger.getLogger().api("Attempted to use blocked API Key: " + acc.getAPIKey(), request.ip());
                            halt(401, "Unauthorized");
                        } else {
                            //Everything checks out!
                            acc.setUses(acc.getUses() + 1);
                            DatabaseManager.getManager().updateAPIAccount(acc);
                        }
                    } else {
                        Logger.getLogger().api("Attempted to use invalid API Key: " + key, request.ip());
                        halt(401, "Unauthorized");
                    }
                } else {
                    Logger.getLogger().api("Attempted to use API without authorization header", request.ip());
                    halt(400, "Bad Request");
                }
            }
            //Only accept json because its easier to parse and handle.
            if (!request.contentType().equalsIgnoreCase("application/json")) {
                halt(400, "Bad Request");
            }
        });

        //API endpoints
        path("/api/v1", () -> {
            before("/*", (q, a) -> System.out.println("Received API call from: " + q.ip() + "; Host:" + q.host()));
            path("/account", () -> {
                post("/register", Account::register);
                post("/login", Account::login);
                post("/logout", Account::logout);
            });
        });

        //Templates and pages...
        get("/", (rq, rs) -> new ModelAndView(AccountHandler.getHandler().getAccount(rq.session().id()), "pages/index"), new ThymeleafTemplateEngine());
        get("/home", (rq, rs) -> new ModelAndView(AccountHandler.getHandler().getAccount(rq.session().id()), "pages/index"), new ThymeleafTemplateEngine());
        get("/about", (rq, rs) -> new ModelAndView(AccountHandler.getHandler().getAccount(rq.session().id()), "pages/about"), new ThymeleafTemplateEngine());
        get("/contact", (rq, rs) -> new ModelAndView(AccountHandler.getHandler().getAccount(rq.session().id()), "pages/contact"), new ThymeleafTemplateEngine());

        //Art pages
        get("/art/ap-studio", (rq, rs) -> new ModelAndView(AccountHandler.getHandler().getAccount(rq.session().id()), "pages/art/ap-studio"), new ThymeleafTemplateEngine());
        get("/portfolio", (rq, rs) -> new ModelAndView(AccountHandler.getHandler().getAccount(rq.session().id()), "pages/art/portfolio"), new ThymeleafTemplateEngine());

        //Plugin pages
        get("/plugins/perworldchatplus", (rq, rs) -> new ModelAndView(AccountHandler.getHandler().getAccount(rq.session().id()), "pages/plugins/perworldchatplus"), new ThymeleafTemplateEngine());
        get("plugins/insane-warps", (rq, rs) -> new ModelAndView(AccountHandler.getHandler().getAccount(rq.session().id()), "pages/plugins/insanewarps"), new ThymeleafTemplateEngine());

        //Policy pages
        get("/policy/privacy", (rq, rs) -> new ModelAndView(AccountHandler.getHandler().getAccount(rq.session().id()), "pages/policy/privacy"), new ThymeleafTemplateEngine());
    }
}