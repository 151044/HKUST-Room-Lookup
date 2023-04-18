package com.s151044.discord.handlers.interactions;

import com.s151044.discord.commands.interactions.buttons.ButtonCommand;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class ButtonHandler extends ListenerAdapter {
    private final List<ButtonCommand> commands = new ArrayList<>();
    private final List<String> prefixes = new ArrayList<>();
    private static final AtomicInteger atomic = new AtomicInteger(0);
    public void addCommand(ButtonCommand cmd) {
        if (prefixes.contains(cmd.prefix())) {
            throw new IllegalArgumentException("Duplicate prefix" + prefixes);
        }
        commands.add(cmd);
        prefixes.add(cmd.prefix());
    }
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String id = event.getComponentId();
        if (prefixes.stream().noneMatch(id::startsWith)) {
            return;
        }
        Optional<ButtonCommand> optCmd = commands.stream().filter(cmd -> id.startsWith(cmd.prefix())).findFirst();
        if (optCmd.isEmpty()) {
            // should be impossible since we streamed above, but safety
            return;
        }
        ButtonCommand dispatch = optCmd.get();
        dispatch.action(event, id.substring(id.indexOf("-") + 1));
    }
    public boolean hasPrefix(String prefix) {
        return prefixes.contains(prefix);
    }
    public static synchronized int allocateId() {
        return atomic.getAndIncrement();
    }
}
