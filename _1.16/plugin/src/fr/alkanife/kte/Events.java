package fr.alkanife.kte;

import com.google.common.collect.Iterables;
import net.minecraft.server.v1_16_R3.MinecraftServer;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.List;

public class Events implements Listener {

    private KTE kte;

    public Events(KTE kte) {
        this.kte = kte;
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent worldLoadEvent) {
        //when a world is loaded

        World world = worldLoadEvent.getWorld();

        //if world name is not "world", return
        if (!world.getName().equalsIgnoreCase("world"))
            return;

        //disable day/night cycle
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        //set time to noon
        world.setTime(6000L);

        //disable natural regeneration
        world.setGameRule(GameRule.NATURAL_REGENERATION, false);

        //disable fire propagation
        world.setGameRule(GameRule.DO_FIRE_TICK, false);

        //disable rain cycle
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);

        //disable advancements announcements
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);

        //disable F3 coordinates
        world.setGameRule(GameRule.REDUCED_DEBUG_INFO, true);

        //disable phantom spawning
        world.setGameRule(GameRule.DO_INSOMNIA, false);

        //configure world border
        WorldBorder worldBorder = world.getWorldBorder();
        //set center to 0 0
        worldBorder.setCenter(0, 0);
        //set warning distance to 1 block
        worldBorder.setWarningDistance(1);
        //set size to 150
        worldBorder.setSize(150);

        //start game runnable
        Bukkit.getScheduler().runTaskTimer(kte, new GameRunnable(kte), 20, 20);

        //set state to WAITING
        kte.setState(KTE.State.WAITING);
    }

    @EventHandler
    public void onPreJoin(AsyncPlayerPreLoginEvent asyncPlayerPreLoginEvent) {
        //when a player tries to connect

        //if state is LOADING
        if (kte.getState().equals(KTE.State.LOADING))
            //disallow connection
            asyncPlayerPreLoginEvent.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "§cC'est pas encore prêt, reviens plus tard !");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        //when a player joins

        //delete default message
        event.setJoinMessage(null);

        Player player = event.getPlayer();

        //if player alive (means that state is to LAUNCHED)
        if (kte.getAlivePlayers().contains(player.getUniqueId())) {

            //show join message
            Bukkit.broadcastMessage("§e" + player.getDisplayName() + " joined the game");

            //end here
            return;
        }

        //if the game is launched and the player is not alive
        if (kte.getState().equals(KTE.State.LAUNCHED)) {

            //reset player
            kte.resetPlayer(player);

            //change game mode to spectator
            player.setGameMode(GameMode.SPECTATOR);

            //teleport to the first online player
            kte.teleportToFirstConnected(player);

            //end here
            return;
        }

        //change player game mode to adventure
        player.setGameMode(GameMode.ADVENTURE);

        //set allow flight and set flying
        player.setAllowFlight(true);
        player.setFlying(true);

        //reset player
        kte.resetPlayer(player);

        //get the center location
        Location zero = new Location(player.getWorld(), 0, 0, 0, 0, 0);
        //find and set the highest Y block
        zero.setY(player.getWorld().getHighestBlockYAt(0, 0) + 2);
        //teleport player
        player.teleport(zero);

        //show join message
        Bukkit.broadcastMessage("§e" + player.getDisplayName() + " joined the game");
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        //when a player leaves

        //delete default message
        event.setQuitMessage(null);

        Player player = event.getPlayer();

        //if player not in spectator
        if (!player.getGameMode().equals(GameMode.SPECTATOR))
            //show leave message
            Bukkit.broadcastMessage("§e" + player.getDisplayName() + " left the game");
    }

    @EventHandler
    public void onSpeak(AsyncPlayerChatEvent asyncPlayerChatEvent) {
        //when a player speak in chat

        //if player is in spectator
        if (asyncPlayerChatEvent.getPlayer().getGameMode().equals(GameMode.SPECTATOR))
            //cancel event
            asyncPlayerChatEvent.setCancelled(true);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent playerDeathEvent) {
        //when a player die

        //for all players
        for (Player player : Bukkit.getOnlinePlayers())
            //play wither spawn sound
            player.playSound(player.getLocation(), Sound.ENTITY_WITHER_SPAWN, 1, 1);

        Player player = playerDeathEvent.getEntity();

        //remove from alive players
        kte.getAlivePlayers().remove(player.getUniqueId());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent playerRespawnEvent) {
        //when a player respawn

        Player player = playerRespawnEvent.getPlayer();

        //reset player
        kte.resetPlayer(player);

        //change game mode to spectator
        player.setGameMode(GameMode.SPECTATOR);

        //teleport to the first connected player
        kte.teleportToFirstConnected(player);
    }

    @EventHandler
    public void onInteract(PlayerInteractAtEntityEvent event) {
        //when a player interact with an entity

        Player player = event.getPlayer();

        //if player not in spectator, return
        if (!player.getGameMode().equals(GameMode.SPECTATOR))
            return;

        //if target entity not a player, return
        if (!(event.getRightClicked() instanceof Player))
            return;

        //identify target player
        Player target = (Player) event.getRightClicked();

        PlayerInventory targetInventory = target.getInventory();

        // create an inventory of 5 lines
        Inventory inventory = Bukkit.createInventory(null, 9*5, target.getName());

        // for all items in the target inventory
        for (int i = 0; i < targetInventory.getContents().length; i++) {

            //duplicate item
            ItemStack item = targetInventory.getItem(i);

            //if not null, place it in the created inventory @ i
            if (item != null)
                inventory.setItem(i, item);
        }

        //start at 36 (9*4)
        int i = 36;

        //for a ll armor content
        for (ItemStack itemStack : targetInventory.getArmorContents()) {
            //if item not null, place it in the created inventory @ i
            if (itemStack != null)
                inventory.setItem(i, itemStack);

            i++;
        }

        //if player have active potion effects
        if (target.getActivePotionEffects().size() != 0) {

            //create an item to display active potion effects
            ItemStack itemStack = new ItemStack(Material.BREWING_STAND);
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.setDisplayName("§ePotions");

            //create lore arraylist
            List<String> lore = new ArrayList<>();

            //for all active potion effects
            for (PotionEffect potionEffect : target.getActivePotionEffects()) {
                //format potion effect duration to mm:ss
                int effectInSeconds = potionEffect.getDuration() / 20;
                int minutes = (effectInSeconds % 3600) / 60;
                int seconds = effectInSeconds % 60;

                // add a line to the item's lore
                String duration = minutes + ":" + (seconds < 10 ? "0" + seconds : seconds);
                lore.add("§e - " + potionEffect.getType().getName() + " " + potionEffect.getAmplifier() + " (" + duration + ")");
            }

            //set lore to the item meta
            itemMeta.setLore(lore);

            //set item meta
            itemStack.setItemMeta(itemMeta);

            //place item
            inventory.setItem(43, itemStack);
        }

        //open the created inventory to the player
        player.openInventory(inventory);
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent playerCommandPreprocessEvent) {
        //when a player tries to execute a command

        Player player = playerCommandPreprocessEvent.getPlayer();

        //if not operator, cancel
        if (!player.isOp()) {
            playerCommandPreprocessEvent.setCancelled(true);
            player.sendMessage("§cTu ne peux pas faire de commandes !");
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent entityDamageEvent) {
        //disable entity damage if not launched
        if (!kte.getState().equals(KTE.State.LAUNCHED))
            entityDamageEvent.setCancelled(true);
    }

    @EventHandler
    public void onTarget(EntityTargetEvent entityTargetEvent) {
        //disable entity target if not launched
        if (!kte.getState().equals(KTE.State.LAUNCHED))
            entityTargetEvent.setCancelled(true);
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent foodLevelChangeEvent) {
        //disable food level change if not launched
        if (!kte.getState().equals(KTE.State.LAUNCHED))
            foodLevelChangeEvent.setCancelled(true);
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent entityPickupItemEvent) {
        //disable entity pickup if not launched
        if (!kte.getState().equals(KTE.State.LAUNCHED))
            entityPickupItemEvent.setCancelled(true);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent playerDropItemEvent) {
        //disable player drop if not launched
        if (!kte.getState().equals(KTE.State.LAUNCHED))
            playerDropItemEvent.setCancelled(true);
    }
}
