package com.novamaday.website.endpoints;

import com.novamaday.website.account.AccountHandler;
import com.novamaday.website.database.DatabaseManager;
import org.json.JSONObject;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import spark.Request;
import spark.Response;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static spark.Spark.halt;

/**
 * Created by: NovaFox161 at 4/11/2018
 * Website: https://www.novamaday.com
 * For Project: Personal-Site
 */
public class Account {
    public static String register(Request request, Response response) {
        JSONObject body = new JSONObject(request.body());
        if (body.has("username") && body.has("email") && body.has("password")) {
            String username = body.getString("username");
            String email = body.getString("email");
            if (!DatabaseManager.getManager().usernameOrEmailTaken(username, email)) {
                BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
                String hash = encoder.encode(body.getString("password"));

                DatabaseManager.getManager().addNewUser(username, email, hash);

                //TODO: Send confirmation email!!!

                Map<String, Object> m = new HashMap<>();
                m.put("year", LocalDate.now().getYear());
                m.put("account", DatabaseManager.getManager().getUserFromEmail(email));
                AccountHandler.getHandler().addAccount(m, request.session().id());

                response.redirect("/account", 301);
            } else {
                response.body(new JSONObject().put("Message", "Username/email taken!").toString());
            }
        } else {
            halt(400, "Invalid request");
        }
        return response.body();
    }

    public static String login(Request request, Response response) {
        JSONObject body = new JSONObject(request.body());
        if (body.has("email") && body.has("password")) {
            String email = body.getString("email");
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            String hash = encoder.encode(body.getString("password"));
            if (DatabaseManager.getManager().validLogin(email, hash)) {

                Map<String, Object> m = new HashMap<>();
                m.put("year", LocalDate.now().getYear());
                m.put("account", DatabaseManager.getManager().getUserFromEmail(email));
                AccountHandler.getHandler().addAccount(m, request.session().id());

                response.redirect("/account", 301);
            } else {
                response.body(new JSONObject().put("Message", "Username/email taken!").toString());
            }
        } else {
            halt(400, "Invalid request");
        }
        return response.body();
    }

    public static String logout(Request request, Response response) {
        AccountHandler.getHandler().removeAccount(request.session().id());
        response.redirect("/", 301);
        return response.body();
    }
}