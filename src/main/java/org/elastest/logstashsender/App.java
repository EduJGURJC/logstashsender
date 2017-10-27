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
			tjobexecid = "21";
		}
		
		String containerName = System.getenv("CONTAINER_NAME");
		if (containerName == null) {
			containerName = "dummy_3717";
		}
		
		String body;
		body= sendMessageDynamically(tjobexecid, containerName);
//		body = sendMultipleLog(tjobexecid, containerName);
//		body = sendComposedMetric(tjobexecid, containerName);
//		body = sendSingleMetric(tjobexecid, containerName);
		

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
	
	public static String sendMessageDynamically(String tjobexecid, String containerName){
		String message = String.join(" ", generateRandomWords(3));
		
		String body = "{"
				+ "\"component\":\"dynamic_component3\""
				+ ",\"tjobexec\":\"" + tjobexecid + "\""
				+ ",\"stream\":\"custom_log\""
				+ ",\"stream_type\":\"log\""
				+ ",\"message\":\"" + message + "\""
				+ ",\"container_name\":\"" + containerName + "\""
//				+ ",\"custom_field\":{"
//					+ "\"custom_1\":\"log\""
//					+ ",\"custom_2\":\"" + containerName + "\""
//				+ "}"
				+ "}";
		return body;
	}
	
	public static String sendMultipleLog(String tjobexecid, String containerName){
		String message = String.join(" ", generateRandomWords(3));
		String jsonMessage = "[ " + formatJsonMessage(message) + ",";
		
		message = String.join(" ", generateRandomWords(3));
		jsonMessage += formatJsonMessage(message) + " ]";
		
		String body = "{"
				+ "\"component\":\"test\""
				+ ",\"tjobexec\":\"" + tjobexecid + "\""
				+ ",\"stream\":\"default_log\""
				+ ",\"stream_type\":\"log\""
				+ ",\"messages\":" + jsonMessage
				+ ",\"container_name\":\"" + containerName + "\""
				+ "}";
		return body;
	}
	
	public static String sendSingleMetric(String tjobexecid, String containerName){
		int value = randInt(0, 100);
		
		String body = "{"
				+ "\"type\":\"single_metric_example\""
				+ ",\"component\":\"test\""
				+ ",\"tjobexec\":\"" + tjobexecid + "\""
				+ ",\"stream\":\"custom_metric\""
				+ ",\"stream_type\":\"atomic_metric\""
				+ ",\"single_metric_example\":\"" + value + "\""
				+ ",\"unit\":\"percent\""
				+ ",\"container_name\":\"" + containerName + "\""
				+ "}";
		return body;
	}
	
	
	public static String sendComposedMetric(String tjobexecid, String containerName){
		int value = randInt(0, 100);
		int value2 = randInt(0, 2000);
		
		String trace = "{"
				+ "\"metric1\": " + value + ","
				+ "\"metric2\": " + value2 + ""
				+"}";
		
		String units = "{"
				+ "\"metric1\":\"percent\","
				+ "\"metric2\":\"bytes\""
				+"}";
		
		String body = "{"
				+ "\"type\":\"metric_example\""
				+ ",\"component\":\"test\""
				+ ",\"tjobexec\":\"" + tjobexecid + "\""
				+ ",\"stream\":\"custom_metric\""
				+ ",\"stream_type\":\"composed_metrics\""
				+ ",\"metric_example\": " + trace
				+ ",\"units\": " + units
				+ ",\"container_name\":\"" + containerName + "\""
				+ "}";
		return body;
	}
	
	public static String formatJsonMessage(String msg){
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
