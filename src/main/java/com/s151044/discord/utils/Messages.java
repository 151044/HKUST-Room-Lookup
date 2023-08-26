package com.s151044.discord.utils;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A utility class for sending messages.
 */
public class Messages {
    /**
     * Queues a message to be sent.
     * This is a convenience method for {@link Messages#send(TextChannel, String)}.
     * @param evt The message received event, from which a text channel can be derived
     * @param msg The message to send
     */
    public static void send(MessageReceivedEvent evt, String msg){
        send(evt.getChannel().asTextChannel(),msg);
    }

    /**
     * Queues a message to be sent.
     * @param text The text channel to send to
     * @param msg The message to send
     */
    public static void send(TextChannel text, String msg){
        boolean hasNewlines = msg.lastIndexOf("\n") != -1;
        boolean hasSpace = msg.lastIndexOf(" ") != -1;
        while (msg.length() > 1980) {
            String copy;
            if(!(hasNewlines || hasSpace)) {
                copy = msg.substring(0, msg.lastIndexOf(" ", 1960));
                msg = msg.substring(msg.lastIndexOf(" ", 1960) + 1);
            } else {
                copy = msg.substring(0, 1960);
                msg = msg.substring(1960 + 1);
            }
            text.sendMessage(copy).queue();
        }
        text.sendMessage(msg).queue();
    }

    /**
     * Queues an embed to be sent.
     * @param text The message received event, from which a text channel can be derived
     * @param toSend The embed to send
     */
    public static void send(TextChannel text, MessageEmbed toSend){
        text.sendMessageEmbeds(toSend).queue();
    }

    /**
     * Queues an embed to be sent.
     * This is a convenience method for {@link Messages#send(TextChannel, MessageEmbed)}.
     * @param evt The message received event, from which a text channel can be derived
     * @param toSend The embed to send
     */
    public static void send(MessageReceivedEvent evt, MessageEmbed toSend){
        send(evt.getChannel().asTextChannel(),toSend);
    }

    public static void send(Message reply, String content){
        send(reply.getChannel().asTextChannel(), content);
    }
    public static void send(MessageChannel channel, MessageEmbed toSend){
        channel.sendMessageEmbeds(toSend).queue();
    }

    /**
     * Gets a emotes by its name for the specified guild.
     * @param name The name of the emote to retrieve
     * @param retrieve The guild to get the emote for
     * @return An optional containing the emote if it can be found, or an empty optional otherwise
     */
    public static Optional<RichCustomEmoji> getEmote(String name, Guild retrieve){
        return retrieve.getEmojisByName(name,true).stream().findFirst();
    }

    /**
     * Converts numbers and digits only to a suitable unicode for use in Discord.
     * @param c The character to convert
     * @return The unicode String
     */
    public static String toUnicode(char c){
        if (Character.isDigit(c)) {
            return "U+00" + Long.toHexString((int) c);
        }else {
            return "U+1F1" + Long.toHexString(((int) c) - 97 + 0xE6);
        }
    }

    /**
     * Converts numbers and digits only to a suitable unicode for use in Discord.
     * @param split The string to convert
     * @return The unicode String
     */
    public static List<String> toUnicode(String split){
        return split.toLowerCase().chars().mapToObj(i -> toUnicode((char) i)).collect(Collectors.toList());
    }

    /**
     * Converts a length of time, in milliseconds, to a string in the format of hh:mm:ss.
     * @param length The time period to use
     * @return The formatted time string
     */
    public static String toTime(long length){
        long seconds = length / 1000;
        long min = (seconds % 3600) / 60;
        long hour = seconds / 3600;
        return String.format("%d:%02d:%02d", hour, min, seconds % 60);
    }
}
