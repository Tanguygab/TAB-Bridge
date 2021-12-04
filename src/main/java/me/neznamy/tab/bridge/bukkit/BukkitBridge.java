package me.neznamy.tab.bridge.bukkit;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import github.scarsz.discordsrv.DiscordSRV;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import me.neznamy.tab.bridge.shared.DataBridge;

import java.util.HashMap;
import java.util.Map;

public class BukkitBridge extends JavaPlugin implements PluginMessageListener {

	public static final String CHANNEL_NAME = "tab:placeholders";
	public static final String ADDON_CHANNEL_NAME = "tabadditions:channel";

	protected DataBridge data;
	private BTABExpansion expansion;
	
	public void onEnable() {
		Bukkit.getMessenger().registerIncomingPluginChannel(this, CHANNEL_NAME, this);
		Bukkit.getMessenger().registerOutgoingPluginChannel(this, CHANNEL_NAME);
		Bukkit.getMessenger().registerIncomingPluginChannel(this, ADDON_CHANNEL_NAME, this);
		Bukkit.getMessenger().registerOutgoingPluginChannel(this, ADDON_CHANNEL_NAME);
		data = new BukkitDataBridge(this);
		if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
			expansion = new BTABExpansion(this);
			expansion.register();
		}
	}
	
	public void onDisable() {
		Bukkit.getMessenger().unregisterIncomingPluginChannel(this);
	}
	
	@Override
	public void onPluginMessageReceived(String channel, Player player, byte[] bytes){
		if (!channel.equalsIgnoreCase(CHANNEL_NAME))
			data.processPluginMessage(player, bytes);
		if (!channel.equalsIgnoreCase(ADDON_CHANNEL_NAME))
			processAddonPluginMessage(player, bytes);
	}

	public void processAddonPluginMessage(Player player, byte[] bytes) {
		ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
		String subChannel = in.readUTF();
		if (subChannel.equalsIgnoreCase("PlaceholderAPI")) {
			String property = in.readUTF();
			String value = in.readUTF();
			Map<String,String> map = new HashMap<>();
			if (expansion.properties.containsKey(player))
			map = expansion.properties.get(player);
			map.put(property,value);
			expansion.properties.put(player,map);
		}
		if (subChannel.equalsIgnoreCase("DiscordSRV")) {
			String msg = in.readUTF();
			String channel = in.readUTF();
			Plugin discordsrv = getServer().getPluginManager().getPlugin("DiscordSRV");
			if (discordsrv != null)
				DiscordSRV.getPlugin().processChatMessage(player, msg, channel, false);
		}
	}
}