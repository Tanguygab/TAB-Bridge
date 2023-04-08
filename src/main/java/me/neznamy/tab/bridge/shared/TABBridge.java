package me.neznamy.tab.bridge.shared;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.neznamy.tab.bridge.shared.features.TabExpansion;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RequiredArgsConstructor
public class TABBridge {

    public static final String CHANNEL_NAME = "tab:bridge-2";
    public static final String PLUGIN_VERSION = "2.0.12";
    @Getter @Setter private static TABBridge instance;

    @Getter private final Platform platform;
    @Getter private final DataBridge dataBridge;
    @Getter private final TabExpansion expansion;
    private final Map<UUID, BridgePlayer> players = new ConcurrentHashMap<>();
    private final ExecutorService executorThread = Executors.newSingleThreadExecutor();

    public void addPlayer(BridgePlayer player) {
        players.put(player.getUniqueId(), player);
    }

    public void removePlayer(BridgePlayer player) {
        players.remove(player.getUniqueId());
    }

    public Collection<BridgePlayer> getOnlinePlayers() {
        return players.values();
    }

    public BridgePlayer getPlayer(UUID uuid) {
        return players.get(uuid);
    }

    public void submitTask(Runnable task) {
        executorThread.submit(task);
    }

    public void shutdownExecutor() {
        executorThread.shutdownNow();
    }
}
