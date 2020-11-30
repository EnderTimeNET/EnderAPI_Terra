package net.endertime.enderapi.spigot.utils;

import net.endertime.enderapi.clan.ClanAPI;
import net.endertime.enderapi.spigot.api.EnderAPI;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScoreBoard {

    public static Map<Player, ScoreBoard> scoreboards = new HashMap<Player, ScoreBoard>();

    private Player player;
    private ScoreboardObjective object;
    private Scoreboard scoreboardPSide;
    private Scoreboard scoreboardPTab;
    private PacketPlayOutScoreboardDisplayObjective sidebar;
    private PacketPlayOutScoreboardObjective createPacket;
    private PacketPlayOutScoreboardObjective removePacket;
    private List<ScoreboardScore> scores;
    private String displayName = "§1";

    public ScoreBoard(Player player) {
        this.player = player;
        this.scores = new ArrayList<>();
        getScoreboards().put(player, this);

        this.scoreboardPTab = new Scoreboard();

        this.object = scoreboardPTab.registerObjective("dummy", IScoreboardCriteria.b);
    }

    public void addScore(int arg0, String arg1) {
        ScoreboardScore scoreboardScore = new ScoreboardScore(getScoreboardPSide(), getObject(), arg1);
        scoreboardScore.setScore(arg0);
        getScores().add(scoreboardScore);
    }

    public void removeAllScores() {
        int s = getScores().size();
        for (int i = 0; i < s; i++) {
            getScores().remove(0);
        }
    }

    private void sendScores() {
        for (ScoreboardScore scoreboardScore : getScores()) {
            PacketPlayOutScoreboardScore packetScoreboardScore = new PacketPlayOutScoreboardScore(scoreboardScore);
            sendPacket(packetScoreboardScore);
        }
    }

    private ScoreboardObjective getObject() {
        return object;
    }

    private List<ScoreboardScore> getScores() {
        return scores;
    }

    public static Map<Player, ScoreBoard> getScoreboards() {
        return scoreboards;
    }

    public PacketPlayOutScoreboardDisplayObjective getSidebar() {
        return sidebar;
    }

    public Player getPlayer() {
        return player;
    }

    public void registerScoreBoard(String args0) {
        this.scoreboardPSide = new Scoreboard();

        this.object = scoreboardPSide.registerObjective("dummy", IScoreboardCriteria.b);

        this.displayName = args0;
        getObject().setDisplayName(args0);

        this.createPacket = new PacketPlayOutScoreboardObjective(getObject(), 0);
        this.removePacket = new PacketPlayOutScoreboardObjective(getObject(), 1);

        this.sidebar = new PacketPlayOutScoreboardDisplayObjective(1, getObject());
    }

    public void registerScoreBoard() {
        this.scoreboardPSide = new Scoreboard();

        this.object = scoreboardPSide.registerObjective("dummy", IScoreboardCriteria.b);

        getObject().setDisplayName(this.displayName);

        this.createPacket = new PacketPlayOutScoreboardObjective(getObject(), 0);
        this.removePacket = new PacketPlayOutScoreboardObjective(getObject(), 1);

        this.sidebar = new PacketPlayOutScoreboardDisplayObjective(1, getObject());
    }

    public void a() {
        b();
        updateSidebar();
    }

    public void updateSidebar() {
        sendPacket(getRemovePacket());
        sendPacket(getCreatePacket());

        sendPacket(getSidebar());
        sendScores();
    }

    public void b() {
        setAllPlayers();

        sendTeams();

        for (Player all : Bukkit.getOnlinePlayers()) {
            ScoreBoardPrefix scoreBoardPrefix = new ScoreBoardPrefix(all.getUniqueId());
            if (ClanAPI.getInstance().getMember().isUserExists(all.getUniqueId())) {
                EnderAPI.getInstance().setSubtitle(player, all.getUniqueId(), scoreBoardPrefix.getLabySuffix());
            } else {
                EnderAPI.getInstance().setSubtitle(player, all.getUniqueId(), null);
            }
        }
    }

    public void b (Player all) {
        setAllPlayers(all);
    }

    public Scoreboard getScoreboardPSide() {
        return scoreboardPSide;
    }

    public Scoreboard getScoreboardPTab() {
        return scoreboardPTab;
    }

    private void setAllPlayers() {
        for (Player all : Bukkit.getOnlinePlayers()) {

            ScoreBoardPrefix scoreBoardPrefix = new ScoreBoardPrefix(all.getUniqueId());

            if (scoreBoardPrefix != null) {
                if (getScoreboardPTab().getTeam(scoreBoardPrefix.getTeam()) == null) {
                    getScoreboardPTab().createTeam(scoreBoardPrefix.getTeam());
                    getScoreboardPTab().addPlayerToTeam(scoreBoardPrefix.getEntry(), scoreBoardPrefix.getTeam());
                    getScoreboardPTab().getTeam(scoreBoardPrefix.getTeam()).setPrefix(scoreBoardPrefix.getRang());
                    getScoreboardPTab().getTeam(scoreBoardPrefix.getTeam()).setSuffix(scoreBoardPrefix.getSuffix());

                    sendPacket(new PacketPlayOutScoreboardTeam(getScoreboardPTab().getTeam(scoreBoardPrefix.getTeam()), 0));

                } else {
                    if (getScoreboardPTab().getTeam(scoreBoardPrefix.getTeam()).getPlayerNameSet().contains(scoreBoardPrefix.getEntry())) {
                        getScoreboardPTab().removePlayerFromTeam(scoreBoardPrefix.getEntry(), getScoreboardPTab().getTeam(scoreBoardPrefix.getTeam()));
                    }
                }
                getScoreboardPTab().addPlayerToTeam(scoreBoardPrefix.getEntry(), scoreBoardPrefix.getTeam());
                getScoreboardPTab().getTeam(scoreBoardPrefix.getTeam()).setPrefix(scoreBoardPrefix.getRang());
                getScoreboardPTab().getTeam(scoreBoardPrefix.getTeam()).setSuffix(scoreBoardPrefix.getSuffix());

            }
        }
    }

    private void setAllPlayers(Player all) {
        ScoreBoardPrefix scoreBoardPrefix = new ScoreBoardPrefix(all.getUniqueId());

        if (scoreBoardPrefix != null) {
            if (getScoreboardPTab().getTeam(scoreBoardPrefix.getTeam()) == null) {
                getScoreboardPTab().createTeam(scoreBoardPrefix.getTeam());
                getScoreboardPTab().addPlayerToTeam(scoreBoardPrefix.getEntry(), scoreBoardPrefix.getTeam());
                getScoreboardPTab().getTeam(scoreBoardPrefix.getTeam()).setPrefix(scoreBoardPrefix.getRang());
                getScoreboardPTab().getTeam(scoreBoardPrefix.getTeam()).setSuffix(scoreBoardPrefix.getSuffix());

            } else {
                if (getScoreboardPTab().getTeam(scoreBoardPrefix.getTeam()).getPlayerNameSet().contains(scoreBoardPrefix.getEntry())) {
                    getScoreboardPTab().removePlayerFromTeam(scoreBoardPrefix.getEntry(), getScoreboardPTab().getTeam(scoreBoardPrefix.getTeam()));
                }
            }
            getScoreboardPTab().addPlayerToTeam(scoreBoardPrefix.getEntry(), scoreBoardPrefix.getTeam());
            getScoreboardPTab().getTeam(scoreBoardPrefix.getTeam()).setPrefix(scoreBoardPrefix.getRang());
            getScoreboardPTab().getTeam(scoreBoardPrefix.getTeam()).setSuffix(scoreBoardPrefix.getSuffix());
        }

        sendPacket(new PacketPlayOutScoreboardTeam(getScoreboardPTab().getTeam(scoreBoardPrefix.getTeam()), 1));
        sendPacket(new PacketPlayOutScoreboardTeam(getScoreboardPTab().getTeam(scoreBoardPrefix.getTeam()), 0));

        if (!EnderAPI.getInstance().isVanished(all)) {
            if (ClanAPI.getInstance().getMember().isUserExists(all.getUniqueId())) {
                EnderAPI.getInstance().setSubtitle(player, all.getUniqueId(), scoreBoardPrefix.getLabySuffix());
            } else {
                EnderAPI.getInstance().setSubtitle(player, all.getUniqueId(), null);
            }
        } else {
            EnderAPI.getInstance().setSubtitle(player, all.getUniqueId(), null);
        }
        if (EnderAPI.getInstance().isVanished(all)) {
            if (EnderAPI.getInstance().isInTeam(player)) {
                (((CraftPlayer)player).getHandle()).playerConnection.sendPacket(
                        new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_DISPLAY_NAME,
                                (((CraftPlayer)all).getHandle())));
            }
        } else {
            (((CraftPlayer)player).getHandle()).playerConnection.sendPacket(
                    new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_DISPLAY_NAME,
                            (((CraftPlayer)all).getHandle())));
        }
    }

    private void sendTeams() {
        for (ScoreboardTeam team : getScoreboardPTab().getTeams()) {
            sendPacket(new PacketPlayOutScoreboardTeam(team, 1));
            sendPacket(new PacketPlayOutScoreboardTeam(team, 0));
        }
    }

    private void sendPacket(Packet<?> packet) {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }

    private PacketPlayOutScoreboardObjective getCreatePacket() {
        return createPacket;
    }

    private PacketPlayOutScoreboardObjective getRemovePacket() {
        return removePacket;
    }

    public void c() {
        sendPacket(getRemovePacket());
    }

}