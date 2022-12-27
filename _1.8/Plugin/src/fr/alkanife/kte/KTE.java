package fr.alkanife.kte;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;

public class KTE extends JavaPlugin {

    private State state;
    private List<UUID> alive;
    private UUID gameMaster;

    private boolean shutdown;
    private String shutdownMessage;

    @Override
    public void onEnable() {
        state = State.LOADING;
        alive = new ArrayList<>();

        File file = new File(Paths.get("").toAbsolutePath().toString() + "/kte");
        Map<String, String> fileValues = readKeys(file);

        if (fileValues == null) {
            shutdown = true;
            shutdownMessage = "Invalid content for the kte file";
            return;
        }

        String gmValue = fileValues.get("game master");

        if (gmValue == null) {
            shutdown = true;
            shutdownMessage = "Invalid content for the kte file - game master not found";
            return;
        }

        gameMaster = UUID.fromString(gmValue);

        Bukkit.getPluginManager().registerEvents(new KTEEvents(this), this);
        Bukkit.getPluginManager().registerEvents(new Alkanifer(), this);

        getCommand("kte").setExecutor(new KTECommand(this));
    }

    public void resetPlayer(Player player) {
        player.closeInventory();
        player.getInventory().clear();
        player.setItemInHand(new ItemStack(Material.AIR));
        player.getInventory().setHelmet(null);
        player.getInventory().setChestplate(null);
        player.getInventory().setLeggings(null);
        player.getInventory().setBoots(null);

        for (PotionEffect effect : player.getActivePotionEffects())
            player.removePotionEffect(effect.getType());

        player.setMaxHealth(20);
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setSaturation(20);
        player.setExp(0);
        player.setLevel(0);
        player.setFallDistance(0);
        player.setFireTicks(0);
        player.setWalkSpeed(0.2F);
    }

    public enum State {
        LOADING,
        WAITING,
        LAUNCHED;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public List<UUID> getAlivePlayersUUID() {
        return alive;
    }

    public UUID getGameMasterUUID() {
        return gameMaster;
    }

    public boolean shutdown() {
        return shutdown;
    }

    public String getShutdownMessage() {
        return shutdownMessage;
    }

    /**
     * Example:
     *      hello: bonjour
     *      can be with spaces: yay!
     *
     * Will only take the first split
     * So, this:
     *      hello: hola: bonjour
     * will return:
     *      hello -> hola
     *
     * @param file to read
     * @return map with keys and values
     */
    private Map<String, String> readKeys(File file) {
        if (!file.exists())
            return null;

        try {
            Scanner scanner = new Scanner(file);

            List<String> lines = new ArrayList<>();

            while (scanner.hasNextLine())
                lines.add(scanner.nextLine());

            scanner.close();

            if (lines.size() == 0)
                return null;

            Map<String, String> values = new HashMap<>();

            for (String line : lines) {
                String[] splittedLine = line.split(": ");

                if (splittedLine.length >= 2)
                    values.put(splittedLine[0].toLowerCase(), splittedLine[1]);
            }

            return values;
        } catch (Exception exception) {
            return null;
        }
    }

    public void atPlayers(String message) {
        for (Player player : Bukkit.getOnlinePlayers())
            if (!player.getGameMode().equals(GameMode.SPECTATOR))
                player.sendMessage(message);
    }
}
