package fr.alkanife.kte;

import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class KTERunnable implements Runnable {

    int timer = 0;

    @Override
    public void run() {
        timer++;

        int hours = timer / 3600;
        int minutes = (timer % 3600) / 60;
        int seconds = timer % 60;

        StringBuilder timeBuilder = new StringBuilder();
        timeBuilder.append(hours < 10 ? "0" + hours : hours).append(":");
        timeBuilder.append(minutes < 10 ? "0" + minutes : minutes).append(":");
        timeBuilder.append(seconds < 10 ? "0" + seconds : seconds);

        for (Player player : Bukkit.getOnlinePlayers())
            player.setPlayerListHeaderFooter(new ComponentBuilder("§6§lKill The Enyxians" +
                    "§f   " + timeBuilder.toString()).create(), new ComponentBuilder("§f").create());
    }
}
