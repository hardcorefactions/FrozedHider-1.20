package club.frozed.hider.command;

import club.frozed.hider.FrozedHider;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ToggleHiderCommand extends Command {

    private final FrozedHider plugin;

    public ToggleHiderCommand(FrozedHider plugin) {
        super("togglehider");
        this.plugin = plugin;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String s, @NotNull String[] strings) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command.", NamedTextColor.RED));
            return true;
        }

        if (!player.hasPermission("frozedhider.toggle")) {
            player.sendMessage(Component.text("You don't have permission to use this command.", NamedTextColor.RED));
            return true;
        }

        UUID uuid = player.getUniqueId();
        boolean enabled = plugin.getToggledViewers().contains(uuid);

        if (enabled) {
            plugin.getToggledViewers().remove(uuid);
            player.sendMessage(Component.text("You will no longer see hidden players.", NamedTextColor.RED));

            for (Player target : Bukkit.getServer().getOnlinePlayers()) {
                if (target == player) continue;

                String regionId = null;
                for (ProtectedRegion region : plugin.getWorldGuardHook().getRegions(target.getUniqueId())) {
                    if (region.getFlag(plugin.getWorldGuardHook().getHidePlayerFlag()) == com.sk89q.worldguard.protection.flags.StateFlag.State.ALLOW) {
                        regionId = region.getId();
                        break;
                    }
                }

                if (!plugin.getPlayerVisibilityManager().shouldViewerSeeTarget(player, target, regionId)) {
                    player.hidePlayer(plugin, target);
                }
            }
        } else {
            plugin.getToggledViewers().add(uuid);
            player.sendMessage(Component.text("You will now see hidden players.", NamedTextColor.GREEN));

            for (Player target : Bukkit.getServer().getOnlinePlayers()) {
                if (target == player) continue;
                player.showPlayer(plugin, target);
            }
        }

        return true;
    }
}
