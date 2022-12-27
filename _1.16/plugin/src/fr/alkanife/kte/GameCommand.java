package fr.alkanife.kte;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

public class GameCommand implements CommandExecutor {

    private KTE kte;

    public GameCommand(KTE kte) {
        this.kte = kte;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        //if the command sender is not op, return
        if (!commandSender.isOp())
            return true;

        //if no arguments, send usage and return
        if (strings.length == 0) {
            sendUsage(commandSender);
            return true;
        }

        //switch arguments
        switch (strings[0].toLowerCase()) {

            case "go":

                //if state different from waiting, break
                if (!kte.getState().equals(KTE.State.WAITING))
                    break;

                //if <2 players connected, break
                if (Bukkit.getOnlinePlayers().size() < 2)
                    break;

                //broadcast warning
                Bukkit.broadcastMessage("§7Et la partie commence ! Attention le serveur risque de pas apprécier.");

                //reconfigure worldborder
                WorldBorder worldBorder = Bukkit.getWorlds().get(0).getWorldBorder();
                worldBorder.setWarningDistance(25);
                worldBorder.setSize(2000);

                //TODO depracted
                /*Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();

                Objective belowName = scoreboard.registerNewObjective("belowName", "health");
                belowName.setDisplaySlot(DisplaySlot.BELOW_NAME);
                belowName.setDisplayName("§c❤§r");

                Objective tab = scoreboard.registerNewObjective("list", "health");
                tab.setDisplaySlot(DisplaySlot.PLAYER_LIST);
                tab.setDisplayName("listhealth");*/

                int amountOfSpawns = Bukkit.getOnlinePlayers().size();
                double delta = (2 * Math.PI) / amountOfSpawns;
                int angle = 0; //angle starts @ 0
                int size = 1000-50; //world border is 1000 blocks, -50 to prevent player stuck in border

                for (Player player : Bukkit.getOnlinePlayers()) {
                    double x = size * Math.sin(angle);
                    double z = size * Math.cos(angle);

                    //create teleport location
                    Location location = new Location(player.getWorld(), x, 150, z, 0, 0);

                    //if chunk is loaded
                    if (location.getChunk().load()) {

                        //disable player fly
                        player.setFlying(false);
                        player.setAllowFlight(false);

                        //reset player
                        kte.resetPlayer(player);

                        // aplly max resistance effect to player for 2 minutes
                        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 2400, 255, false, false));

                        //set gamemode to survival
                        player.setGameMode(GameMode.SURVIVAL);

                        //teleport player
                        player.teleport(location);
                    }

                    //add player to alive players
                    kte.getAlivePlayers().add(player.getUniqueId());

                    //add delta to angle
                    angle += delta;
                }

                //change state to LAUNCHED
                kte.setState(KTE.State.LAUNCHED);

                //update motd
                kte.setMOTD("§7En cours");

                //after 2 minutes, broadcast end of resistance warning & play angray wolf sound
                Bukkit.getScheduler().runTaskLater(kte, () -> {
                    Bukkit.broadcastMessage("§7Les joueurs sont désormais vulnérables.");
                    for (Player p : Bukkit.getOnlinePlayers())
                        p.playSound(p.getLocation(), Sound.ENTITY_WOLF_HOWL, 1, 1);
                }, 2400);

                break;

            case "food":

                //do nothing if not launched
                if (!kte.getState().equals(KTE.State.LAUNCHED))
                    break;

                //for all players
                for (Player player : Bukkit.getOnlinePlayers())
                    //not in spectator
                    if (!player.getGameMode().equals(GameMode.SPECTATOR))
                        //give 64 cooked rabbit
                        player.getInventory().addItem(new ItemStack(Material.COOKED_RABBIT, 64));

                //broadcast message
                Bukkit.broadcastMessage("§7les dieux ont pitié de vous, prenez cette nourriture.");

                break;

            case "heal":

                //do nothing if not launched
                if (!kte.getState().equals(KTE.State.LAUNCHED))
                    break;

                //for all players
                for (Player player : Bukkit.getOnlinePlayers())
                    //restore health
                    player.setHealth(20);

                //broadcast message
                Bukkit.broadcastMessage("§7Tous les joueurs ont été soignés.");

                break;

            case "end":

                //do nothing if not launched
                if (!kte.getState().equals(KTE.State.LAUNCHED))
                    break;

                //for all players
                for (Player player : Bukkit.getOnlinePlayers())
                    //kick
                    player.kickPlayer("§6-GG-");

                //shutdown
                Bukkit.shutdown();

                break;

            case "debug":

                commandSender.sendMessage("§7----------------------------------");
                commandSender.sendMessage("§7State: §f" + kte.getState().name());
                commandSender.sendMessage("§f");

                ComponentBuilder aliveUUIDs = new ComponentBuilder();

                for (UUID uuid : kte.getAlivePlayers())
                    aliveUUIDs.append("§f" + uuid.toString() + "\n");

                ComponentBuilder alivePlayers = new ComponentBuilder(kte.getAlivePlayers().size() + " alive players")
                        .color(ChatColor.YELLOW.asBungee())
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, aliveUUIDs.create()));

                commandSender.spigot().sendMessage(alivePlayers.create());
                commandSender.sendMessage("§7----------------------------------");

                break;

            default:

                sendUsage(commandSender);

                break;
        }

        return true;
    }

    public void sendUsage(CommandSender commandSender) {
        commandSender.sendMessage("/kte [go, end, heal, food, debug]");
    }
}
