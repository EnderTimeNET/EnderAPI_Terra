package net.endertime.enderapi.spigot.utils;

import net.endertime.enderapi.clan.ClanAPI;
import net.endertime.enderapi.spigot.api.EnderAPI;
import net.endertime.enderapi.spigot.api.PermAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ScoreBoardPrefix {

    private String rang;
    private String suffix;
    private String labySuffix;
    private String team;
    private String entry;

    public ScoreBoardPrefix(UUID uuid) {
        if (ClanAPI.getInstance().getMember().isUserExists(uuid)) {
            if (ClanAPI.getInstance().getClans().isTagExists(ClanAPI.getInstance().getMember().getTag(uuid))) {
                this.suffix = " §8[§6" + ClanAPI.getInstance().getMember().getTag(uuid) + "§8]";
                if (ClanAPI.getInstance().getMember().getRang(uuid) == 0) {
                    this.labySuffix = "§6" + ClanAPI.getInstance().getClans().getName(ClanAPI.getInstance().getMember().getTag(uuid)) + " §8× §7Member";
                } else if (ClanAPI.getInstance().getMember().getRang(uuid) == 1) {
                    this.labySuffix = "§6" + ClanAPI.getInstance().getClans().getName(ClanAPI.getInstance().getMember().getTag(uuid)) + " §8× §4Leader";
                } else if (ClanAPI.getInstance().getMember().getRang(uuid) == 2) {
                    this.labySuffix = "§6" + ClanAPI.getInstance().getClans().getName(ClanAPI.getInstance().getMember().getTag(uuid)) + " §8× §cMod";
                }
            }
        } else {
            this.suffix = "";
            this.labySuffix = "";
        }
        this.rang = PermAPI.getInstance().getRanks().getCompletedPrefix(PermAPI.getInstance().getUsers().getRank(uuid));
        this.team = PermAPI.getInstance().getRanks().getTeam(PermAPI.getInstance().getUsers().getRank(uuid))
                + EnderAPI.getInstance().getName(uuid);
        this.entry = EnderAPI.getInstance().getName(uuid);

        if (this.team.length() > 16) {
            this.team = (this.team).substring(0, 16);
        }

        if (EnderAPI.getInstance().getBadlion().contains(uuid)) {
            suffix = " ✔" + suffix;
        }
    }

    public String getSuffix() {
        return suffix;
    }

    public String getLabySuffix() {
        return labySuffix;
    }

    public String getTeam() {
        return team;
    }

    public String getRang() {
        return rang;
    }

    public String getEntry() {
        return entry;
    }
}