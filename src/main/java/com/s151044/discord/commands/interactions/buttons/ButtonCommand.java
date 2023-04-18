package com.s151044.discord.commands.interactions.buttons;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

/**
 * Interface representing a button event.
 */
public interface ButtonCommand {
    /**
     * Runs an action associated with this button event.
     * @param evt The event to operate on
     * @param id The name of the button clicked
     */
    void action(ButtonInteractionEvent evt, String id);

    /**
     * Gets the prefix attached to each button of this command.
     * @return The prefix of this command
     */
    String prefix();
}
