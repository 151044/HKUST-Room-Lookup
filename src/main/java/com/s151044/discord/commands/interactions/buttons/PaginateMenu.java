package com.s151044.discord.commands.interactions.buttons;

import com.s151044.discord.Embeds;
import com.s151044.discord.handlers.interactions.ButtonHandler;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.Collections;
import java.util.List;

public class PaginateMenu implements ButtonCommand {
    private final List<String> embeds;
    private final List<String> titles;
    private final SlashCommandInteractionEvent event;
    private final Button nextButton;
    private final Button prevButton;
    private int pos = 0;
    private int id;

    public PaginateMenu(List<String> messages, List<String> titles, SlashCommandInteractionEvent event) {
        this.embeds = messages;
        this.titles = titles;
        this.event = event;
        this.nextButton = Button.secondary("Menu_" + id + "-" + "next", "Next Page")
                .withEmoji(Emoji.fromFormatted("➡️"));
        this.prevButton = Button.secondary("Menu_" + id + "-" + "prev", "Prev Page")
                .withEmoji(Emoji.fromFormatted("⬅️"));
        id = ButtonHandler.allocateId();
    }

    public PaginateMenu(List<String> messages, String title, SlashCommandInteractionEvent event) {
        this(messages, Collections.nCopies(messages.size(), title), event);
    }

    public void showMenu() {
        if (embeds.size() == 0) {
            throw new IllegalStateException("Empty embeds list?");
        }
        String pageString = String.format("**Page %d of %d**", 1, embeds.size());
        String title = titles.get(0);
        MessageEmbed emb = Embeds.getEmbed(embeds.get(0) + pageString, title);
        event.replyEmbeds(emb)
                .addActionRow(prevButton, nextButton)
                .queue();
    }

    @Override
    public void action(ButtonInteractionEvent evt, String id) {
        evt.getInteraction().deferEdit().queue();
        if (id.equals("next")) {
            if (pos < embeds.size() - 1) {
                pos++;
                String pageString = String.format("\n**Page %d of %d**", pos + 1, embeds.size());
                String title = titles.get(pos);
                evt.getMessage().editMessageEmbeds(Embeds.getEmbed(embeds.get(pos) + pageString, title))
                        .queue();
            }
        } else if (id.equals("prev")) {
            if (pos != 0) {
                pos--;
                String pageString = String.format("\n**Page %d of %d**", pos + 1, embeds.size());
                String title = titles.get(pos);
                evt.getMessage().editMessageEmbeds(Embeds.getEmbed(embeds.get(pos) + pageString, title))
                        .queue();
            }
        }
    }

    @Override
    public String prefix() {
        return "Menu_" + id;
    }
}