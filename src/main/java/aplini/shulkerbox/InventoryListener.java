package aplini.shulkerbox;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.block.ShulkerBox;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class InventoryListener implements Listener {
    Shulkerbox plugin;
    FileConfiguration config;
    Map<UUID, ItemStack> openShulkerBoxes = new HashMap<>();

    public InventoryListener(Shulkerbox plugin, FileConfiguration config) {
        this.config = config;
        this.plugin = plugin;
    }

    private boolean IsShulkerBox(Material material) {
        switch (material) {
            case SHULKER_BOX:
            case RED_SHULKER_BOX:
            case MAGENTA_SHULKER_BOX:
            case PINK_SHULKER_BOX:
            case PURPLE_SHULKER_BOX:
            case YELLOW_SHULKER_BOX:
            case ORANGE_SHULKER_BOX:
            case LIME_SHULKER_BOX:
            case GREEN_SHULKER_BOX:
            case CYAN_SHULKER_BOX:
            case BLUE_SHULKER_BOX:
            case LIGHT_BLUE_SHULKER_BOX:
            case LIGHT_GRAY_SHULKER_BOX:
            case GRAY_SHULKER_BOX:
            case BROWN_SHULKER_BOX:
            case BLACK_SHULKER_BOX:
            case WHITE_SHULKER_BOX: return true;
            default: return false;
        }
    }

    private void ShowEnderchest(HumanEntity player) {
        if (player.getOpenInventory().getType() == InventoryType.ENDER_CHEST) {
            player.closeInventory();
            Objects.requireNonNull(Bukkit.getServer().getPlayer(player.getUniqueId()))
                    .playSound(player, Sound.BLOCK_ENDER_CHEST_CLOSE, SoundCategory.BLOCKS, 1.0f, 1.2f);
        } else {
            player.openInventory(player.getEnderChest());
            Objects.requireNonNull(Bukkit.getServer().getPlayer(player.getUniqueId()))
                    .playSound(player, Sound.BLOCK_ENDER_CHEST_OPEN, SoundCategory.BLOCKS, 1.0f, 1.2f);
        }
    }

    private void ShowCraftingTable(HumanEntity player) {
        if (player.getOpenInventory().getType() == InventoryType.WORKBENCH) {
            return;
        }

        player.openWorkbench(null, true);
    }

    private void OpenShulkerbox(HumanEntity player, ItemStack shulkerItem) {
//        // 堆叠数量检查, 写在使用此函数的监听器中
//        if(shulkerItem.getAmount() != 1){
//            return;
//        }
        // Don't open the box if already open (avoids a duplication bug)
        if (openShulkerBoxes.containsKey(player.getUniqueId()) && openShulkerBoxes.get(player.getUniqueId()).equals(shulkerItem)) {
            return;
        }

        // 添加用于防止堆叠的NBT
        ItemMeta meta = shulkerItem.getItemMeta();
        PersistentDataContainer data = meta.getPersistentDataContainer();
        NamespacedKey nbtKey = new NamespacedKey(plugin, "__shulkerbox_plugin");
        if(!data.has(nbtKey, PersistentDataType.STRING)){
            data.set(nbtKey, PersistentDataType.STRING, String.valueOf(System.currentTimeMillis()));
            shulkerItem.setItemMeta(meta);
        }


        Inventory shulker_inventory = ((ShulkerBox)((BlockStateMeta)meta).getBlockState()).getSnapshotInventory();
        String displayName = shulkerItem.getItemMeta().getDisplayName();
        Inventory inventory;
        if (displayName.isEmpty()) {
            inventory = Bukkit.createInventory(null, InventoryType.SHULKER_BOX);
        } else {
            inventory = Bukkit.createInventory(null, InventoryType.SHULKER_BOX, displayName);
        }
        inventory.setContents(shulker_inventory.getContents());

        player.openInventory(inventory);
        Objects.requireNonNull(Bukkit.getServer().getPlayer(player.getUniqueId()))
                .playSound(player, Sound.BLOCK_SHULKER_BOX_OPEN, SoundCategory.BLOCKS, 1.0f, 1.2f);

        openShulkerBoxes.put(player.getUniqueId(), shulkerItem);
    }

    private void CloseShulkerbox(HumanEntity player) {
        ItemStack shulkerItem = openShulkerBoxes.get(player.getUniqueId());

        // 删除用于防止堆叠的NBT
//        ItemMeta meta = shulkerItem.getItemMeta();
//        PersistentDataContainer data = meta.getPersistentDataContainer();
//        NamespacedKey nbtKey = new NamespacedKey(plugin, "__shulkerbox_plugin");
//        if(data.has(nbtKey, PersistentDataType.STRING)){
//            data.remove(nbtKey);
//        }
//        shulkerItem.setItemMeta(meta);

        BlockStateMeta meta = (BlockStateMeta)shulkerItem.getItemMeta();
        ShulkerBox shulkerbox = (ShulkerBox)meta.getBlockState();
        shulkerbox.getInventory().setContents(player.getOpenInventory().getTopInventory().getContents());

        // 删除用于防止堆叠的NBT
        PersistentDataContainer data = meta.getPersistentDataContainer();
        NamespacedKey nbtKey = new NamespacedKey(plugin, "__shulkerbox_plugin");
        if(data.has(nbtKey, PersistentDataType.STRING)){
            data.remove(nbtKey);
        }

//        // 如果潜影盒为空, 则删除无用的NBT, 防止影响堆叠. 类似: https://bugs.mojang.com/browse/MC-211283
//        if(isShulkerBoxEmpty(shulkerItem)){
//            data.remove(new NamespacedKey(plugin, "tag"));
//        }

        meta.setBlockState(shulkerbox);
        shulkerItem.setItemMeta(meta);

        Objects.requireNonNull(Bukkit.getServer().getPlayer(player.getUniqueId()))
                .playSound(player, Sound.BLOCK_SHULKER_BOX_CLOSE, SoundCategory.BLOCKS, 1.0f, 1.2f);

        openShulkerBoxes.remove(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void InventoryClick(InventoryClickEvent e) {
        ItemStack item = e.getCurrentItem();
        if (e.getAction() == InventoryAction.NOTHING) {
            return;
        }

        if (!e.isRightClick() || e.isShiftClick()) {
            // Prevent moving or modifying shulker box stacks while a shulker box is open
            if (openShulkerBoxes.containsKey(e.getWhoClicked().getUniqueId()) &&
                    (
                            e.getAction() == InventoryAction.HOTBAR_SWAP
                                    || e.getAction() == InventoryAction.HOTBAR_MOVE_AND_READD
                                    || (item != null && IsShulkerBox(item.getType()))
                    )
            ) {
                e.setCancelled(true);
            }

            return;
        }

        InventoryType clickedInventory = Objects.requireNonNull(e.getClickedInventory()).getType();

        if (!(clickedInventory == InventoryType.PLAYER || clickedInventory == InventoryType.ENDER_CHEST || clickedInventory == InventoryType.CREATIVE)) {
            return;
        }

        assert item != null;
        Material itemType = item.getType();

        if (itemType == Material.ENDER_CHEST && item.getAmount() == 1) {
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(
                    plugin,
                    () -> ShowEnderchest(e.getWhoClicked())
            );
            e.setCancelled(true);
        }

        if (itemType == Material.CRAFTING_TABLE && item.getAmount() == 1) {
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(
                    plugin,
                    () -> ShowCraftingTable(e.getWhoClicked())
            );
            e.setCancelled(true);
        }

        if (IsShulkerBox(itemType)) {
            // 堆叠数量检查
            if(item.getAmount() == 1){
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(
                        plugin,
                        () -> OpenShulkerbox(e.getWhoClicked(), item)
                );
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void RightClick(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND || e.getAction() != Action.RIGHT_CLICK_AIR) {
            return;
        }

        Player player = e.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        Material itemType = item.getType();

        if (itemType == Material.ENDER_CHEST) {
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(
                    plugin,
                    () -> ShowEnderchest(player)
            );
            e.setCancelled(true);
        }

        if (itemType == Material.CRAFTING_TABLE) {
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(
                    plugin,
                    () -> ShowCraftingTable(player)
            );
            e.setCancelled(true);
        }

        if (IsShulkerBox(itemType)) {
            // 堆叠数量检查
            if(item.getAmount() == 1){
                Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(
                        plugin,
                        () -> OpenShulkerbox(player, item)
                );
                e.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void InventoryClose(InventoryCloseEvent e) {
        if (openShulkerBoxes.containsKey(e.getPlayer().getUniqueId())) {
            CloseShulkerbox(e.getPlayer());
        }
    }

    // Needs to close shulker box before items drop on death to avoid a duplication bug
    @EventHandler(priority = EventPriority.HIGHEST)
    public void Death(PlayerDeathEvent e) {
        Player player = e.getEntity();
        if (openShulkerBoxes.containsKey(player.getUniqueId())) {
            CloseShulkerbox(player);
        }
    }

    // 判断潜影盒是否为空
    public boolean isShulkerBoxEmpty(ItemStack shulkerBox) {
        ShulkerBox shulker = (ShulkerBox) ((BlockStateMeta) shulkerBox.getItemMeta()).getBlockState();
        for (ItemStack item : shulker.getInventory().getContents()) {
            if (item != null && !item.getType().isAir()) {
                return false;
            }
        }
        return true;
    }
}
