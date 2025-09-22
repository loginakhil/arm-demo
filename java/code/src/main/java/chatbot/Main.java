package chatbot;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Structure;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import org.json.JSONObject;

final class ChatbotRequest {
    private final String prompt;
    private final int tokens;

    public ChatbotRequest(String prompt, int tokens) {
        this.prompt = prompt;
        this.tokens = tokens;
    }

    public String getPrompt() {
        return prompt;
    }

    public int getTokens() {
        return tokens;
    }
}

class RequestHandler implements HttpHandler {
    private String getRequestBody(HttpExchange exchange) throws IOException {
        String line;
        StringBuilder body = new StringBuilder();

        InputStreamReader reader = new InputStreamReader(exchange.getRequestBody());
        BufferedReader responseReader = new BufferedReader(reader);

        while ((line = responseReader.readLine()) != null) body.append(line);

        return body.toString();
    }

    private ChatbotRequest getChatbotRequest(String body) {
        JSONObject object = new JSONObject(body);
        return new ChatbotRequest(object.getString("Prompt"), object.getInt("Tokens"));
    }

    private synchronized String predict(ChatbotRequest request) {
        Main.ChatbotResponse response = new Main.ChatbotResponse();
        Main.chatbotlib.predict(request.getPrompt(), request.getTokens(), response);

        return String.format("\nResponse: %s\n\nInference Time: %s ms | Total Tokens: %d\n", response.output.toString(), response.inference_time, response.num_tokens);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String response = "";

        try {
            String body = getRequestBody(exchange);
            response = predict(getChatbotRequest(body));
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length());
        }
        catch (Exception e) {
            response = "The request body must be a JSON formatted string containing a non-empty 'prompt' attribute.";
            exchange.sendResponseHeaders(400, response.length());
        }
        finally {
            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();
        }
    }
}

class RootHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String response = "Welcome to the Graviton Developer Day!";
        exchange.getResponseHeaders().set("Content-Type", "text/plain");
        exchange.sendResponseHeaders(200, response.length());

        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}

public class Main {
    private static final int PORT = 8081;
    public static ChatbotLib chatbotlib;

    public static class ChatbotResponse extends Structure{
        public String output;
        public float inference_time;
        public int num_tokens;
    
        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("output", "inference_time", "num_tokens");
        }
    }

    public interface ChatbotLib extends Library {
        int init();
        int predict(String prompt, int tokens, ChatbotResponse response);
    }

    private static void initWebServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/", new RootHandler());
        server.createContext("/generateResponse", new RequestHandler());
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
    }

    public static void main(String[] args) {
        if (System.getProperty("os.arch").equals("amd64")) {
            chatbotlib = (ChatbotLib) Native.load("libchatbot-amd64.so", ChatbotLib.class);
        } else {
            chatbotlib = (ChatbotLib) Native.load("libchatbot-arm64.so", ChatbotLib.class);
        }
        chatbotlib.init();
        try {
            initWebServer();
            System.out.println("[INFO] Web server initialised");
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
