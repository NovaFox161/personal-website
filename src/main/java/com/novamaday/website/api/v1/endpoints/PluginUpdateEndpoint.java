package com.novamaday.website.api.v1.endpoints;

import com.novamaday.website.database.DatabaseManager;
import com.novamaday.website.objects.Plugin;
import org.json.JSONObject;
import spark.Request;
import spark.Response;

public class PluginUpdateEndpoint {
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
                response.body("Plugin not found");
            }
        } else {
            response.status(400);
            response.body("Invalid request");
        }
        return response.body();
    }

    //TODO: Add update/download endpoint.
}
