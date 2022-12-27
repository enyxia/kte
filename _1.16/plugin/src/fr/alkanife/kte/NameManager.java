package fr.alkanife.kte;

import com.mojang.authlib.GameProfile;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Locale;

public class NameManager implements Listener {

    private final Field nameField;
    private HashMap<String, String> names;

    public NameManager() {
        nameField = getField(GameProfile.class, "name");
        names = new HashMap<>();
        //default values
        names.put("alkanife", "alka");
        names.put("sheelecavalies", "Sheele");
    }

    @EventHandler
    public void onPreLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();

        try {
            String playerName = player.getName().toLowerCase(Locale.ROOT);

            if (names.containsKey(playerName)) {
                nameField.set(((CraftPlayer) player).getProfile(), names.get(playerName));
                player.setDisplayName(names.get(playerName));
            }
        } catch (IllegalAccessException exception) {
            exception.printStackTrace();
        }
    }

    private Field getField(Class<?> clazz, String name) {
        try {
            Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException | SecurityException exception) {
            exception.printStackTrace();
            return null;
        }
    }

    public HashMap<String, String> getNames() {
        return names;
    }
}
