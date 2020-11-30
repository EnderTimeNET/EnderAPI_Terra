package net.endertime.enderapi.spigot.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.TabCompleteEvent;

import java.util.ArrayList;
import java.util.List;

public class TabCompleteListener implements Listener {

    @EventHandler
    public void onTab (TabCompleteEvent event) {
        Player player = (Player) event.getSender();
        if (commands.contains(event.getBuffer())) {
            if (event.getCompletions() != null) {
                if (!event.getCompletions().isEmpty())
                    event.getCompletions().clear();
            }
        }
    }

    private static List<String> commands = new ArrayList<>();

    public static void fillCommands() {
        commands.add("/about");
    }
}
