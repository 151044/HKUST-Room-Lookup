package com.s151044.discord.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

/**
 * The interface to represent a command which can be triggered by a message in Discord.
 */
public interface Command {
    /**
     * The function that is called on a message sent by anyone in a guild.
     *
     * @param evt       The message received event
     * @param callName  The string used to invoke the command
     * @param arguments The arguments trailing the command word
     */
    void action(MessageReceivedEvent evt, String callName, String arguments);

    /**
     * Returns a list of aliases with which this command can be called by a message.
     * @return The list of aliases
     */
    List<String> alias();

    /**
     * Gets the name with which this command can be invoked by a message.
     * @return The call name of this command
     */
    String callName();

    String shortHelp();

    String longHelp();

    default boolean hidden(){
        return false;
    }
}
