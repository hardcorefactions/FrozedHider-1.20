package club.frozed.hider;

import club.frozed.hider.hook.WorldGuardHook;
import club.frozed.hider.listener.PlayerListener;
import club.frozed.hider.manager.PlayerVisibilityManager;
import club.frozed.hider.nms.NMSAdapter;
import club.frozed.hider.command.ToggleHiderCommand;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Elb1to
 * @since 7/18/2025
 */
@Getter
public class FrozedHider extends JavaPlugin {

	private final WorldGuardHook worldGuardHook;
	private PlayerVisibilityManager playerVisibilityManager;
	private NMSAdapter nmsAdapter;
	private boolean debug;

	private final Set<UUID> toggledViewers = ConcurrentHashMap.newKeySet();

	{
		worldGuardHook = new WorldGuardHook(this);
	}

	@Override
	public void onLoad() {
		worldGuardHook.registerFlag();
	}

	@Override
	public void onEnable() {
		saveDefaultConfig();
		this.debug = getConfig().getBoolean("debug", false);

		// Detect server version and load appropriate NMS adapter
		String version = getServerVersion();
		NMSAdapter adapter = createNMSAdapter(version);

		if (adapter == null) {
			getLogger().severe("Unsupported server version: " + version);
			getLogger().severe("Supported versions: 1.20.3, 1.21.1 - 1.21.8");
			getServer().getPluginManager().disablePlugin(this);
			return;
		}

		this.nmsAdapter = adapter;
		getLogger().info("Loaded NMS adapter for version: " + version);

		this.worldGuardHook.init();
		this.playerVisibilityManager = new PlayerVisibilityManager(this);

		getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

		Bukkit.getServer().getCommandMap().register("togglehider", new ToggleHiderCommand(this));
		getLogger().info("FrozedHider has been enabled using NMS version: " + nmsAdapter.getVersion());
	}

	private String getServerVersion() {
		String packageName = Bukkit.getServer().getClass().getPackage().getName();
		String version = packageName.substring(packageName.lastIndexOf('.') + 1);

		if (version.equals("craftbukkit")) {
			String bukkitVersion = Bukkit.getBukkitVersion();
			if (bukkitVersion.startsWith("1.20.")) {
				return "v1_20_R3";
			} else if (bukkitVersion.startsWith("1.21.1")) {
				return "v1_21_R1";
			} else if (bukkitVersion.startsWith("1.21.3")) {
				return "v1_21_R2";
			} else if (bukkitVersion.startsWith("1.21.4")) {
				return "v1_21_R3";
			} else if (bukkitVersion.startsWith("1.21.5")) {
				return "v1_21_R4";
			} else if (bukkitVersion.startsWith("1.21.6") || bukkitVersion.startsWith("1.21.7") || bukkitVersion.startsWith("1.21.8")) {
				return "v1_21_R5";
			}
		}

		return version;
	}

	private NMSAdapter createNMSAdapter(String version) {
		try {
			return switch (version) {
				case "v1_20_R3" -> {
					Class<?> adapterClass1 = Class.forName("club.frozed.hider.nms.v1_20_R3.NMSAdapter_v1_20_R3");
					yield (NMSAdapter) adapterClass1.getConstructor(FrozedHider.class).newInstance(this);
				}
				case "v1_21_R1" -> {
					Class<?> adapterClass2 = Class.forName("club.frozed.hider.nms.v1_21_R1.NMSAdapter_v1_21_R1");
					yield (NMSAdapter) adapterClass2.getConstructor(FrozedHider.class).newInstance(this);
				}
				case "v1_21_R2" -> {
					Class<?> adapterClass3 = Class.forName("club.frozed.hider.nms.v1_21_R2.NMSAdapter_v1_21_R2");
					yield (NMSAdapter) adapterClass3.getConstructor(FrozedHider.class).newInstance(this);
				}
				case "v1_21_R3" -> {
					Class<?> adapterClass4 = Class.forName("club.frozed.hider.nms.v1_21_R3.NMSAdapter_v1_21_R3");
					yield (NMSAdapter) adapterClass4.getConstructor(FrozedHider.class).newInstance(this);
				}
				case "v1_21_R4" -> {
					Class<?> adapterClass5 = Class.forName("club.frozed.hider.nms.v1_21_R4.NMSAdapter_v1_21_R4");
					yield (NMSAdapter) adapterClass5.getConstructor(FrozedHider.class).newInstance(this);
				}
				case "v1_21_R5" -> {
					Class<?> adapterClass6 = Class.forName("club.frozed.hider.nms.v1_21_R5.NMSAdapter_v1_21_R5");
					yield (NMSAdapter) adapterClass6.getConstructor(FrozedHider.class).newInstance(this);
				}
				default -> null;
			};
		} catch (Exception e) {
			getLogger().severe("Failed to load NMS adapter for version " + version + ": " + e.getMessage());
			return null;
		}
	}
}
