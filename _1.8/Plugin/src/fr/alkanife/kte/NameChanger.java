package fr.alkanife.kte;

import com.mojang.authlib.GameProfile;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import java.lang.reflect.Field;

public class Alkanifer implements Listener {

    private final Field nameField;

    public Alkanifer() {
        nameField = getField(GameProfile.class, "name");
    }

    @EventHandler
    public void onPreLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();

        if (!player.getName().equalsIgnoreCase("Alkanife"))
            return;

        try {
            nameField.set(((CraftPlayer) player).getProfile(), "alka");
        } catch (IllegalAccessException ignore) {}

        player.setDisplayName("alka");
    }

    private Field getField(Class<?> clazz, String name) {
        try {
            Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException | SecurityException e) {
            e.printStackTrace();
            return null;
        }
    }
}
