# FrozedHider

A Minecraft plugin that uses the WorldGuard API to hide players within designated regions. When a player enters a
specified WorldGuard region, they become invisible to other players, and upon exiting, they are revealed again. This is
all done with packets, so players are not removed from the Tablist.

## Features

- Lightweight and easy to use.
- Hide players in configurable WorldGuard regions using the `hide-player` flag.

## Dependencies

- PaperSpigot (1.21.1+)
- [WorldGuard v7.0.13](https://dev.bukkit.org/projects/worldguard/files/6201343/download)

## Building

To build the project, you will need:

- Java 21 or higher
- Maven

Clone the repository and run the following command:

```bash
mvn clean package
```

## Installation

1. Download the latest release from the [releases page](https://github.com/FrozedClub/frozedhider/releases).
2. Place the plugin JAR file in your server's `plugins` folder.
3. Restart or reload your server.

## Configuration

The configuration is located in `plugins/FrozedHider/config.yml`.

```yaml
# This will show a message on chat when a player enters or exits a region.
debug: false
```

## Usage & Permissions

To hide players in a WorldGuard region, set the `hide-player` flag on the region.

Permission model (current):
- `frozedhider.see-always`: players with this permission are always visible (never hidden by the plugin).
- `frozedhider.toggle`: players with this permission can run `/togglehider` to toggle seeing all hidden players (or revert to normal visibility).

Behavior summary:
- By default, players inside a `hide-player` region are invisible to everyone.
- Players with `frozedhider.see-always` will remain visible to everyone even if they are inside a hide region.
- Players with `frozedhider.toggle` can run `/togglehider` to see all hidden players or to go back to normal visibility.

## Author

This plugin is developed and maintained by [Elb1to](https://elb1to.me).

## License

This project is licensed under the FCDL-2.0 License - see the [LICENSE](LICENSE) file for details.
