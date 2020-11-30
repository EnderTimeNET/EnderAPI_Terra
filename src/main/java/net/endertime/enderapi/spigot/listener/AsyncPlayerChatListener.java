package net.endertime.enderapi.spigot.listener;

import net.endertime.enderapi.spigot.api.EnderAPI;
import net.endertime.enderapi.spigot.utils.ScoreBoardPrefix;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class AsyncPlayerChatListener implements Listener {

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();

        if (EnderAPI.getInstance().getVanish().contains(p)) {
            if (!e.getMessage().startsWith("/"))
                e.setCancelled(true);
            return;
        }
        e.setCancelled(true);
        for (Player all : Bukkit.getOnlinePlayers()) {
            ScoreBoardPrefix scoreBoardPrefix = new ScoreBoardPrefix(p.getUniqueId());
                    all.sendMessage(scoreBoardPrefix.getRang() + scoreBoardPrefix.getEntry() +
                            " §8» §7" + e.getMessage());
        }
    }
}
