package me.neznamy.tab.bridge.bukkit.features;

import me.neznamy.tab.bridge.bukkit.nms.DataWatcher;
import me.neznamy.tab.bridge.bukkit.nms.DataWatcherItem;
import me.neznamy.tab.bridge.bukkit.nms.NMSStorage;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * A feature to disable minecraft 1.9+ feature making tamed animals with custom names copy NameTag properties of their owner
 * This is achieved by listening to entity spawn (<1.15) / entity metadata packets and removing owner field from the DataWatcher list
 * Since 1.16 this results in client sending entity use packet twice, so we must cancel the 2nd one to prevent double toggle
 */
public class PetFix {

    //nms storage
    private final NMSStorage nms = NMSStorage.getInstance();

    //DataWatcher position of pet owner field
    private final int petOwnerPosition = getPetOwnerPosition();

    //logger of last interacts to prevent feature not working on 1.16
    private final WeakHashMap<Player, Long> lastInteractFix = new WeakHashMap<>();

    /**
     * Returns position of pet owner field based on server version
     * @return position of pet owner field based on server version
     */
    private int getPetOwnerPosition() {
        if (nms.getMinorVersion() >= 17) {
            //1.17.x, 1.18.x
            return 18;
        } else if (nms.getMinorVersion() >= 15) {
            //1.15.x, 1.16.x
            return 17;
        } else if (nms.getMinorVersion() >= 14) {
            //1.14.x
            return 16;
        } else if (nms.getMinorVersion() >= 10) {
            //1.10.x - 1.13.x
            return 14;
        } else {
            //1.9.x
            return 13;
        }
    }

    /**
     * Cancels a packet if previous one arrived with no delay to prevent double toggle on 1.16
     * @throws	ReflectiveOperationException
     * 			if thrown by reflective operation
     */
    public boolean onPacketReceive(Player sender, Object packet) throws ReflectiveOperationException {
        if (nms == null) return false;
        if (nms.PacketPlayInUseEntity.isInstance(packet) && isInteract(nms.PacketPlayInUseEntity_ACTION.get(packet))) {
            if (System.currentTimeMillis() - lastInteractFix.getOrDefault(sender, 0L) < 160) {
                //last interact packet was sent right now, cancelling to prevent double-toggle due to this feature enabled
                return true;
            }
            //this is the first packet, saving player so the next packet can be cancelled
            lastInteractFix.put(sender, System.currentTimeMillis());
        }
        return false;
    }

    private boolean isInteract(Object action) {
        if (nms.getMinorVersion() >= 17) {
            return nms.PacketPlayInUseEntity$d.isInstance(action);
        } else {
            return action.toString().equals("INTERACT");
        }
    }

    /**
     * Removes pet owner field from DataWatcher
     * @throws	ReflectiveOperationException
     * 			if thrown by reflective operation
     */
    @SuppressWarnings("unchecked")
    public void onPacketSend(Player receiver, Object packet) throws ReflectiveOperationException {
        if (nms == null) return;
        if (nms.PacketPlayOutEntityMetadata.isInstance(packet)) {
            Object removedEntry = null;
            List<Object> items = (List<Object>) nms.PacketPlayOutEntityMetadata_LIST.get(packet);
            if (items == null) return;
            try {
                for (Object item : items) {
                    if (item == null) continue;
                    if (nms.DataWatcherObject_SLOT.getInt(nms.DataWatcherItem_TYPE.get(item)) == petOwnerPosition) {
                        Object value = nms.DataWatcherItem_VALUE.get(item);
                        if (value instanceof java.util.Optional || value instanceof com.google.common.base.Optional) {
                            removedEntry = item;
                        }
                    }
                }
            } catch (ConcurrentModificationException e) {
                //no idea how can this list change in another thread since it's created for the packet but whatever, try again
                onPacketSend(receiver, packet);
            }
            if (removedEntry != null) items.remove(removedEntry);
        } else if (nms.PacketPlayOutSpawnEntityLiving.isInstance(packet) && nms.PacketPlayOutSpawnEntityLiving_DATAWATCHER != null) {
            //<1.15
            DataWatcher watcher = DataWatcher.fromNMS(nms.PacketPlayOutSpawnEntityLiving_DATAWATCHER.get(packet));
            DataWatcherItem petOwner = watcher.getItem(petOwnerPosition);
            if (petOwner != null && (petOwner.getValue() instanceof java.util.Optional || petOwner.getValue() instanceof com.google.common.base.Optional)) {
                watcher.removeValue(petOwnerPosition);
                nms.setField(packet, nms.PacketPlayOutSpawnEntityLiving_DATAWATCHER, watcher.toNMS());
            }
        }
    }
}