package com.jagrosh.jmusicbot.utils;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;

public final class RoleUtil {
    private RoleUtil() {
    }

    public static void addRole(Guild guild, User user, String roleName) {
        Role role = findRole(guild, roleName);
        addRole(guild, user, role);
    }

    private static void addRole(Guild guild, User user, Role role) {
        Member member = guild.getMember(user);
        if (member != null) {
            guild.addRoleToMember(member, role)
                .queue(success -> {
                }, fail -> {
                });
        }
    }

    public static Role findRole(Guild guild, String roleName) {
        if (guild == null) {
            throw new IllegalArgumentException("Guild not found!");
        }
        String temp = roleName;
        if (roleName.startsWith("<")) {
            temp = temp.replaceAll("[@&<#>]", "");
            return guild.getRoleById(temp);
        } else {
            return guild.getRolesByName(temp, false).stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format("Role \"%s\" doesn't exist!", roleName)));
        }
    }

    public static void removeRole(Guild guild, User user, String roleName) {
        Role role = findRole(guild, roleName);
        Member member = guild.getMember(user);
        if (member != null) {
            guild.removeRoleFromMember(member, role)
                .queue(success -> {
                }, fail -> {
                });
        }
    }
}

