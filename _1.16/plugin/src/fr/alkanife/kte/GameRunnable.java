package fr.alkanife.kte;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class GameRunnable implements Runnable {

    private KTE kte;

    public GameRunnable(KTE kte) {
        this.kte = kte;
    }

    int timer = 0;

    @Override
    public void run() {

        //create an empty StringBuilder
        StringBuilder timeBuilder = null;
        
        //if state is LAUNCHED
        if (kte.getState().equals(KTE.State.LAUNCHED)) {
            //add 1 to timer
            timer++;

            //convert timer into hours, minutes, and seconds
            int hours = timer / 3600;
            int minutes = (timer % 3600) / 60;
            int seconds = timer % 60;

            //initialize the StringBuilder
            timeBuilder = new StringBuilder();
            //then format to hh:mm:ss
            timeBuilder.append(hours < 10 ? "0" + hours : hours).append(":");
            timeBuilder.append(minutes < 10 ? "0" + minutes : minutes).append(":");
            timeBuilder.append(seconds < 10 ? "0" + seconds : seconds);
        }

        //for all connected players
        for (Player player : Bukkit.getOnlinePlayers())
            //update the playerlist Header & footer
            /*
            §f
            §6§lKill The Enyxians
            (if timeBuilder != null) §fhh:mm:ss
            §f
            §7Ping: §e<ping>§7 ms
            §f
            */
            player.setPlayerListHeaderFooter("§f\n" + "§6§lKill The Enyxians§f\n" + (timeBuilder == null ? "§f\n" : (timeBuilder.toString() + "§f\n§f\n")) + "§7Ping: §e" + ((CraftPlayer) player).getHandle().ping + "§7 ms\n§f", "§f");
    }
}