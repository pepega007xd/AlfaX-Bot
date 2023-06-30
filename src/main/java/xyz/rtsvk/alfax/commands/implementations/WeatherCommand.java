package xyz.rtsvk.alfax.commands.implementations;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import xyz.rtsvk.alfax.commands.Command;

import java.io.BufferedInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.List;

public class WeatherCommand implements Command {
	@Override
	public void handle(User user, MessageChannel channel, List<String> args, Snowflake guildId, GatewayDiscordClient bot) {
		DecimalFormat f = new DecimalFormat("##.00");

		try {
			String cityName = args.get(0);

			// fetch the weather data
			URL url = new URL("http://api.weatherapi.com/v1/current.json?key=f525d1fb75ea4325b84120028221210&q=" + cityName + "&aqi=no");
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoInput(true);
			BufferedInputStream input = new BufferedInputStream(conn.getInputStream());

			String response = "";
			byte[] buffer = new byte[2048];
			while (input.available() > 0) {
				int read = input.read(buffer);
				response += new String(buffer, 0, read, StandardCharsets.UTF_8);
			}
			JSONObject json = (JSONObject) (new JSONParser().parse(response));

			JSONObject current = (JSONObject) json.get("current");
			double temp = Double.parseDouble(String.valueOf(current.get("temp_c")));
			double feelsLike = Double.parseDouble(String.valueOf(current.get("feelslike_c")));
			int humidity = Integer.parseInt(String.valueOf(current.get("humidity")));
			double windSpeed = Double.parseDouble(String.valueOf(current.get("wind_kph")));
			double windGusts = Double.parseDouble(String.valueOf(current.get("gust_kph")));
			int windHeading = Integer.parseInt(String.valueOf(current.get("wind_degree")));

			input.close();
			conn.disconnect();

			EmbedCreateSpec table = EmbedCreateSpec.builder()
					.author("Aktualne pocasie pre " + cityName, null, null)
					.title("Powered by Weather API")
					.url("https://www.weatherapi.com/")
					.addField("Teplota:", temp + " °C (pocitovo " + feelsLike + ")", false)
					.addField("Vlhkosť:", humidity + "%", false)
					.addField("Vietor:", windHeading + "@" + windSpeed + " km/h (" + f.format(kphToKts(windSpeed)) + " kts)", false)
					.addField("Nárazy vetra: ", windGusts + " km/h", false)
					.timestamp(Instant.now())
					.build();

			channel.createMessage(table).block();

		} catch (Exception e) {
			channel.createMessage("**Chyba pri ziskavani udajov o pocasi: " + e.getMessage() + "**").block();
		}
	}

	@Override
	public String getDescription() {
		return "Zobrazi predpoved pocasia pre zadanu oblast. [DOCASNE NEFUNKCNE]";
	}

	private double kphToKts(double kph) {
		return kph * 1.852;
	}
}
