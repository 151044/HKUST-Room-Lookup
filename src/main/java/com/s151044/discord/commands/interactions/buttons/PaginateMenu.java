package com.s151044.discord.commands.interactions.buttons;

import com.s151044.discord.utils.Embeds;
import com.s151044.discord.handlers.interactions.ButtonHandler;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Creates a menu in Discord which can show strings by pages.
 */
public class PaginateMenu implements ButtonCommand {
    private final List<String> embeds;
    private final List<String> titles;
    private final InteractionHook hook;
    private final Button nextButton;
    private final Button prevButton;
    private int pos = 0;
    private final int id;

    /**
     * Constructs a new PaginateMenu.
     * @param messages The messages to show (i.e. the main content)
     * @param titles The titles of each message (should match the number of messages)
     * @param hook The interaction hook to respond to
     */
    public PaginateMenu(List<String> messages, List<String> titles, InteractionHook hook) {
        this.embeds = messages;
        this.titles = titles;
        this.hook = hook;
        id = ButtonHandler.allocateId();
        this.nextButton = Button.secondary("Menu_" + id + "-" + "next", "Next Page")
                .withEmoji(Emoji.fromFormatted("➡️"));
        this.prevButton = Button.secondary("Menu_" + id + "-" + "prev", "Prev Page")
                .withEmoji(Emoji.fromFormatted("⬅️"));
    }

    /**
     * Constructs a new PaginateMenu.
     * @param messages The messages to show (i.e. the main content)
     * @param title The title for each page of the menu
     * @param hook The interaction hook to respond to
     */
    public PaginateMenu(List<String> messages, String title, InteractionHook hook) {
        this(messages, Collections.nCopies(messages.size(), title), hook);
    }

    /**
     * Shows the menu with the first page.
     * Do not acknowledge the event before calling this method.
     */
    public void showMenu() {
        if (embeds.isEmpty()) {
            throw new IllegalStateException("Empty embeds list?");
        }
        String pageString = String.format("\n**Page %d of %d**", 1, embeds.size());
        String title = titles.get(0);
        MessageEmbed emb = Embeds.getEmbed(embeds.get(0) + pageString, title);
        hook.sendMessageEmbeds(emb)
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

    public static <T> List<String> splitEntries(List<T> list, int limit, Function<T, String> mapper) {
        int index = 0;
        List<String> answers = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (T t : list) {
            if (index != 0 && index % limit == 0) {
                answers.add(sb.toString());
                sb = new StringBuilder();
            }
            sb.append(mapper.apply(t)).append("\n");
            index++;
        }
        answers.add(sb.toString());
        return answers;
    }
}