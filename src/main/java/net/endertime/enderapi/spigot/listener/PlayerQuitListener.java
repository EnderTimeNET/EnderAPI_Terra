package net.endertime.enderapi.spigot.listener;

import net.endertime.enderapi.spigot.api.EnderAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent event) {
        final Player player = event.getPlayer();


        event.setQuitMessage("§8[§4-§8] §7" + player.getName());

        if (EnderAPI.getInstance().getBadlion().contains(player.getUniqueId())) {
            EnderAPI.getInstance().getBadlion().remove(player.getUniqueId());
        }

        if (EnderAPI.getInstance().getNoActionbar().contains(player)) {
            EnderAPI.getInstance().getNoActionbar().remove(player);
        }
    }
}
