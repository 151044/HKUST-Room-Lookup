package com.s151044.discord.commands.interactions.buttons;

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

public interface ButtonCommand {
    void action(ButtonInteractionEvent evt, String id);
    String prefix();
}
