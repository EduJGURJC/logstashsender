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

		while(counter > 0){
			try {
				sendPost();
			} catch (Exception e) {
				e.printStackTrace();
			}
			counter--;
		}
	}

	// HTTP POST request
	private static void sendPost() throws Exception {
		String logstash = System.getenv("LS_API");
		if (logstash == null) {
			logstash = "http://" + "localhost" + ":5003";
		}
		System.out.println("LOGSTASH: " + logstash);
		URL url = new URL(logstash);

		URLConnection con = url.openConnection();
		HttpURLConnection http = (HttpURLConnection) con;
		http.setRequestMethod("POST");
		http.setDoOutput(true);

		String tjobexecid = System.getenv("TJOBEXEC_ID");
		if (tjobexecid == null) {
			tjobexecid = "5";
		}
		
		String containerName = System.getenv("CONTAINER_NAME");
		if (containerName == null) {
			containerName = "dummy_3717";
		}
		

		String message = String.join(" ", generateRandomWords(3));
		
		String body = "{"
				+ "\"component_type\":\"dynamic_component_type3\""
				+ ",\"tjobexec\":\"" + tjobexecid + "\""
				+ ",\"info_id\":\"custom_log\""
				+ ",\"trace_type\":\"log\""
				+ ",\"message\":\"" + message + "\""
				+ ",\"container_name\":\"" + containerName + "\""
//				+ ",\"custom_field\":{"
//					+ "\"custom_1\":\"log\""
//					+ ",\"custom_2\":\"" + containerName + "\""
//				+ "}"
				+ "}";
		
		byte[] out = body.getBytes(StandardCharsets.UTF_8);

		int length = out.length;

		http.setFixedLengthStreamingMode(length);
		http.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
		http.connect();
		try (OutputStream os = http.getOutputStream()) {
			os.write(out);
		}
	    Thread.sleep(2000);

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

}
