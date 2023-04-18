package com.s151044.discord.commands;

import java.util.*;

/**
 * A list of commands for a specific prefix.
 */
public class CommandList {
    private final Map<String, Command> commandMap;
    private final Map<List<String>, String> aliasMap;

    public CommandList() {
        commandMap = new HashMap<>();
        aliasMap = new HashMap<>();
    }

    /**
     * Tries to get a command if it exists by its alias or name.
     *
     * @param byName The name of the command, or its alias, to search for
     * @return An optional containing the requested command, or an empty optional if it cannot be found
     */
    public Optional<Command> tryGet(String byName) {
        if (commandMap.containsKey(byName)) {
            return Optional.of(commandMap.get(byName));
        }
        if (aliasMap.keySet().stream().flatMap(Collection::stream).anyMatch(str -> str.equals(byName))) {
            return Optional.of(commandMap.get(aliasMap.entrySet().stream().filter(ent -> ent.getKey().contains(byName))
                    .findFirst().map(Map.Entry::getValue).get()));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Adds a command to this command list.
     *
     * @param toAdd The command to add
     */
    public void addCommand(Command toAdd) {
        commandMap.put(toAdd.callName(), toAdd);
        aliasMap.put(toAdd.alias(), toAdd.callName());
    }

    public List<Command> getCommands() {
        return List.copyOf(commandMap.values());
    }

    public int getSize() {
        return commandMap.size();
    }
}
