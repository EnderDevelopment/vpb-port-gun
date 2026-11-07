package com.enderdevelopment.plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public final class Main extends JavaPlugin implements Listener, CommandExecutor {
    private static final int MAX_DISTANCE = 32;
    private static final long COOLDOWN_MILLIS = 1200L;
    private NamespacedKey gunKey;
    private NamespacedKey cooldownKey;

    @Override
    public void onEnable() {
        gunKey = new NamespacedKey(this, "port_gun");
        cooldownKey = new NamespacedKey(this, "cooldown_until");
        Bukkit.getPluginManager().registerEvents(this, this);
        if (getCommand("portgun") != null) {
            getCommand("portgun").setExecutor(this);
        }
    }

    @Override
    public void onDisable() {
    }

    @EventHandler
    public void onUse(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND || !event.getAction().isRightClick()) {
            return;
        }
        Player player = event.getPlayer();
        if (!isPortGun(player.getInventory().getItemInMainHand())) {
            return;
        }
        event.setCancelled(true);
        if (!player.hasPermission("portgun.use")) {
            player.sendMessage(Component.text("You cannot use the port gun.", NamedTextColor.RED));
            return;
        }
        long now = System.currentTimeMillis();
        Long cooldownUntil = player.getPersistentDataContainer().get(cooldownKey, PersistentDataType.LONG);
        if (cooldownUntil != null && cooldownUntil > now) {
            long remaining = (cooldownUntil - now + 99L) / 100L;
            player.sendActionBar(Component.text("Recharge: " + (remaining / 10.0) + "s", NamedTextColor.YELLOW));
            return;
        }
        LocationTarget target = findTarget(player);
        if (target == null) {
            player.sendActionBar(Component.text("No safe landing spot.", NamedTextColor.RED));
            return;
        }
        player.getPersistentDataContainer().set(cooldownKey, PersistentDataType.LONG, now + COOLDOWN_MILLIS);
        player.teleport(target.location());
        player.sendActionBar(Component.text("Ported " + target.distance() + " blocks", NamedTextColor.AQUA));
    }

    private LocationTarget findTarget(Player player) {
        RayTraceResult hit = player.getWorld().rayTraceBlocks(
                player.getEyeLocation(),
                player.getEyeLocation().getDirection(),
                MAX_DISTANCE,
                org.bukkit.FluidCollisionMode.NEVER,
                true);
        if (hit == null || hit.getHitBlock() == null || hit.getHitBlockFace() == null) {
            return null;
        }
        Vector face = hit.getHitBlockFace().getDirection();
        var block = hit.getHitBlock().getLocation().add(face.getX(), face.getY(), face.getZ()).getBlock();
        if (!block.isEmpty() || !block.getRelative(0, 1, 0).isEmpty()) {
            return null;
        }
        double distance = player.getLocation().distance(block.getLocation());
        return new LocationTarget(
                block.getLocation().add(0.5, 0, 0.5).setDirection(player.getLocation().getDirection()),
                (int) Math.ceil(distance));
    }

    private boolean isPortGun(ItemStack item) {
        if (item == null || item.getType() != Material.CROSSBOW || !item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().has(gunKey, PersistentDataType.BYTE);
    }

    private ItemStack createPortGun() {
        ItemStack item = new ItemStack(Material.CROSSBOW);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("Port Gun", NamedTextColor.AQUA));
        meta.lore(java.util.List.of(
                Component.text("Right-click a nearby block to teleport.", NamedTextColor.GRAY),
                Component.text("Lightweight mode: no particles or entities.", NamedTextColor.DARK_GRAY)));
        meta.getPersistentDataContainer().set(gunKey, PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("portgun")) {
            return false;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can receive the port gun.");
            return true;
        }
        if (!player.hasPermission("portgun.give")) {
            player.sendMessage(Component.text("You do not have permission.", NamedTextColor.RED));
            return true;
        }
        player.getInventory().addItem(createPortGun());
        player.sendMessage(Component.text("Port gun received.", NamedTextColor.AQUA));
        return true;
    }

    private record LocationTarget(org.bukkit.Location location, int distance) {
    }
}
