package fr.alkanife.kte;

import net.md_5.bungee.api.chat.ComponentBuilder;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.List;

public class KTEEvents implements Listener {

    private KTE kte;

    public KTEEvents(KTE kte) {
        this.kte = kte;
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        World world = event.getWorld();

        if (!world.getName().equalsIgnoreCase("world"))
            return;

        if (kte.shutdown()) {
            Bukkit.getConsoleSender().sendMessage("§cShutting down [Cause: " + kte.getShutdownMessage() + "]");
            Bukkit.shutdown();
            return;
        }

        world.setGameRuleValue("doDaylightCycle", "false");
        world.setGameRuleValue("naturalRegeneration", "false");
        world.setGameRuleValue("doFireTick", "false");
        world.setTime(6000L);

        WorldBorder worldBorder = world.getWorldBorder();
        worldBorder.setCenter(0, 0);
        worldBorder.setWarningDistance(1);
        worldBorder.setSize(150);

        MinecraftServer.getServer().setMotd("§6-KTE-");

        kte.setState(KTE.State.WAITING);
    }

    @EventHandler
    public void onPreJoin(AsyncPlayerPreLoginEvent event) {
        if (kte.getState().equals(KTE.State.LOADING))
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "§cLe monde n'est pas encore totalement chargé, reviens plus tard.");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);

        Player player = event.getPlayer();

        player.setPlayerListHeaderFooter(new ComponentBuilder("§6§lKill The Enyxians").create(), new ComponentBuilder("§f").create());

        if (kte.getAlivePlayersUUID().contains(player.getUniqueId())) {
            Bukkit.broadcastMessage("§e" + player.getDisplayName() + " joined the game");
            return;
        }

        if (kte.getState().equals(KTE.State.LAUNCHED)) {
            player.setGameMode(GameMode.SPECTATOR);
            return;
        }

        player.setGameMode(GameMode.ADVENTURE);
        player.setAllowFlight(true);
        player.setFlying(true);

        kte.resetPlayer(player);

        Location zero = new Location(player.getWorld(), 0, 0, 0, 0, 0);
        zero.setY(player.getWorld().getHighestBlockYAt(0, 0) + 2);
        player.teleport(zero);

        Bukkit.broadcastMessage("§e" + player.getDisplayName() + " joined the game");
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);

        Player player = event.getPlayer();

        if (!player.getGameMode().equals(GameMode.SPECTATOR))
            Bukkit.broadcastMessage("§e" + player.getDisplayName() + " left the game");
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        for (Player player : Bukkit.getOnlinePlayers())
            player.playSound(player.getLocation(), Sound.WITHER_SPAWN, 1, 1);

        Player player = event.getEntity();

        player.setHealth(20.0);
        player.setGameMode(GameMode.SPECTATOR);

        kte.getAlivePlayersUUID().remove(player.getUniqueId());
    }

    @EventHandler
    public void onInteract(PlayerInteractAtEntityEvent event) {
        Player player = event.getPlayer();

        if (!player.getGameMode().equals(GameMode.SPECTATOR))
            return;

        if (!(event.getRightClicked() instanceof Player))
            return;

        Player target = (Player) event.getRightClicked();

        PlayerInventory targetInventory = target.getInventory();
        Inventory inventory = Bukkit.createInventory(null, 9*5, target.getName());

        for (int i = 0; i < targetInventory.getContents().length; i++) {
            ItemStack item = targetInventory.getItem(i);
            if (item != null)
                inventory.setItem(i, item);
        }

        int i = 36;
        for (ItemStack itemStack : targetInventory.getArmorContents()) {
            if (itemStack != null)
                inventory.setItem(i, itemStack);
            i++;
        }

        if (target.getActivePotionEffects().size() != 0) {
            ItemStack itemStack = new ItemStack(Material.BREWING_STAND_ITEM);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName("§ePotions");

            List<String> lore = new ArrayList<>();

            for (PotionEffect potionEffect : target.getActivePotionEffects()) {
                int effectInSeconds = potionEffect.getDuration()/20;
                int minutes = (effectInSeconds % 3600) / 60;
                int seconds = effectInSeconds % 60;

                String duration = minutes + ":" +
                        (seconds < 10 ? "0" + seconds : seconds);
                lore.add("§e - " + potionEffect.getType().getName() + " " + potionEffect.getAmplifier() + " (" + duration + ")");
            }

            itemMeta.setLore(lore);
            itemStack.setItemMeta(itemMeta);
            inventory.setItem(43, itemStack);
        }

        player.openInventory(inventory);
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (!event.getMessage().toLowerCase().startsWith("/kte"))
            event.setCancelled(true);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!kte.getState().equals(KTE.State.LAUNCHED))
            event.setCancelled(true);
    }

    @EventHandler
    public void onTarget(EntityTargetEvent event) {
        if (!kte.getState().equals(KTE.State.LAUNCHED))
            event.setCancelled(true);
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (!kte.getState().equals(KTE.State.LAUNCHED))
            event.setCancelled(true);
    }

    @EventHandler
    public void onPickup(PlayerPickupItemEvent event) {
        if (!kte.getState().equals(KTE.State.LAUNCHED))
            event.setCancelled(true);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (!kte.getState().equals(KTE.State.LAUNCHED))
            event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!kte.getState().equals(KTE.State.LAUNCHED))
            event.setCancelled(true);
    }

    @EventHandler
    public void onWeather(WeatherChangeEvent event) {
        event.setCancelled(true);
    }
}
