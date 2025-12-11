package club.frozed.hider.nms.v1_20_R3;

import club.frozed.hider.FrozedHider;
import club.frozed.hider.nms.NMSAdapter;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

/**
 * NMS adapter for Minecraft 1.20.3/1.20.4 (v1_20_R3)
 * @author jsexp
 * @since 11/10/2025
 */
public class NMSAdapter_v1_20_R3 implements NMSAdapter {

    private final FrozedHider plugin;
    private boolean nmsDisabled = false;

    // Cached reflection objects
    private Method getHandleMethod;
    private Field connectionField;
    private Method sendMethod;
    private Method getUUIDMethod;
    private Class<?> packetInfoUpdateClass;
    private Class<?> packetInfoRemoveClass;
    private Class<?> actionEnum;
    private Object addPlayerAction;
    private Object updateDisplayNameAction;
    private Object updateListedAction;
    private Constructor<?> updatePacketConstructor;
    private Constructor<?> removePacketConstructor;

    public NMSAdapter_v1_20_R3(FrozedHider plugin) {
        this.plugin = plugin;
        initReflection();
    }

    private void initReflection() {
        try {
            String version = "v1_20_R3";

            // Get CraftPlayer.getHandle()
            Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + version + ".entity.CraftPlayer");
            getHandleMethod = craftPlayerClass.getMethod("getHandle");

            // Get the NMS player class (works with both EntityPlayer and ServerPlayer names)
            Class<?> nmsPlayerClass = getHandleMethod.getReturnType();

            // Get connection field (try both 'c' obfuscated and 'connection' mojang names)
            connectionField = null;
            try {
                connectionField = nmsPlayerClass.getField("c"); // Spigot obfuscated
            } catch (NoSuchFieldException e) {
                connectionField = nmsPlayerClass.getField("connection"); // Mojang mapped
            }

            // Get PlayerConnection/ServerGamePacketListenerImpl class
            Class<?> connectionClass = connectionField.getType();

            // Get send method (try both 'a' obfuscated and 'send' mojang names)
            sendMethod = null;
            for (Method method : connectionClass.getMethods()) {
                if (method.getName().equals("a") || method.getName().equals("send")) {
                    if (method.getParameterCount() == 1) {
                        sendMethod = method;
                        break;
                    }
                }
            }

            // Get UUID method
            getUUIDMethod = nmsPlayerClass.getMethod("getUniqueId");
            if (getUUIDMethod == null) {
                getUUIDMethod = nmsPlayerClass.getMethod("getBukkitEntity");
            }

            // Get packet classes
            packetInfoUpdateClass = Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket");
            packetInfoRemoveClass = Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket");

            // Get Action enum (try both names)
            try {
                actionEnum = Class.forName("net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket$Action");
            } catch (ClassNotFoundException e) {
                // Try obfuscated name
                for (Class<?> inner : packetInfoUpdateClass.getDeclaredClasses()) {
                    if (inner.isEnum()) {
                        actionEnum = inner;
                        break;
                    }
                }
            }

            // Get enum constants
            Object[] actions = actionEnum.getEnumConstants();
            for (Object action : actions) {
                String name = ((Enum<?>) action).name();
                switch (name) {
                    case "ADD_PLAYER", "a" -> addPlayerAction = action;
                    case "UPDATE_DISPLAY_NAME", "d" -> updateDisplayNameAction = action;
                    case "UPDATE_LISTED", "e" -> updateListedAction = action;
                }
            }

            // Get constructors
            for (Constructor<?> constructor : packetInfoUpdateClass.getConstructors()) {
                if (constructor.getParameterCount() == 2) {
                    updatePacketConstructor = constructor;
                    break;
                }
            }

            for (Constructor<?> constructor : packetInfoRemoveClass.getConstructors()) {
                if (constructor.getParameterCount() == 1) {
                    removePacketConstructor = constructor;
                    break;
                }
            }

        } catch (Exception e) {
            plugin.getLogger().severe("Failed to initialize NMS reflection: " + e.getMessage());
            nmsDisabled = true;
        }
    }

    @Override
    public void keepOnTablist(Player player) {
        if (nmsDisabled) return;

        try {
            Object nmsPlayer = getHandleMethod.invoke(player);

            Object addPlayer = updatePacketConstructor.newInstance(addPlayerAction, nmsPlayer);
            Object updateDisplayName = updatePacketConstructor.newInstance(updateDisplayNameAction, nmsPlayer);
            Object updateTablist = updatePacketConstructor.newInstance(updateListedAction, nmsPlayer);

            for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
                if (onlinePlayer.equals(player)) {
                    continue;
                }

                Object onlineHandle = getHandleMethod.invoke(onlinePlayer);
                Object connection = connectionField.get(onlineHandle);

                sendMethod.invoke(connection, addPlayer);
                sendMethod.invoke(connection, updateDisplayName);
                sendMethod.invoke(connection, updateTablist);

                if (plugin.isDebug()) {
                    plugin.getServer().broadcastMessage("Packet sent to keep player on tablist from: " + player.getName());
                }
            }
        } catch (Throwable e) {
            nmsDisabled = true;
            plugin.getLogger().warning("NMS tablist operations disabled: " + e.getMessage());
        }
    }

    @Override
    public void removeFromTablist(Player player) {
        if (nmsDisabled) return;

        try {
            UUID uuid = player.getUniqueId();
            Object removePlayer = removePacketConstructor.newInstance(List.of(uuid));

            for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
                if (onlinePlayer.equals(player)) {
                    continue;
                }

                Object onlineHandle = getHandleMethod.invoke(onlinePlayer);
                Object connection = connectionField.get(onlineHandle);

                sendMethod.invoke(connection, removePlayer);

                if (plugin.isDebug()) {
                    plugin.getServer().broadcastMessage("Packet sent to remove player from tablist: " + player.getName());
                }
            }
        } catch (Throwable e) {
            nmsDisabled = true;
            plugin.getLogger().warning("NMS tablist operations disabled: " + e.getMessage());
        }
    }

    @Override
    public String getVersion() {
        return "v1_20_R3";
    }
}
