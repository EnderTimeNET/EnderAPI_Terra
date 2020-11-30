package net.endertime.enderapi.spigot.listener;

import net.endertime.enderapi.spigot.api.EnderAPI;
import net.endertime.enderapi.spigot.utils.ScoreBoard;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PlayerJoinListener implements Listener {

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        setScoreboard(player);

        if (!EnderAPI.getInstance().getBadlion().contains(player.getUniqueId()))
            badlion(player);

        event.setJoinMessage("§8[§2+§8] §7" + player.getName());

        for (Player vanished : EnderAPI.getInstance().getVanish())
            EnderAPI.getInstance().getScoreboard(vanished).b(player);
    }

    private void setScoreboard(final Player player) {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.getUniqueId().equals(player.getUniqueId())) {
                if (!EnderAPI.getInstance().isVanished(onlinePlayer)) {
                    ScoreBoard scoreBoard = EnderAPI.getInstance().getScoreboard(onlinePlayer);

                    scoreBoard.b();
                }
            } else {
                if (!EnderAPI.getInstance().isVanished(onlinePlayer)) {
                    ScoreBoard scoreBoard = EnderAPI.getInstance().getScoreboard(onlinePlayer);

                    scoreBoard.b(player);
                }
            }
        }
    }

    private void badlion(Player player) {
        Bukkit.getScheduler().runTaskLater(EnderAPI.getInstance().getPlugin(), () -> {

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);

            try {
                dataOutputStream.writeUTF("heartbeat");
            } catch (IOException ignored) {
                return;
            }

            player.sendPluginMessage(EnderAPI.getInstance().getPlugin(), "BungeeCord", byteArrayOutputStream.toByteArray());
        }, 20L);

    }
}
