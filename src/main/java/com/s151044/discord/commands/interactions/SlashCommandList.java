package com.s151044.discord.commands.interactions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A list of slash commands.
 */
public class SlashCommandList {
    private final Map<String, SlashCommand> commandMap;

    /**
     * Constructs a new SlashCommandList.
     */
    public SlashCommandList() {
        commandMap = new HashMap<>();
    }

    /**
     * Tries to get a command if it exists by its alias or name.
     * @param byName The name of the command, or its alias, to search for
     * @return An optional containing the requested command, or an empty optional if it cannot be found
     */
    public Optional<SlashCommand> tryGet(String byName){
        if(commandMap.containsKey(byName)){
            return Optional.of(commandMap.get(byName));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Adds a command to this command list.
     * @param toAdd The command to add
     */
    public void addCommand(SlashCommand toAdd){
        commandMap.put(toAdd.callName(), toAdd);
    }

    /**
     * Gets a list of slash commands added to this command list.
     * @return A copy of the underlying list of commands
     */
    public List<SlashCommand> getCommands(){
        return List.copyOf(commandMap.values());
    }

    /**
     * Gets the number of commands added to the list.
     * @return The number of commands added
     */
    public int getSize(){
        return commandMap.size();
    }
}
