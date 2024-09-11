package me._2818.countryWelcome;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class CountryWelcome extends JavaPlugin implements Listener {
    public void onEnable() {
        this.getLogger().info("Deutschland plugin has been enabled.");
        this.getServer().getPluginManager().registerEvents(this, this);
        this.saveDefaultConfig();
    }

    public void onDisable() {
        this.getLogger().info("Deutschland plugin has been disabled.");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String playerIP = player.getAddress().getAddress().getHostAddress();
        String country = this.apiCheck(playerIP);
        if (country != null) {
            this.getLogger().info(player.getName() + " joined from " + country);
            Component welcomeMessage = this.welcomeMessage(player, country);
            if (welcomeMessage != null) {
                this.getServer().getScheduler().runTaskLater(this, () -> player.sendMessage(welcomeMessage), getConfig().getInt("Delay", 20));
            }
        } else {
            this.getLogger().warning("Unable to identify country for " + player.getName());
        }

    }

    public String apiCheck(String ip) {
        String url = "http://ip-api.com/json/" + ip;

        try {
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection)obj.openConnection();
            con.setRequestMethod("GET");
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder response = new StringBuilder();

            String inputLine;
            while((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            in.close();
            JSONObject responseJson = (JSONObject)JSONValue.parse(response.toString());
            return (String)responseJson.get("country");
        } catch (Exception var9) {
            return null;
        }
    }

    public Component welcomeMessage(Player player, String country) {
        ConfigurationSection countriesSection = this.getConfig().getConfigurationSection("Countries");
        if (countriesSection != null && countriesSection.contains(country)) {
            String message = countriesSection.getString(country + ".Message");
            String hex = countriesSection.getString(country + ".Hex", "#FFFFFF");
            String link = countriesSection.getString(country + ".Link");
            boolean bold = countriesSection.getBoolean(country + ".Bold", false);
            boolean italic = countriesSection.getBoolean(country + ".Italic", false);
            if (message == null) {
                return null;
            } else {
                ComponentBuilder componentBuilder = Component.text().append(Component.text(message).color(TextColor.fromHexString(hex)));
                if (bold) {
                    componentBuilder.decorate(TextDecoration.BOLD);
                }

                if (italic) {
                    componentBuilder.decorate(TextDecoration.ITALIC);
                }

                if (link != null) {
                    componentBuilder.clickEvent(ClickEvent.openUrl(link));
                }

                return componentBuilder.build();
            }
        } else {
            return null;
        }
    }
}