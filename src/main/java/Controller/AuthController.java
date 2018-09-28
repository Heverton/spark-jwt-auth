package Controller;

import Bootstrapper.AppModule;
import DataAccess.ISignupRepository;
import Model.UserModel;
import Service.IJwtTokenService;
import Validation.ITokenValidator;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import spark.Spark;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by previousdeveloper on 14.09.2015.
 */
public class AuthController {

    public AuthController() {

        Injector injector = Guice.createInjector(new AppModule());
        ISignupRepository signupRepository = injector.getInstance(ISignupRepository.class);
        IJwtTokenService jwtAuthService = injector.getInstance(IJwtTokenService.class);
        ITokenValidator tokenValidation = injector.getInstance(ITokenValidator.class);

        //TODO: DI CONTAINER
        Gson gson = new Gson();

        Spark.port(4201);

        Spark.before("/protected/*", (request, response) -> {
            String xApiToken = request.headers("X-API-TOKEN");
            if (xApiToken != null) {
                boolean result = tokenValidation.validate(xApiToken);
                if (!result) {
                    Spark.halt(401, "token expired");
                }
            } else {
                Spark.halt(401, "header token not found");
            }
        });

        Spark.post("/token", (request, response) -> {

            UserModel userModel = null;
            String token = null;
            String result = null;

            Map<String, String> keyValuePair = null;

            // POST - http://localhost:4201/token
            // BODY - { 'username': '123', 'password': '123' }
            try {
                userModel = gson.fromJson(request.body(), UserModel.class);
                String username = userModel.getUsername();
                String password = userModel.getPassword();
                if (username != null && password != null) {
                    token = jwtAuthService.tokenGenerator(username, password);
                    keyValuePair = new HashMap();
                    keyValuePair.put("token", token);
                    result = gson.toJson(keyValuePair);
                }

            } catch (JsonSyntaxException e) {
                e.printStackTrace();
                response.status(400);
                return "INVALID JSON";
            }
            return result;
        });

        Spark.post("/register", (request, response) -> {
            UserModel userModel = null;
            Map<String, Object> user;

            try {
                userModel = gson.fromJson(request.body(), UserModel.class);
                signupRepository.saveUser(userModel);
                user = new HashMap<>();
                user.put("Registered:", userModel.getUsername());

            } catch (JsonSyntaxException e) {
                e.printStackTrace();
                response.status(400);

                return "INVALID JSON";
            }

            return gson.toJson(user);
        });

        Spark.post("/protected/deneme", (request, response) -> {

            return "SELAM BASARILI GIRIS YAPTIN :)";
        });
    }
}
