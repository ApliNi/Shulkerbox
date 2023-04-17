package aplini.shulkerbox;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class Shulkerbox extends JavaPlugin {

    FileConfiguration config = getConfig();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new InventoryListener(this, config), this);
    }
}