package pw.ollie.minerefer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import pw.ollie.minerefer.command.ReferCommandExecutor;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public final class MineRefer extends JavaPlugin {
    private final List<UUID> referred = new ArrayList<>();
    private final List<ItemStack> rewards = new ArrayList<>();

    /**
     * The plugin {@link Logger} instance.
     */
    private Logger logger = null;

    @Override
    public void onEnable() {
        logger = getLogger();

        ensureExists(getDataFolder(), true);
        saveResource("config.yml", false);

        FileConfiguration config = getConfig();
        ConfigurationSection rewards = config.getConfigurationSection("referral-rewards");
        for (String material : rewards.getKeys(false)) {
            Material mat = getMaterial(material);
            if (mat == null) {
                logger.warning("Invalid material specified in rewards config: " + material);
                continue;
            }

            int amount = rewards.getInt(material);
            this.rewards.add(new ItemStack(mat, amount));
        }

        File dataFile = new File(getDataFolder(), "data.yml");
        ensureExists(dataFile, false);
        YamlConfiguration data = YamlConfiguration.loadConfiguration(dataFile);

        List<String> rawReferred = data.getStringList("referred");
        if (rawReferred != null) {
            for (String rawReferral : rawReferred) {
                referred.add(UUID.fromString(rawReferral));
            }
        }

        getCommand("refer").setExecutor(new ReferCommandExecutor(this));
    }

    @Override
    public void onDisable() {
        File dataFile = new File(getDataFolder(), "data.yml");
        ensureExists(dataFile, false);
        YamlConfiguration data = YamlConfiguration.loadConfiguration(dataFile);

        List<String> rawReferred = new ArrayList<String>();
        for (UUID referredUuid : referred) {
            rawReferred.add(referredUuid.toString());
        }
        data.set("referred", rawReferred);
    }

    @SuppressWarnings("deprecation")
    public void giveRewards(Player player) {
        for (ItemStack reward : rewards) {
            player.getInventory().addItem(reward);
        }
        player.updateInventory();
    }

    public void addReferred(UUID uuid) {
        referred.add(uuid);
    }

    public boolean hasReferred(UUID uuid) {
        return referred.contains(uuid);
    }

    @SuppressWarnings("deprecation")
    private Material getMaterial(String arg) {
        Material mat = Material.getMaterial(arg);
        if (mat == null) {
            mat = Material.valueOf(arg);
        }
        if (mat == null) {
            try {
                mat = Material.getMaterial(Integer.parseInt(arg));
            } catch (NumberFormatException ignore) {
            }
        }
        return mat;
    }

    private void ensureExists(File file, boolean directory) {
        if (file.exists() && file.isDirectory() == directory) {
            return;
        }
        if ((directory && !file.isDirectory()) || (!directory && !file.isFile())) {
            file.delete();
        }
        if (directory) {
            file.mkdirs();
        } else {
            try {
                file.createNewFile();
            } catch (IOException ignore) {
            }
        }
    }

}
