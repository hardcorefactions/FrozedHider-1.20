package club.frozed.hider.nms.v1_20_R3;

import club.frozed.hider.FrozedHider;
import club.frozed.hider.nms.NMSAdapter;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * NMS adapter for Minecraft 1.20.3 (v1_20_R3)
 * @author jsexp
 * @since 11/10/2025
 */
public class NMSAdapter_v1_20_R3 implements NMSAdapter {

    private final FrozedHider plugin;

    public NMSAdapter_v1_20_R3(FrozedHider plugin) {
        this.plugin = plugin;
    }

    @Override
    public void keepOnTablist(Player player) {
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        ClientboundPlayerInfoUpdatePacket addPlayer = new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, serverPlayer);
        ClientboundPlayerInfoUpdatePacket updateDisplayName = new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME, serverPlayer);
        ClientboundPlayerInfoUpdatePacket updateTablist = new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_LISTED, serverPlayer);

        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            if (onlinePlayer.equals(player)) {
                continue;
            }

            ((CraftPlayer) onlinePlayer).getHandle().connection.send(addPlayer);
            ((CraftPlayer) onlinePlayer).getHandle().connection.send(updateDisplayName);
            ((CraftPlayer) onlinePlayer).getHandle().connection.send(updateTablist);

            if (plugin.isDebug()) {
                plugin.getServer().broadcastMessage("Packet sent to keep player on tablist from: " + player.getName());
            }
        }
    }

    @Override
    public void removeFromTablist(Player player) {
        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
        ClientboundPlayerInfoRemovePacket removePlayer = new ClientboundPlayerInfoRemovePacket(List.of(serverPlayer.getUUID()));

        for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
            if (onlinePlayer.equals(player)) {
                continue;
            }

            ((CraftPlayer) onlinePlayer).getHandle().connection.send(removePlayer);

            if (plugin.isDebug()) {
                plugin.getServer().broadcastMessage("Packet sent to remove player from, player: " + player.getName());
            }
        }
    }

    @Override
    public String getVersion() {
        return "v1_20_R3";
    }
}
