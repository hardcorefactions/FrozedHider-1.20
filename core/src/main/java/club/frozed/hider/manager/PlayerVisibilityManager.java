package club.frozed.hider.manager;

import club.frozed.hider.FrozedHider;
import com.sk89q.worldguard.protection.flags.StateFlag;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

/**
 * @author Elb1to
 * @since 7/18/2025
 */
public class PlayerVisibilityManager {

	private final FrozedHider plugin;

	public PlayerVisibilityManager(FrozedHider plugin) {
		this.plugin = plugin;
	}

	public void hidePlayer(Player player, String regionId) {
		for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
			if (onlinePlayer == player || !onlinePlayer.isOnline()) {
				continue;
			}
			if (shouldViewerSeeTarget(onlinePlayer, player, regionId)) {
				if (plugin.isDebug()) {
					plugin.getServer().broadcast(Component.text("Player '" + onlinePlayer.getName() + "' can see '" + player.getName() + "' due to permissions."));
				}
				continue;
			}
			onlinePlayer.hidePlayer(plugin, player);
		}

		plugin.getNmsAdapter().keepOnTablist(player);

		if (plugin.isDebug()) {
			plugin.getServer().broadcast(Component.text("Hiding player '" + player.getName() + "' in region '" + regionId + "'."));
		}
	}

	public void showPlayer(Player player, String regionId) {
		for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
			if (onlinePlayer == player || !onlinePlayer.isOnline()) {
				continue;
			}
			onlinePlayer.showPlayer(plugin, player);
		}

		if (plugin.isDebug()) {
			plugin.getServer().broadcast(Component.text("Showing player '" + player.getName() + "' from region '" + regionId + "'."));
		}
	}

	public boolean shouldViewerSeeTarget(Player viewer, Player target, String regionId) {
		// If the target is not in a hide region, they should be visible normally
		if (regionId == null) {
			return true;
		}

		// If the target has the see-always permission, they are always visible
		if (target.hasPermission("frozedhider.see-always")) {
			return true;
		}

		// If the viewer has toggled viewing on, they should see everyone
        return plugin.getToggledViewers().contains(viewer.getUniqueId());
	}

	private boolean isPlayerInHideRegion(Player player, String regionId) {
		StateFlag flag = plugin.getWorldGuardHook().getHidePlayerFlag();
		return plugin.getWorldGuardHook().getRegions(player.getUniqueId())
				.stream()
				.anyMatch(region -> region.getId().equals(regionId) && region.getFlag(flag) == StateFlag.State.ALLOW);
	}
}
