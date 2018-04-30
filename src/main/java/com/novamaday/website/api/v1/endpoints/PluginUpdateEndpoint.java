package com.novamaday.website.api.v1.endpoints;

import com.novamaday.website.account.AccountHandler;
import com.novamaday.website.database.DatabaseManager;
import com.novamaday.website.objects.Plugin;
import com.novamaday.website.objects.User;
import org.json.JSONObject;
import spark.Request;
import spark.Response;

public class PluginUpdateEndpoint {
    public static String add(Request request, Response response) {
        JSONObject body = new JSONObject(request.body());
        if (body.has("name") && body.has("version") && body.has("main_page") && body.has("download_link")) {
            if (AccountHandler.getHandler().hasAccount(request.session().id())) {
                User user = (User) AccountHandler.getHandler().getAccount(request.session().id()).get("account");
                if (user.isAdmin()) {
                    Plugin plugin = new Plugin(body.getString("name"));

                    plugin.setVersion(body.getString("version"));
                    plugin.setMainPage(body.getString("main_page"));
                    plugin.setDownloadLink(body.getString("download_link"));

                    DatabaseManager.getManager().addPlugin(plugin);

                    response.status(200);
                    response.body("Successfully added plugin!");
                } else {
                    response.status(405);
                    response.body("Action not allowed!");
                }
            } else {
                response.status(405);
                response.body("Action not allowed!");
            }
        } else {
            response.status(400);
            response.body("Invalid request");
        }
        return response.body();
    }

    public static String check(Request request, Response response) {
        JSONObject body = new JSONObject(request.body());
        if (body.has("name") && body.has("version")) {
            Plugin plugin = DatabaseManager.getManager().getPlugin(body.getString("name"));

            if (plugin != null) {
                if (!body.getString("version").equalsIgnoreCase(plugin.getVersion())) {
                    JSONObject bod = new JSONObject();
                    bod.put("message", "Outdated! Please download the latest version!");
                    bod.put("version", plugin.getVersion());
                    body.put("download", plugin.getDownloadLink());

                    response.status(200);
                    response.body(bod.toString());
                } else {
                    response.status(200);
                    response.body(new JSONObject().put("message", "Up to date").toString());
                }
            } else {
                response.status(404);
                response.body(new JSONObject().put("message", "Plugin not found").toString());
            }
        } else {
            response.status(400);
            response.body(new JSONObject().put("message", "Bad Request").toString());
        }
        return response.body();
    }

    //TODO: Add update/download endpoint.
}
