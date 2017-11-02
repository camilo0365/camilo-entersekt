package com.camilo.assessment;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Response;

import java.io.IOException;
import java.nio.file.*;
import java.util.Map;

import static spark.Spark.*;

public class Main {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    private static final String PATH_FIELD = "path", ENDPOINT = "/inspect";

    public static void main(String[] args) {

        // Stop smoothly our server in case we receive a signal from the exterior (SIGINT, SIGTERM)
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            LOG.info("Stopping server...");
            stop();
            LOG.info("Server stopped.");
        }));

        // Initialize our server as requested
        port(8080);
        init();

        // Our endpoint
        post(ENDPOINT, (req, res) -> {
            // We will respond always with JSON
            res.type("application/json");

            if (!req.contentType().equals("application/json")) {
                res.status(400);
                return "{\"error\":\"Please POST with application/json as content type\"}";
            }

            String payload = req.body().isEmpty() ? "{}" : req.body();
            JsonValue val = Json.parse(payload);

            // Validate posted data
            if (!val.isObject() || val.asObject().get(PATH_FIELD) == null) {
                res.status(400);
                return String.format("{\"error\":\"Please post a JSON object with a property named %s\"}", PATH_FIELD);
            }

            // Get the path from posted data
            String path = val.asObject().get(PATH_FIELD).asString();

            // Respond accordingly
            try {
                return getPathInformation(path);
            } catch (IOException e) {
                return errorsInResponse(e, res);
            }
        });
    }

    /**
     * Builds a Json array of the provided directory's files and their information
     * @param path The path of the directory
     * @return A Json array with the information requested
     * @throws IOException In case there's any IO error
     */
    private static JsonValue getPathInformation(String path) throws IOException {
        Path dir = Paths.get(path);

        DirectoryStream<Path> stream = Files.newDirectoryStream(dir);

        JsonArray answerArray = new JsonArray();

        for (Path p : stream) {
            JsonObject fileObj = new JsonObject();

            fileObj.add("full_path", p.toString());
            fileObj.add("size", Files.size(p));

            Map<String, Object> attrs = Files.readAttributes(p, "posix:*");

            for (Map.Entry<String, Object> entry : attrs.entrySet()) {
                fileObj.add(entry.getKey(), entry.getValue().toString());
            }

            answerArray.add(fileObj);
        }

        return answerArray;
    }

    /**
     * Get a user-friendly JSON-ified error message from an Exception.
     * @param e The exception
     * @param res The response in case status code should be changed for convenience
     * @return The error message in JSON format
     */
    private static String errorsInResponse(Exception e, Response res) {

        String reason;
        if (e instanceof NoSuchFileException) {
            reason = "The specified path does not exist in the filesystem";
            res.status(404);
        } else if (e instanceof AccessDeniedException) {
            reason = "Access is denied at the filesystem level";
            res.status(403);
        } else if (e instanceof NotDirectoryException) {
            reason = "Not a directory";
            res.status(403);
        } else {
            reason = e.getMessage();
            res.status(500);
        }

        return String.format("{\"unexpected_error\":\"%s\"}", reason);
    }


}
