package fr.alkanife.kte;

import com.google.common.collect.Iterables;
import net.minecraft.server.v1_16_R3.MinecraftServer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class KTE extends JavaPlugin {

    //NameManager instance
    private NameManager nameManager;

    //current game state
    private State state;

    //list of alive players
    private List<UUID> alive;

    @Override
    public void onEnable() {
        //when plugin is enabled

        //initialize NameManager instance
        nameManager = new NameManager();

        //set LOADING state
        state = State.LOADING;

        //initialize alive ArrayList
        alive = new ArrayList<>();

        //register nameManager for PlayerLoginEvent
        Bukkit.getPluginManager().registerEvents(nameManager, this);
        //register other events
        Bukkit.getPluginManager().registerEvents(new Events(this), this);

        //register command
        getCommand("kte").setExecutor(new GameCommand(this));

        //update motd
        setMOTD("");

        super.onEnable();
    }

    public NameManager getNameManager() {
        return nameManager;
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

    public List<UUID> getAlivePlayers() {
        return alive;
    }

    public void setMOTD(String motd) {
        //TODO getServer() is deprecated
        MinecraftServer.getServer().setMotd("§6-KTE-\n§r" + motd);
    }

    public void resetPlayer(Player player) {
        // clear player inventory completely
        player.closeInventory();
        player.getInventory().clear();
        player.setItemInHand(new ItemStack(Material.AIR));
        player.getInventory().setHelmet(null);
        player.getInventory().setChestplate(null);
        player.getInventory().setLeggings(null);
        player.getInventory().setBoots(null);

        //remove potion effects
        for (PotionEffect effect : player.getActivePotionEffects())
            player.removePotionEffect(effect.getType());

        //set all atributes to default
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

    public void teleportToFirstConnected(Player player) {
        //if there's <2 players connected, do nothing
        if (Bukkit.getOnlinePlayers().size() < 2)
            return;

        //get the first connected player
        Player target = Iterables.get(Bukkit.getOnlinePlayers(), 0);

        //if the target is the player itself
        if (player == target) {
            //teleport the player to the second connected player
            player.teleport(Iterables.get(Bukkit.getOnlinePlayers(), 1));
        } else {
            //else teleport to the target
            player.teleport(target);
        }
    }


}
