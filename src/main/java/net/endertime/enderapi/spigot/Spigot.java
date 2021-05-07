package net.endertime.enderapi.spigot;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import net.endertime.enderapi.clan.ClanAPI;
import net.endertime.enderapi.database.databaseapi.DataBaseAPI;
import net.endertime.enderapi.spigot.api.PermAPI;
import net.endertime.enderapi.spigot.listener.AsyncPlayerChatListener;
import net.endertime.enderapi.spigot.listener.PlayerJoinListener;
import net.endertime.enderapi.spigot.listener.PlayerQuitListener;
import net.endertime.enderapi.spigot.listener.TabCompleteListener;
import org.bukkit.Sound;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Spigot extends JavaPlugin {

    private static Spigot plugin = null;

    public static Spigot getPlugin() {
        return plugin;
    }

    @Override
    public void onEnable() {
        plugin = this;
        DataBaseAPI.instance = new DataBaseAPI(false);
        PermAPI.instance = new PermAPI();
        ClanAPI.instance = new ClanAPI(false);

        TabCompleteListener.fillCommands();

        registerEvents(this.getServer().getPluginManager());

        registerProtocolSoundBlocker();
    }

    private void registerEvents(final PluginManager pm) {
        pm.registerEvents(new AsyncPlayerChatListener(), this);
        pm.registerEvents(new PlayerJoinListener(), this);


        pm.registerEvents(new PlayerQuitListener(), this);
        pm.registerEvents(new TabCompleteListener(), this);
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

    public static JsonParser getJsonParser() {
        return jsonParser;
    }

    private static final JsonParser jsonParser = new JsonParser();

    public JsonElement cloneJson(JsonElement cloneElement) {
        try {
            return jsonParser.parse(cloneElement.toString());
        } catch (JsonParseException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public void registerProtocolSoundBlocker() {
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(plugin, ListenerPriority.NORMAL,
                PacketType.Play.Server.NAMED_SOUND_EFFECT) {
            @Override
            public void onPacketSending(PacketEvent e) {
                if(e.getPacketType() == PacketType.Play.Server.NAMED_SOUND_EFFECT) {
                    if(e.getPacket().getSoundEffects().getValues().contains(Sound.ENTITY_PLAYER_ATTACK_NODAMAGE)
                            || e.getPacket().getSoundEffects().getValues().contains(Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK)) {
                        e.setCancelled(true);
                    }
                }
            }
        });
    }
}
