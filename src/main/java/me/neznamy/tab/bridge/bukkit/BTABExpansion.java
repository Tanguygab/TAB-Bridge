package me.neznamy.tab.bridge.bukkit;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;

import java.util.*;

public class BTABExpansion extends PlaceholderExpansion {

    private final BukkitBridge plugin;
    private final String loading;
    public final Map<Player,Map<String,String>> properties = new HashMap<>();

    public BTABExpansion(BukkitBridge plugin) {
        this.plugin = plugin;
        loading = plugin.data.loading;
    }

    @Override
    public String getIdentifier() {
        return "btab";
    }

    @Override
    public boolean persist(){
        return true;
    }

    @Override
    public boolean canRegister(){
        return true;
    }

    @Override
    public String getAuthor(){
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public String getVersion(){
        return plugin.getDescription().getVersion();
    }

    @Override
    public List<String> getPlaceholders() {
        return new ArrayList<>(Arrays.asList("%btab_scoreboard_visible%",
                "%btab_bossbar_visible%",
                "%btab_ntpreview%",
                "%btab_placeholder_<placeholder>%",
                "%btab_<property>%"));
    }

    public void getProp(Player player, String property,String hi) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("PlaceholderAPI");
        out.writeUTF(property);
        if (!hi.equalsIgnoreCase(""))
            out.writeUTF(hi);
        player.sendPluginMessage(plugin, BukkitBridge.ADDON_CHANNEL_NAME,out.toByteArray());
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier){
        if (player == null) return "";
        if (identifier.equals("scoreboard_visible")) {
            getProp(player,"scoreboard_visible","");
            if (properties.containsKey(player) && properties.get(player).containsKey("scoreboard_visible"))
                return Boolean.parseBoolean(properties.get(player).get("scoreboard_visible")) ? "Enabled" : "Disabled";
            return loading;
        }
        if (identifier.equals("bossbar_visible")) {
            getProp(player,"bossbar_visible","");
            if (properties.containsKey(player) && properties.get(player).containsKey("bossbar_visible"))
                return Boolean.parseBoolean(properties.get(player).get("bossbar_visible")) ? "Enabled" : "Disabled";
            return loading;
        }
        if (identifier.equals("ntpreview")) {
            getProp(player,"ntpreview","");
            if (properties.containsKey(player) && properties.get(player).containsKey("ntpreview"))
                return Boolean.parseBoolean(properties.get(player).get("ntpreview")) ? "Enabled" : "Disabled";
            return loading;
        }
        if (identifier.startsWith("replace_")) {
            String placeholder = "%" + identifier.substring(8) + "%";
            getProp(player,"Replace", placeholder);
            if (properties.containsKey(player) && properties.get(player).containsKey(placeholder))
                return properties.get(player).get(placeholder);
            return loading;
        }
        if (identifier.startsWith("placeholder_")) {
            String placeholder = "%" + identifier.substring(12) + "%";
            getProp(player,"Placeholder", placeholder);
            if (properties.containsKey(player) && properties.get(player).containsKey(placeholder))
                return properties.get(player).get(placeholder);
            return loading;
        }

        getProp(player,"Property",identifier);
        if (properties.containsKey(player) && properties.get(player).containsKey(identifier)) {
            return properties.get(player).get(identifier);
        }
        return "";
    }
}