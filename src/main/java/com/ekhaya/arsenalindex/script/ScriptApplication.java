package com.ekhaya.arsenalindex.script;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.ekhaya.arsenalindex.model.FootballMatch;
import com.ekhaya.arsenalindex.model.StockOrder;
import com.ekhaya.arsenalindex.model.Team;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class ScriptApplication {

	// Load environment variables
	private static final Dotenv dotenv = Dotenv.load();

	// Environment variables
	private static final String FOOTBALL_API_KEY = dotenv.get("FOOTBALL_API_KEY");
	private static final String ALPACA_API_KEY = dotenv.get("APCA-API-KEY-ID");
	private static final String ALPACA_SECRET_KEY = dotenv.get("APCA-API-SECRET-KEY");

	// API URLs
	private static final String FOOTBALL_API_URL = "https://api.football-data.org/v4/teams/57/matches?status=FINISHED&limit=1"; // Arsenal's
	// team ID
	private static final String ALPACA_API_URL = "https://api.alpaca.markets/v2/orders";

	// Stock lists
	private static final List<String> GOOD_STOCKS = List.of("AAPL", "MSFT", "NVDA"); // Stocks to buy if Arsenal wins
	private static final List<String> BAD_STOCKS = List.of("TSLA", "AMC", "GME"); // Stocks to buy if Arsenal loses
	private static final List<String> MEH_STOCKS = List.of("INTC", "CSCO", "IBM"); // Stocks to buy if Arsenal draws

	private static final String USED_MATCHES_FILE = System.getProperty("user.home") + "/used_matches.txt";
	public static void main(String[] args) {
		try {
			// Ensure the file exists
			ensureFileExists();


			String result = getArsenalResult();
			System.out.println("Arsenal's latest result: " + result);

			if(result == null) {
				System.out.println("Getting match issue.");	
				return;
			}


			String stock = selectStock(result);
			System.out.println("Selected stock to buy: " + stock);

			buyStock(stock);
		} catch (IOException | InterruptedException e) {
			System.out.println("Error: " + e.getMessage());
		}
	}

	// Function to retrieve the latest match result for Arsenal
	// ... existing code ...

	private static String getArsenalResult() throws IOException, InterruptedException {
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(FOOTBALL_API_URL))
				.header("X-Auth-Token", FOOTBALL_API_KEY)
				.build();

		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		String responseBody = response.body();

		// Parse the JSON response
		JSONObject jsonResponse = new JSONObject(responseBody);
		JSONArray matches = jsonResponse.getJSONArray("matches");

		if (matches.length() == 0) {
			return null; // Default to draw if no matches found
		}

		String matchId = String.valueOf(matches.getJSONObject(0).getInt("id"));

		List<String> usedMatches = Files.readAllLines(Paths.get(USED_MATCHES_FILE));

		// Check if the match has been used
        if (usedMatches.contains(matchId)) {

			System.out.println("Match already used.");
			return null; 
            
        }

		System.out.println("Match not used yet. Writing to note pad");
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(USED_MATCHES_FILE, true))) {
            writer.write(matchId);
            writer.newLine();
        }


		// Get the latest match
		JSONObject latestMatch = matches.getJSONObject(0);
		JSONObject homeTeamJson = latestMatch.getJSONObject("homeTeam");
		JSONObject awayTeamJson = latestMatch.getJSONObject("awayTeam");
		JSONObject score = latestMatch.getJSONObject("score");

		Team homeTeam = new Team(homeTeamJson.getInt("id"), homeTeamJson.getString("name"));
		Team awayTeam = new Team(awayTeamJson.getInt("id"), awayTeamJson.getString("name"));
		FootballMatch match = new FootballMatch(homeTeam, awayTeam, score.getString("winner"));

		// Log the match details
		System.out.println("Arsenal vs " + match.getOpponent() + ": " + match.getResult());

		return match.getResult();
	}

	// Function to select stock based on match result
	private static String selectStock(String result) {
		Random random = new Random();
		return switch (result) {
			case "win" -> GOOD_STOCKS.get(random.nextInt(GOOD_STOCKS.size()));
			case "loss" -> BAD_STOCKS.get(random.nextInt(BAD_STOCKS.size()));
			default -> MEH_STOCKS.get(random.nextInt(MEH_STOCKS.size()));
		};
	}

	// Function to buy stock using Alpaca API
	private static void buyStock(String stockSymbol) throws IOException, InterruptedException {
		HttpClient client = HttpClient.newHttpClient();
		StockOrder order = new StockOrder(stockSymbol, 15.0);

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(ALPACA_API_URL))
				.header("APCA-API-KEY-ID", ALPACA_API_KEY)
				.header("APCA-API-SECRET-KEY", ALPACA_SECRET_KEY)
				.header("accept", "application/json")
				.header("content-type", "application/json")
				.POST(HttpRequest.BodyPublishers.ofString(order.toJson()))
				.build();

		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

		if (response.statusCode() == 200) {
			System.out.println("Successfully bought " + stockSymbol);
		} else {
			System.out.println("Failed to buy " + stockSymbol + ": " + response.body());
		}
	}

	private static void ensureFileExists() throws IOException {
		Path filePath = Paths.get(USED_MATCHES_FILE);
		if (!Files.exists(filePath)) {
			Files.createFile(filePath);
			System.out.println("Created file: " + USED_MATCHES_FILE);
    }
}
}
