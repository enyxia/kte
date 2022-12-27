package fr.alkanife.kte;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.util.UUID;

public class KTECommand implements CommandExecutor {

    private KTE kte;

    public KTECommand(KTE kte) {
        this.kte = kte;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (commandSender instanceof Player)
            if (!((Player) commandSender).getUniqueId().equals(kte.getGameMasterUUID()))
                return true;

        if (strings.length == 0) {
            usage(commandSender);
            return true;
        }

        switch (strings[0].toLowerCase()) {
            case "go":
                if (!kte.getState().equals(KTE.State.WAITING))
                    break;

                if (Bukkit.getOnlinePlayers().size() < 2)
                    break;

                kte.atPlayers("§7Et la partie commence ! Attention le serveur risque de pas apprécier.");

                WorldBorder worldBorder = Bukkit.getWorlds().get(0).getWorldBorder();
                worldBorder.setCenter(0, 0);
                worldBorder.setWarningDistance(25);
                worldBorder.setSize(2000);

                Scoreboard sc = Bukkit.getScoreboardManager().getMainScoreboard();
                Objective belowName = sc.registerNewObjective("belowName", "health");
                belowName.setDisplaySlot(DisplaySlot.BELOW_NAME);
                belowName.setDisplayName("§c❤§r");
                Objective tab = sc.registerNewObjective("list", "health");
                tab.setDisplaySlot(DisplaySlot.PLAYER_LIST);
                tab.setDisplayName("listhealth");

                int amountOfSpawns = Bukkit.getOnlinePlayers().size();
                double delta = (2 * Math.PI) / amountOfSpawns;
                int angle = 0;
                int size = 1000-50;

                for (Player player : Bukkit.getOnlinePlayers()) {
                    double x = size * Math.sin(angle);
                    double z = size * Math.cos(angle);

                    Location location = new Location(player.getWorld(), x, 150, z, 0, 0);

                    if (location.getChunk().load()) {
                        player.setFlying(false);
                        player.setAllowFlight(false);
                        kte.resetPlayer(player);
                        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 2400, 255, false, false));
                        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20, 1, false, false));
                        player.setGameMode(GameMode.SURVIVAL);
                        player.teleport(location);
                    }

                    kte.getAlivePlayersUUID().add(player.getUniqueId());

                    angle += delta;
                }

                kte.setState(KTE.State.LAUNCHED);

                Bukkit.getScheduler().runTaskLater(kte, () -> {
                    kte.atPlayers("§7Tu es désormais vulénrable.");
                    kte.atPlayers("§7Pro tip: fait pas de câlin à un creeper.");
                }, 2400);

                Bukkit.getScheduler().runTaskTimer(kte, new KTERunnable(), 20, 20);
                break;

            case "food":
                if (!kte.getState().equals(KTE.State.LAUNCHED))
                    break;

                for (Player player : Bukkit.getOnlinePlayers())
                    if (!player.getGameMode().equals(GameMode.SPECTATOR))
                        player.getInventory().addItem(new ItemStack(Material.COOKED_RABBIT, 64));

                kte.atPlayers("§7Oh ! De la nourriture tombée du ciel.");
                break;

            case "heal":
                if (!kte.getState().equals(KTE.State.LAUNCHED))
                    break;

                for (Player player : Bukkit.getOnlinePlayers())
                    player.setHealth(player.getMaxHealth());

                kte.atPlayers("§7Youhou, tous les joueurs ont été soigné !");
                break;

            case "end":
                if (!kte.getState().equals(KTE.State.LAUNCHED))
                    break;

                for (Player player : Bukkit.getOnlinePlayers())
                    player.kickPlayer("§6-GG-");

                Bukkit.shutdown();
                break;

            case "debug":
                commandSender.sendMessage("§7----------------------------------");
                commandSender.sendMessage("§7State: §f" + kte.getState().name());

                StringBuilder aliveBuilder = new StringBuilder();

                boolean first = true;

                for (UUID uuid : kte.getAlivePlayersUUID()) {
                    aliveBuilder.append(first ? " " : ", ").append("§f").append(uuid).append("§7");
                    if (first)
                        first = false;
                }

                commandSender.sendMessage("§7Alive players: [§f" + kte.getAlivePlayersUUID().size() + "§7]" + aliveBuilder.toString());
                commandSender.sendMessage("§7Shutdown: §f" + kte.shutdown());
                commandSender.sendMessage("§7Shutdown message: §f" + kte.getShutdownMessage());
                commandSender.sendMessage("§7Game master: §f" + kte.getGameMasterUUID());
                commandSender.sendMessage("§7----------------------------------");
                break;

            default:
                usage(commandSender);
                break;
        }

        return true;
    }

    public void usage(CommandSender commandSender) {
        commandSender.sendMessage("§c/kte [go, end, heal, food, debug]");
    }
}
