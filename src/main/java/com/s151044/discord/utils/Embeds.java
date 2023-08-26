package com.s151044.discord.utils;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for creating embeds.
 */
public class Embeds {

    /**
     * Creates an embed with the specified description and title.
     * @param desc The description to set
     * @return The created embed
     */
    public static MessageEmbed getEmbed(String desc){
        return new EmbedBuilder().setColor(Color.CYAN).setDescription(desc).build();
    }

    /**
     * Creates an embed with the specified description and title.
     * @param desc The description to set
     * @param title The title to set
     * @return The created embed
     */
    public static MessageEmbed getEmbed(String desc, String title){
        return new EmbedBuilder().setColor(Color.CYAN).setDescription(desc).setTitle(title).build();
    }

    /**
     * Creates one or more embeds with a given input string.
     * If {@param desc} is too long such that it cannot be sent in one message, it will be split up onto multiple embeds.
     * @param desc The description to send
     * @return The created embed(s)
     */
    public static List<MessageEmbed> getLongEmbed(String desc){
        List<MessageEmbed> ret = new ArrayList<>();
        boolean hasNewlines = desc.lastIndexOf("\n") != -1;
        boolean hasSpace = desc.lastIndexOf(" ") != -1;
        while (desc.length() > 1980) {
            String copy;
            if(!(hasNewlines || hasSpace)) {
                copy = desc.substring(0, desc.lastIndexOf(" ", 1960));
                desc = desc.substring(desc.lastIndexOf(" ", 1960) + 1);
            } else {
                copy = desc.substring(0, 1960);
                desc = desc.substring(1960 + 1);
            }
            ret.add(new EmbedBuilder().setColor(Color.CYAN).setDescription(copy).build());
        }
        ret.add(new EmbedBuilder().setColor(Color.CYAN).setDescription(desc).build());
        return ret;
    }

    /**
     * Creates one or more embeds with a given input string.
     * If {@param desc} is too long such that it cannot be sent in one message, it will be split up onto multiple embeds.
     * @param desc The description to send
     * @param title The title to send
     * @return The created embed(s)
     */
    public static List<MessageEmbed> getLongEmbed(String desc,String title){
        List<MessageEmbed> ret = new ArrayList<>();
        boolean isFirst = true;
        boolean hasNewlines = desc.lastIndexOf("\n") != -1;
        while (desc.length() > 1980) {
            String copy = desc.substring(0, desc.lastIndexOf(hasNewlines ? "\n" : " ", 1960));
            desc = desc.substring(desc.lastIndexOf(hasNewlines ? "\n" : " ", 1960) + 1);
            ret.add(new EmbedBuilder().setColor(Color.CYAN).setDescription(copy).setTitle(isFirst ? title : title + "(Continued)").build());
            if (isFirst) {
                isFirst = false;
            }
        }
        ret.add(new EmbedBuilder().setColor(Color.CYAN).setDescription(desc).setTitle(isFirst ? title : title + "(Continued)").build());
        return ret;
    }
}
