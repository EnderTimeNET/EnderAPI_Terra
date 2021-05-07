package net.endertime.enderapi.spigot.api;

import com.boydti.fawe.bukkit.wrapper.AsyncWorld;
import com.boydti.fawe.util.TaskManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.endertime.enderapi.database.databaseapi.DataBaseAPI;
import net.endertime.enderapi.database.databaseapi.mysql.MySQL;
import net.endertime.enderapi.database.databaseapi.mysql.PreparedStatement;
import net.endertime.enderapi.database.enderapi.*;
import net.endertime.enderapi.database.friends.FriendSettings;
import net.endertime.enderapi.database.friends.Friends;
import net.endertime.enderapi.database.friends.Requests;
import net.endertime.enderapi.spigot.Spigot;
import net.endertime.enderapi.spigot.utils.SkullType;
import net.endertime.enderapi.spigot.utils.*;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public class EnderAPI {

    private static EnderAPI instance = new EnderAPI();

    public static EnderAPI getInstance() {
        return instance;
    }

    public Spigot getPlugin() {
        return Spigot.getPlugin();
    }

    public String getPrefix(String prefix) {
        return "§8§l┃ " + prefix + " §8» §7";
    }

    public String getConsolePrefix(String prefix) {
        return "§8[" + prefix + "§8] §7";
    }

    private String noPerm = getPrefix("§5EnderTime") + "§7Dieser Befehl wurde nicht gefunden oder ist gesperrt!";

    private String prefix = getPrefix("§5EnderAPI");
    private String consolePrefix = getConsolePrefix("§5EnderAPI");
    private String clanPrefix = getPrefix("§6Clan");

    private String prefixFriend = "§8§l┃ §5Freunde §8» ";
    private String prefixParty = "§8§l┃ §5Party §8» ";

    private String prefixNick = "§8§l┃ §5EnderNicker §8» §7";

    private TimeDatabase timeDatabase = new TimeDatabase();
    private LobbyDatabase lobbyDatabase = new LobbyDatabase();
    private AutoReset autoReset = new AutoReset();
    private EnderDatabase enderDatabase = new EnderDatabase();
    private TeamDatabase teamDatabase = new TeamDatabase();
    private NickDatabase nickDatabase = new NickDatabase();
    private FriendSettings settings = new FriendSettings();
    private Friends friends = new Friends();
    private Requests requests = new Requests();

    private List<Player> vanish = new ArrayList<>();
    private List<UUID> vanishUUID = new ArrayList<>();
    private List<UUID> badlion = new ArrayList<>();

    private List<Player> noActionbar = new ArrayList<>();



    public ArrayList<UUID> getPointsTopThree () {
        ArrayList<UUID> list = new ArrayList<>();
        PreparedStatement ps = DataBaseAPI.getInstance().getPreparedStatement("SELECT UUID FROM ENDERAPI ORDER BY POINTS DESC LIMIT 3");

        ResultSet rs = EnderAPI.getInstance().getEnderDatabase().getMysql().runAsyncQuery(ps);

        try {
            while (rs.next()) {
                list.add(UUID.fromString(rs.getString("UUID")));
            }
        } catch (SQLException e) {
        } finally {
            EnderAPI.getInstance().getEnderDatabase().getMysql().closeConnections(ps);
        }
        return list;
    }

    public void setRanking (String gameServer) {
        MySQL mysql= DataBaseAPI.getInstance().getMySQL("STATSDATABASE");
        HashMap<Integer, UUID> rangAllTime = new HashMap<>();
        HashMap<Integer, UUID> rangMonthly = new HashMap<>();

        PreparedStatement ps = DataBaseAPI.getInstance().getPreparedStatement
                ("SELECT UUID FROM " + gameServer + "_ALLTIME ORDER BY POINTS DESC LIMIT 10");

        ResultSet rs = mysql.runAsyncQuery(ps);

        int in = 0;

        try {
            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("UUID"));
                rangAllTime.put(in, uuid);
                in++;
            }

            for (int i = 0; i < rangAllTime.keySet().size(); i++) {
                if (getPoints(mysql, gameServer + "_ALLTIME", rangAllTime.get(i)) > 0) {
                    Location loc = new Location(Bukkit.getWorld("Wartelobby"), 5 - i, 102, 51);
                    Skull skull = (Skull) loc.getBlock().getState();

                    String name = getName(rangAllTime.get(i));

                    setSkinOnBlock(rangAllTime.get(i), skull.getBlock());

                    loc.subtract(0, 1, 0);

                    if (loc.getBlock().getState() instanceof Sign) {
                        BlockState blockState = loc.getBlock().getState();
                        Sign sign = (Sign) blockState;

                        sign.setLine(1, name);
                        sign.setLine(3, "§o" + getPoints(mysql, gameServer + "_ALLTIME", rangAllTime.get(i)) + " Punkte");

                        sign.update();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            mysql.closeConnections(ps);
        }

        ps = DataBaseAPI.getInstance().getPreparedStatement("SELECT UUID FROM " + gameServer + "_MONTHLY ORDER BY POINTS DESC LIMIT 10");

        rs = mysql.runAsyncQuery(ps);

        in = 0;

        try {
            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("UUID"));
                rangMonthly.put(in, uuid);
                in++;
            }

            for (int i = 0; i < rangMonthly.keySet().size(); i++) {
                if (getPoints(mysql, gameServer + "_MONTHLY", rangMonthly.get(i)) > 0) {
                    Location loc = new Location(Bukkit.getWorld("Wartelobby"), 5 - i, 104, 51);
                    Skull skull = (Skull) loc.getBlock().getState();

                    String name = getName(rangMonthly.get(i));

                    setSkinOnBlock(rangMonthly.get(i), skull.getBlock());

                    loc.subtract(0, 1, 0);

                    if (loc.getBlock().getState() instanceof Sign) {
                        BlockState blockState = loc.getBlock().getState();
                        Sign sign = (Sign) blockState;

                        sign.setLine(1, name);
                        sign.setLine(3, "§o" + getPoints(mysql, gameServer + "_MONTHLY", rangMonthly.get(i)) + " Punkte");

                        sign.update();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            mysql.closeConnections(ps);
        }

        Location loc = new Location(Bukkit.getWorld("Wartelobby"), 6, 104, 51);
        if (loc.getBlock().getState() instanceof Sign) {
            BlockState blockState = loc.getBlock().getState();
            Sign sign = (Sign) blockState;

            String month = new SimpleDateFormat("MM").format(new Date());
            String monthString = "";
            if (month.equals("01")) {
                monthString = "Januar";
            } else if (month.equals("02")) {
                monthString = "Februar";
            } else if (month.equals("03")) {
                monthString = "März";
            } else if (month.equals("04")) {
                monthString = "April";
            } else if (month.equals("05")) {
                monthString = "Mai";
            } else if (month.equals("06")) {
                monthString = "Juni";
            } else if (month.equals("07")) {
                monthString = "Juli";
            } else if (month.equals("08")) {
                monthString = "August";
            } else if (month.equals("09")) {
                monthString = "September";
            } else if (month.equals("10")) {
                monthString = "Oktober";
            } else if (month.equals("11")) {
                monthString = "November";
            } else if (month.equals("12")) {
                monthString = "Dezember";
            }

            sign.setLine(2, "§l- " + monthString + " -");

            sign.update();
        }
    }

    private int getPoints(MySQL mysql, String tableName, UUID uuid) {
        PreparedStatement ps = new PreparedStatement("SELECT POINTS FROM " + tableName + " WHERE UUID = ?");
        ps.setString(1, uuid.toString());
        return mysql.getInt(ps, "POINTS");
    }

    private int getPoints(MySQL mysql, String tableName, String pointsName, UUID uuid) {
        PreparedStatement ps = new PreparedStatement("SELECT " + pointsName + " FROM " + tableName + " WHERE UUID = ?");
        ps.setString(1, uuid.toString());
        return mysql.getInt(ps, pointsName);
    }


    public void addCoins(Player p, int amount) {
        EnderAPI.getInstance().getEnderDatabase().updateCoins(p.getUniqueId(),
                EnderAPI.getInstance().getEnderDatabase().getCoins(p.getUniqueId()) + amount);
    }

    public void addPoints(Player p, int amount) {
        if (!p.hasPermission("teamserver.join"))
            EnderAPI.getInstance().getEnderDatabase().updatePoints(p.getUniqueId(),
                    EnderAPI.getInstance().getEnderDatabase().getPoints(p.getUniqueId()) + amount);
    }

    public boolean removeCoins(Player p, int amount) {
        if (EnderAPI.getInstance().getEnderDatabase().getCoins(p.getUniqueId()) - amount >= 0) {
            EnderAPI.getInstance().getEnderDatabase().updateCoins(p.getUniqueId(),
                    EnderAPI.getInstance().getEnderDatabase().getCoins(p.getUniqueId()) - amount);
            return true;
        }
        return false;
    }

    public boolean removePoints(Player p, int amount) {
        if (!p.hasPermission("teamserver.join")) {
            if (EnderAPI.getInstance().getEnderDatabase().getPoints(p.getUniqueId()) - amount >= 0) {
                EnderAPI.getInstance().getEnderDatabase().updatePoints(p.getUniqueId(),
                        EnderAPI.getInstance().getEnderDatabase().getPoints(p.getUniqueId()) - amount);
                return true;
            }
        }
        return false;
    }

    public void sendActionBarGlobal(String message) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            sendActionBar(p, message);
        }
    }

    public Block getTargetBlock(Player p, int range) {
        BlockIterator iter = new BlockIterator(p, range);
        Block b = iter.next();
        while (iter.hasNext()) {
            b = iter.next();
            if (b.getType().equals(Material.AIR))
                continue;
            break;
        }
        return b;
    }

    public void revivePlayer(final Player p) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(getPlugin(), new Runnable() {
            public void run() {

                PacketPlayInClientCommand packet = new PacketPlayInClientCommand(PacketPlayInClientCommand.EnumClientCommand.PERFORM_RESPAWN);
                ((CraftPlayer) p).getHandle().playerConnection.a(packet);
                p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN, (float) 0.5, 2);
            }
        }, 5);
    }

    public void unloadWorld(World world) {
        if (!world.equals(null)) {
            Bukkit.getServer().unloadWorld(world, true);
        }
    }

    public String[] playerInventoryToBase64(org.bukkit.inventory.PlayerInventory playerInventory) throws IllegalStateException {
        String content = toBase64(playerInventory);
        String armor = itemStackArrayToBase64(playerInventory.getArmorContents());

        return new String[] { content, armor };
    }

    public String itemStackArrayToBase64(ItemStack[] items) throws IllegalStateException {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            dataOutput.writeInt(items.length);

            for (int i = 0; i < items.length; i++) {
                dataOutput.writeObject(items[i]);
            }
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save item stacks.", e);
        }
    }

    public String toBase64(Inventory inventory) throws IllegalStateException {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            dataOutput.writeInt(inventory.getSize());

            for (int i = 0; i < inventory.getSize(); i++) {
                dataOutput.writeObject(inventory.getItem(i));
            }

            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("Unable to save item stacks.", e);
        }
    }

    public Inventory fromBase64(String data) throws IOException {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            Inventory inventory = Bukkit.getServer().createInventory(null, dataInput.readInt());

            for (int i = 0; i < inventory.getSize(); i++) {
                inventory.setItem(i, (ItemStack) dataInput.readObject());
            }

            dataInput.close();
            return inventory;
        } catch (ClassNotFoundException e) {
            throw new IOException("Unable to decode class type.");
        }
    }

    public ItemStack[] itemStackArrayFromBase64(String data) throws IOException {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            ItemStack[] items = new ItemStack[dataInput.readInt()];

            for (int i = 0; i < items.length; i++) {
                items[i] = (ItemStack) dataInput.readObject();
            }

            dataInput.close();
            return items;
        } catch (ClassNotFoundException e) {
            throw new IOException("Unable to decode class type.");
        }
    }

    public GameProfile getGameProfile (UUID uuid) {
        if (EnderAPI.getInstance().getEnderDatabase().isUserExists(uuid)) {
            String value = EnderAPI.getInstance().getEnderDatabase().getValue(uuid);
            String signature = EnderAPI.getInstance().getEnderDatabase().getSignature(uuid);
            GameProfile profile = new GameProfile(uuid, EnderAPI.getInstance().getEnderDatabase().getName(uuid));
            profile.getProperties().removeAll("textures");
            profile.getProperties().put("textures", new Property("textures", value, signature));
            return profile;
        }
        return null;
    }

    public GameProfile getGameProfile (UUID uuid, String value, String signature) {
        if (EnderAPI.getInstance().getEnderDatabase().isUserExists(uuid)) {
            GameProfile profile = new GameProfile(uuid, EnderAPI.getInstance().getEnderDatabase().getName(uuid));
            profile.getProperties().removeAll("textures");
            profile.getProperties().put("textures", new Property("textures", value, signature));
            return profile;
        }
        return null;
    }

    public void setSkinOnBlock(UUID uuid, Block block) {
        CreateSkulls.setBlock(block, EnderAPI.getInstance().getEnderDatabase().getSignature(uuid), EnderAPI.getInstance().getEnderDatabase().getValue(uuid));
    }

    public void createHologram(Location loc, String text) {
        Date date = new Date();
        SimpleDateFormat f = new SimpleDateFormat("dd-MM-yyyy");

        ArmorStand a = loc.getWorld().spawn(loc, ArmorStand.class);

        a.setGravity(false);
        a.setCustomNameVisible(true);
        a.setCustomName(text.replace('&', '§').replaceAll("%date%", f.format(date)));
        a.setMarker(true);
        a.setVisible(false);
        a.setSmall(true);
    }

    public void addPlayer (Player player) {
        for (Player all : Bukkit.getOnlinePlayers()) {
            if (all.getGameMode().equals(GameMode.SPECTATOR)) {
                PacketPlayOutPlayerInfo playOutPlayerInfo = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER,
                        ((CraftPlayer) all).getHandle());
                ((CraftPlayer)player).getHandle().playerConnection.sendPacket(playOutPlayerInfo);
            }
        }
    }

    public void closeServer () {
        for (Player all : Bukkit.getOnlinePlayers()) {
            all.kickPlayer(null);
        }
        Bukkit.shutdown();
    }

    public void loadWorld() {
        int worlds = 0;
        for (File files : Bukkit.getWorldContainer().listFiles()) {
            if (!files.isFile())
                if (!files.getName().equals("plugins"))
                    if (!files.getName().equals("logs"))
                        if (!files.getName().equals("config"))
                            if (!files.getName().startsWith(".")) {
                                worlds = getWorlds(worlds, files);
                            }
        }
        Bukkit.getConsoleSender().sendMessage(getConsolePrefix() + "§c" + worlds + "§2 worlds have been loaded");
    }

    public void loadWorld(List<World> list) {
        int worlds = 0;
        for (File files : Bukkit.getWorldContainer().listFiles()) {
            if (!files.isFile())
                if (!files.getName().equals("plugins"))
                    if (!files.getName().equals("logs"))
                        if (!files.getName().equals("config"))
                            if (!files.getName().startsWith(".")) {
                                worlds = getWorlds(worlds, files);
                                list.add(Bukkit.getWorld(files.getName()));
                            }
        }
        Bukkit.getConsoleSender().sendMessage(getConsolePrefix() + "§c" + worlds + "§2 worlds have been loaded");
    }

    private int getWorlds(int worlds, File files) {
        World world = Bukkit.createWorld(new WorldCreator(files.getName()));
        world.setGameRuleValue("announceAdvancements", "false");
        world.setGameRuleValue("doMobLoot", "false");
        world.setGameRuleValue("doMobSpawning", "true");
        world.setGameRuleValue("doWeatherCycle", "false");
        world.setDifficulty(Difficulty.EASY);
        Bukkit.getConsoleSender().sendMessage(getConsolePrefix() + "§2world §c" + files.getName() + " §2successfully loaded");
        worlds++;
        return worlds;
    }

    /*public void loadWorld() {
        int worlds = 0;
        for (File files : Bukkit.getWorldContainer().listFiles()) {
            if (files.isFile()) continue;
            if (files.getName().equals("plugins")) continue;
            if (files.getName().equals("logs")) continue;
            if (files.getName().equals("config")) continue;
            if (files.getName().startsWith(".")) continue;
            if (files.getName().equals("spigotbuild")) continue;
            createWorldWithProperties(files.getName(), World.Environment.NORMAL, WorldType.NORMAL);
            Bukkit.getConsoleSender().sendMessage(getConsolePrefix() + "§2world §c" + files.getName() + " §2successfully loaded");
            worlds++;
        }
        Bukkit.getConsoleSender().sendMessage(getConsolePrefix() + "§c" + worlds + "§2 worlds have been loaded");
    }

    public void loadWorldName(List<String> list) {
        int worlds = 0;
        for (File files : Bukkit.getWorldContainer().listFiles()) {
            if (files.isFile()) continue;
            if (files.getName().equals("plugins")) continue;
            if (files.getName().equals("logs")) continue;
            if (files.getName().equals("config")) continue;
            if (files.getName().startsWith(".")) continue;
            if (files.getName().equals("spigotbuild")) continue;
            createWorldWithProperties(files.getName(), World.Environment.NORMAL, WorldType.NORMAL);
            Bukkit.getConsoleSender().sendMessage(getConsolePrefix() + "§2world §c" + files.getName() + " §2successfully loaded");
            list.add(files.getName());
            worlds++;
        }
        Bukkit.getConsoleSender().sendMessage(getConsolePrefix() + "§c" + worlds + "§2 worlds have been loaded");
    }

    public void loadWorld(List<World> list) {
        int worlds = 0;
        for (File files : Bukkit.getWorldContainer().listFiles()) {
            if (files.isFile()) continue;
            if (files.getName().equals("plugins")) continue;
            if (files.getName().equals("logs")) continue;
            if (files.getName().equals("config")) continue;
            if (files.getName().startsWith(".")) continue;
            if (files.getName().equals("spigotbuild")) continue;
            createWorldWithProperties(files.getName(), World.Environment.NORMAL, WorldType.NORMAL);
            Bukkit.getConsoleSender().sendMessage(getConsolePrefix() + "§2world §c" + files.getName() + " §2successfully loaded");
            list.add(Bukkit.getWorld(files.getName()));
            worlds++;
        }
        Bukkit.getConsoleSender().sendMessage(getConsolePrefix() + "§c" + worlds + "§2 worlds have been loaded");
    }*/

    public static void createWorldWithProperties(String worldname, World.Environment environment, WorldType worldType) {
        TaskManager.IMP.async(new Runnable() {
            @Override
            public void run() {
                AsyncWorld w = AsyncWorld.create(new WorldCreator(worldname).environment(environment).type(worldType).generateStructures(true));
                w.setSpawnLocation(0, 100, 0);
                w.setStorm(false);
                w.setThundering(false);
                w.setGameRuleValue("mobGriefing", "false");
                w.setGameRuleValue("commandBlockOutput", "false");
                w.setGameRuleValue("doDaylightCycle", "false");
                w.setGameRuleValue("doEntityDrops", "false");
                w.setGameRuleValue("doFireTick", "false");
                w.setGameRuleValue("doMobLoot", "false");
                w.setGameRuleValue("doMobSpawning", "false");
                w.setGameRuleValue("doTileDrops", "true");
                w.setGameRuleValue("naturalRegeneration", "true");
                w.setGameRuleValue("logAdminCommands", "false");
                w.setGameRuleValue("randomTickSpeed", "3");
                w.setGameRuleValue("reducedDebugInfo", "false");
                w.setGameRuleValue("sendCommandFeedback", "false");
                w.setGameRuleValue("announceAdvancements", "false");
                w.setDifficulty(Difficulty.EASY);
                w.commit();
            }
        });
    }

    public void playSound(Player p, Sound sound) {
        p.playSound(p.getLocation(), sound, 0.5F, 1.0F);
    }

    public void playSound(Player p, Location location, Sound sound) {
        p.playSound(location, sound, 0.5F, 1.0F);
    }

    public void playSound(Player p, Location location, Sound sound, float pitch) {
        p.playSound(location, sound, 0.5F, pitch);
    }

    public void playSound(Player p, Sounds sounds) {
        p.playSound(p.getLocation(), sounds.getSound(), 0.5F, 1.0F);
    }

    public void playSound(Player p, Location location, Sounds sounds) {
        p.playSound(location, sounds.getSound(), 0.5F, 1.0F);
    }

    public void playSound(Player p, Location location, Sounds sounds, float pitch) {
        p.playSound(location, sounds.getSound(), 0.5F, pitch);
    }

    public void playSound(Player p, Sound sound, float pitch) {
        p.playSound(p.getLocation(), sound, 0.5F, pitch);
    }

    public void sendActionBar(Player p, String message) {
        if (!getNoActionbar().contains(p)) {
            CraftPlayer player = (CraftPlayer) p;
            IChatBaseComponent ibc = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + message + "\"}");
            PacketPlayOutTitle packet = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.ACTIONBAR, ibc, 200, 5000,
                    900);
            player.getHandle().playerConnection.sendPacket(packet);
        }
    }

    public void sendTitle(Player p, String message, Integer fadeIn, Integer displayTime, Integer fadeOut) {
        CraftPlayer player = (CraftPlayer) p;
        IChatBaseComponent ibc = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + message + "\"}");
        PacketPlayOutTitle packet = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, ibc, fadeIn, displayTime, fadeOut);
        player.getHandle().playerConnection.sendPacket(packet);
    }

    public void sendSubTitle(Player p, String message, Integer fadeIn, Integer displayTime, Integer fadeOut) {
        CraftPlayer player = (CraftPlayer) p;
        IChatBaseComponent ibc = IChatBaseComponent.ChatSerializer.a("{\"text\": \"" + message + "\"}");
        PacketPlayOutTitle packet = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, ibc, fadeIn, displayTime, fadeOut);
        player.getHandle().playerConnection.sendPacket(packet);
    }

    private String replace(String arg1, String search, String rename) {
        return getString(arg1, search, rename);
    }

    @NotNull
    public String getString(String arg1, String search, String rename) {
        String arg0 = arg1;

        int start = arg0.indexOf(search);

        while (start != -1) {
            arg0 = arg0.substring(0, start) + rename + arg0.substring(start + search.length(), arg0.length());
            start = arg0.indexOf(search, start + rename.length());
        }
        return arg0;
    }

    public String getPrefix(UUID uuid) {
        String prefix = PermAPI.getInstance().getRanks().getPrefix(PermAPI.getInstance().getGroup(uuid));
        if (prefix == null)
            return "§7";
        return prefix;
    }

    public String getRang(UUID uuid) {
        return PermAPI.getInstance().getRanks().getName(PermAPI.getInstance().getGroup(uuid));
    }

    public float round(final float value, final int frac) {
        return (float) (Math.round(Math.pow(10.0, frac) * value) / Math.pow(10.0, frac));
    }

    public double round(final double value, final int frac) {
        return Math.round(Math.pow(10.0, frac) * value) / Math.pow(10.0, frac);
    }

    public Skulls getSkull(UUID uuid) {
        return new Skulls(uuid);
    }

    public Skulls getSkull(String name) {
        return new Skulls(name);
    }

    public Skulls getSkull(String value, String non) {
        return new Skulls(value, non);
    }

    public ItemBuilder getItem(Material material) {
        return new ItemBuilder(material);
    }

    public ItemBuilder getItem(Material material, int amount) {
        return new ItemBuilder(material, amount);
    }

    public ItemBuilder getItem(Material material, int amount, int subID) {
        return new ItemBuilder(material, amount, subID);
    }

    public ItemBuilder getItem(ItemStack itemStack) {
        return new ItemBuilder(itemStack);
    }

    public Skulls getSkull(SkullType skullType) {
        return new Skulls(skullType);
    }

    public UUID getUUID (String name) {
        return getEnderDatabase().getUUID(name);
    }

    public String getName(UUID uuid) {
        return getEnderDatabase().getName(uuid);
    }

    public boolean isInTeam (Player player) {
        if (!player.hasPermission("teamserver.join"))
            return false;
        return true;
    }

    public Version getVersion (UUID uuid) {
        if (Version.versions.containsKey(uuid))
            return Version.versions.get(uuid);
        else
            return null;
    }

    public int countRows() {
        return EnderAPI.getInstance().getEnderDatabase().countRows();
    }

    public int getCoins(UUID uuid) {
        return EnderAPI.getInstance().getEnderDatabase().getCoins(uuid);
    }

    public int getPoints(UUID uuid) {
        return EnderAPI.getInstance().getEnderDatabase().getPoints(uuid);
    }

    public boolean isVanished(Player p) {
        return getVanish().contains(p);
    }

    public boolean isVanished(UUID uuid) {
        for (Player player : getVanish()) {
            if (player.getUniqueId().equals(uuid))
                return true;
        }
        return false;
    }

    public void removeOnTablist (Player p, GameProfile gameProfile) {
        MinecraftServer minecraftServer = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer worldServer = ((CraftWorld)p.getWorld()).getHandle();
        final EntityPlayer entityPlayer = new EntityPlayer(minecraftServer, worldServer, gameProfile, new PlayerInteractManager(worldServer));

        PacketPlayOutPlayerInfo packetPlayOutPlayerInfo = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER,
                entityPlayer);
        (((CraftPlayer)p).getHandle()).playerConnection.sendPacket(packetPlayOutPlayerInfo);

        packetPlayOutPlayerInfo = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_DISPLAY_NAME,
                (((CraftPlayer)p).getHandle()));
        (((CraftPlayer)p).getHandle()).playerConnection.sendPacket(packetPlayOutPlayerInfo);
    }

    public List<Player> getNoActionbar() {
        return noActionbar;
    }

    public List<Player> getVanish() {
        return vanish;
    }

    public List<UUID> getBadlion() {
        return badlion;
    }

    public List<UUID> getVanishUUID() {
        return vanishUUID;
    }

    public LobbyDatabase getLobbyDatabase() {
        return lobbyDatabase;
    }

    public String getConsolePrefix() {
        return consolePrefix;
    }

    public String getPrefix() {
        return prefix;
    }

    public AutoReset getAutoReset() {
        return autoReset;
    }

    public EnderDatabase getEnderDatabase() {
        return enderDatabase;
    }

    public NickDatabase getNickDatabase() {
        return nickDatabase;
    }

    public String getClanPrefix() {
        return clanPrefix;
    }

    public String getNoPerm() {
        return noPerm;
    }

    public String getPrefixFriend() {
        return prefixFriend;
    }

    public String getPrefixNick() {
        return prefixNick;
    }

    public String getPrefixParty() {
        return prefixParty;
    }

    public TeamDatabase getTeamDatabase() {
        return teamDatabase;
    }

    public TimeDatabase getTimeDatabase() {
        return timeDatabase;
    }

    public Friends getFriends() {
        return friends;
    }

    public FriendSettings getSettings() {
        return settings;
    }

    public Requests getRequests() {
        return requests;
    }
}
