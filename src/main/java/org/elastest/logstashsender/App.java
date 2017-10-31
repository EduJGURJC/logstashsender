package org.elastest.logstashsender;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class App {
    public static void main(String[] args) {
        int counter = 100000;

        while (counter > 0) {
            try {
                sendPost("singlelog");
                sendPost("multiplelog");
                sendPost("atomicmetric");
                sendPost("composedmetric");
            } catch (Exception e) {
                e.printStackTrace();
            }
            counter--;
        }
    }

    // HTTP POST request
    private static void sendPost(String type) throws Exception {
        String logstash = System.getenv("ET_MON_LSHTTP_API");
        if (logstash == null) {
            logstash = "http://" + "localhost" + ":5003";
        }
        System.out.println("LOGSTASH: " + logstash);
        URL url = new URL(logstash);

        URLConnection con = url.openConnection();
        HttpURLConnection http = (HttpURLConnection) con;
        http.setRequestMethod("POST");
        http.setDoOutput(true);

        String execid = System.getenv("ET_MON_EXEC");
        if (execid == null) {
            execid = "3717";
        }

        String containerName = System.getenv("CONTAINER_NAME");
        if (containerName == null) {
            containerName = "dummy_3717";
        }

        String component = System.getenv("COMPONENT");
        if (component == null) {
            component = "dynamic_component3";
        }

        String body;
        switch (type) {
        case "singlelog":
            body = sendSingleMessage(execid, containerName, component);
            break;

        case "multiplelog":
            body = sendMultipleLog(execid, containerName, component);
            break;
        case "atomicmetric":
            body = sendSingleMetric(execid, containerName, component);
            break;
        case "composedmetric":
            body = sendComposedMetric(execid, containerName, component);
            break;

        default:
            body = "";
            break;
        }
        byte[] out = body.getBytes(StandardCharsets.UTF_8);

        int length = out.length;

        http.setFixedLengthStreamingMode(length);
        http.setRequestProperty("Content-Type",
                "application/json; charset=UTF-8");
        http.connect();
        try (OutputStream os = http.getOutputStream()) {
            os.write(out);
        }
        Thread.sleep(2000);

    }

    public static String sendSingleMessage(String execid, String containerName,
            String component) {
        String message = String.join(" ", generateRandomWords(3));

        String body = "{" + "\"component\":\"" + component + "\""
                + ",\"exec\":\"" + execid + "\"" + ",\"stream\":\"custom_log\""
                + ",\"message\":\"" + message + "\"" + ",\"container_name\":\""
                + containerName + "\""
                // + ",\"custom_field\":{"
                // + "\"custom_1\":\"log\""
                // + ",\"custom_2\":\"" + containerName + "\""
                // + "}"
                + "}";
        return body;
    }

    public static String sendMultipleLog(String execid, String containerName,
            String component) {
        String message = String.join(" ", generateRandomWords(3));
        String jsonMessage = "[ " + formatJsonMessage(message) + ",";

        message = String.join(" ", generateRandomWords(3));
        jsonMessage += formatJsonMessage(message) + " ]";

        String body = "{" + "\"component\":\"" + component + "\""
                + ",\"exec\":\"" + execid + "\"" + ",\"stream\":\"default_log\""
                + ",\"messages\":" + jsonMessage + ",\"container_name\":\""
                + containerName + "\"" + "}";
        return body;
    }

    public static String sendSingleMetric(String execid, String containerName,
            String component) {
        int value = randInt(0, 100);

        String body = "{" + "\"type\":\"single_metric_example\""
                + ",\"component\":\"" + component + "\"" + ",\"exec\":\""
                + execid + "\"" + ",\"stream\":\"custom_metric\""
                + ",\"stream_type\":\"atomic_metric\""
                + ",\"single_metric_example\":\"" + value + "\""
                + ",\"unit\":\"percent\"" + ",\"container_name\":\""
                + containerName + "\"" + "}";
        return body;
    }

    public static String sendComposedMetric(String execid, String containerName,
            String component) {
        int value = randInt(0, 100);
        int value2 = randInt(0, 2000);

        String trace = "{" + "\"metric1\": " + value + "," + "\"metric2\": "
                + value2 + "" + "}";

        String units = "{" + "\"metric1\":\"percent\","
                + "\"metric2\":\"bytes\"" + "}";

        String body = "{" + "\"type\":\"metric_example\"" + ",\"component\":\""
                + component + "\"" + ",\"exec\":\"" + execid + "\""
                + ",\"stream\":\"custom_metric\""
                + ",\"stream_type\":\"composed_metrics\""
                + ",\"metric_example\": " + trace + ",\"units\": " + units
                + ",\"container_name\":\"" + containerName + "\"" + "}";
        return body;
    }

    public static String formatJsonMessage(String msg) {
        return "\"" + msg + "\"";
    }

    public static String[] generateRandomWords(int numberOfWords) {
        String[] randomStrings = new String[numberOfWords];
        Random random = new Random();
        for (int i = 0; i < numberOfWords; i++) {
            char[] word = new char[random.nextInt(8) + 3];
            for (int j = 0; j < word.length; j++) {
                word[j] = (char) ('a' + random.nextInt(26));
            }
            randomStrings[i] = new String(word);
        }
        return randomStrings;
    }

    public static int randInt(int min, int max) {
        Random rand = new Random();
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }

}
