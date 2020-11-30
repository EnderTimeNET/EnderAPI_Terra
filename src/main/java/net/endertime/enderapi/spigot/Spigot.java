package net.endertime.enderapi.spigot;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.endertime.enderapi.clan.ClanAPI;
import net.endertime.enderapi.database.databaseapi.DataBaseAPI;
import net.endertime.enderapi.spigot.api.EnderAPI;
import net.endertime.enderapi.spigot.api.PermAPI;
import net.endertime.enderapi.spigot.listener.*;
import net.labymod.serverapi.Addon;
import net.labymod.serverapi.LabyModAPI;
import net.labymod.serverapi.LabyModConfig;
import net.labymod.serverapi.Permission;
import net.labymod.serverapi.bukkit.BukkitLabyModConfig;
import net.labymod.serverapi.bukkit.event.LabyModPlayerJoinEvent;
import net.labymod.serverapi.bukkit.event.MessageReceiveEvent;
import net.labymod.serverapi.bukkit.event.MessageSendEvent;
import net.labymod.serverapi.bukkit.event.PermissionsSendEvent;
import net.labymod.serverapi.bukkit.listener.PlayerJoinListener;
import net.labymod.serverapi.bukkit.utils.PacketUtils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

        labySetup();

        registerEvents(this.getServer().getPluginManager());

        registerProtocolSoundBlocker();
    }

    private void registerEvents(final PluginManager pm) {
        pm.registerEvents(new AsyncPlayerChatListener(), this);
        pm.registerEvents(new PlayerJoinListener(), this);


        pm.registerEvents(new BadlionListener(), this);
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

    private LabyModConfig labyModConfig;

    public LabyModConfig getLabyModConfig() {
        return this.labyModConfig;
    }

    private LabyModAPI api = new LabyModAPI();

    private PacketUtils packetUtils;

    public LabyModAPI getApi() {
        return this.api;
    }

    public PacketUtils getPacketUtils() {
        return this.packetUtils;
    }

    public void labySetup() {
        this.packetUtils = new PacketUtils();
        if (!this.getDataFolder().exists())
            this.getDataFolder().mkdir();
        this.labyModConfig = new BukkitLabyModConfig(new File(this.getDataFolder(), "config.yml"));
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        EnderAPI.getInstance().getPlugin().getServer().getMessenger().registerIncomingPluginChannel(this, "LABYMOD", new PluginMessageListener() {
            public void onPluginMessageReceived(String channel, final Player player, byte[] bytes) {
                ByteBuf buf = Unpooled.wrappedBuffer(bytes);
                try {
                    final String version = Spigot.this.api.readString(buf, 32767);
                    Bukkit.getScheduler().runTask((Plugin)Spigot.this, new Runnable() {
                        public void run() {
                            if (!player.isOnline())
                                return;
                            Bukkit.getPluginManager().callEvent((Event)new LabyModPlayerJoinEvent(player, version, false,
                                    0, new ArrayList()));
                        }
                    });
                } catch (RuntimeException runtimeException) {}
            }
        });
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "LMC", new PluginMessageListener() {
            public void onPluginMessageReceived(String channel, final Player player, byte[] bytes) {
                ByteBuf buf = Unpooled.wrappedBuffer(bytes);
                try {
                    final String messageKey = Spigot.this.api.readString(buf, 32767);
                    String messageContents = Spigot.this.api.readString(buf, 32767);
                    final JsonElement jsonMessage = Spigot.jsonParser.parse(messageContents);
                    Bukkit.getScheduler().runTask((Plugin)Spigot.this, new Runnable() {
                        public void run() {
                            if (!player.isOnline())
                                return;
                            if (messageKey.equals("INFO") && jsonMessage.isJsonObject()) {
                                JsonObject jsonObject = jsonMessage.getAsJsonObject();
                                String version = (jsonObject.has("version") && jsonObject.get("version").isJsonPrimitive() &&
                                        jsonObject.get("version").getAsJsonPrimitive().isString()) ? jsonObject.get("version").getAsString() : "Unknown";
                                boolean chunkCachingEnabled = false;
                                int chunkCachingVersion = 0;
                                if (jsonObject.has("ccp") && jsonObject.get("ccp").isJsonObject()) {
                                    JsonObject chunkCachingObject = jsonObject.get("ccp").getAsJsonObject();
                                    if (chunkCachingObject.has("enabled"))
                                        chunkCachingEnabled = chunkCachingObject.get("enabled").getAsBoolean();
                                    if (chunkCachingObject.has("version"))
                                        chunkCachingVersion = chunkCachingObject.get("version").getAsInt();
                                }
                                Bukkit.getPluginManager().callEvent((Event)new LabyModPlayerJoinEvent(player, version, chunkCachingEnabled, chunkCachingVersion,
                                        Addon.getAddons(jsonObject)));
                                return;
                            }
                            Bukkit.getPluginManager().callEvent((Event)new MessageReceiveEvent(player, messageKey, jsonMessage));
                        }
                    });
                } catch (RuntimeException runtimeException) {}
            }
        });
    }

    public void sendPermissions(Player player) {
        Map<Permission, Boolean> modifiedPermissions = new HashMap<>();
        modifiedPermissions.putAll(labyModConfig.permissions);
        PermissionsSendEvent sendEvent = new PermissionsSendEvent(player, modifiedPermissions, false);
        Bukkit.getPluginManager().callEvent((Event)sendEvent);
        if (!sendEvent.isCancelled() && sendEvent.getPermissions().size() > 0)
            this.packetUtils.sendPacket(player, this.packetUtils.getPluginMessagePacket("LMC", this.api.getBytesToSend(modifiedPermissions)));
    }

    public void sendServerMessage(Player player, String messageKey, JsonElement messageContents) {
        messageContents = cloneJson(messageContents);
        MessageSendEvent sendEvent = new MessageSendEvent(player, messageKey, messageContents, false);
        Bukkit.getPluginManager().callEvent((Event)sendEvent);
        if (!sendEvent.isCancelled())
            this.packetUtils.sendPacket(player, this.packetUtils.getPluginMessagePacket("LMC", this.api.getBytesToSend(messageKey, messageContents.toString())));
    }

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
