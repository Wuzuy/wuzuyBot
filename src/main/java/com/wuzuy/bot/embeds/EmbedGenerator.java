package com.wuzuy.bot.embeds;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

public class EmbedGenerator {

    EmbedBuilder embed = new EmbedBuilder();

    public MessageEmbed defaultEmbed(String option1, Color optionColor, String text, String memberEvent) {
        embed.setTitle(option1)
                .setColor(optionColor)
                .setDescription(text)
                .setFooter("Servidor do Wuzuy")
                .setTimestamp(OffsetDateTime.now())
                .setThumbnail(memberEvent);

                return embed.build();
    }

    public MessageEmbed criarEmbedRegistro() {
        embed.setAuthor("Registro!")
                .setColor(Color.WHITE)
                .appendDescription("Clique no botão abaixo para se registrar!");

        return embed.build();
    }

    public MessageEmbed criarEmbedGenero() {
        embed.setTitle("Informe seu gênero:")
                .setColor(Color.WHITE);

        return embed.build();
    }

    public MessageEmbed criarEmbedIdade() {
        embed.setTitle("Maior ou menor de idade?")
                .setColor(Color.WHITE);

        return embed.build();
    }

    public List<Button> criarButtonsRegistro() {
        List<Button> buttons = new ArrayList<>();
        buttons.add(Button.primary("registro", "Registrar!"));

        return buttons;
    }

    public List<Button> criarButtonsGeneros() {
        List<Button> buttons = new ArrayList<>();
        buttons.add(Button.primary("masculino", "Homem"));
        buttons.add(Button.primary("feminino", "Mulher"));

        return buttons;
    }

    public List<Button> criarButtonsIdades() {
        List<Button> buttons = new ArrayList<>();
        buttons.add(Button.primary("+18", "+18"));
        buttons.add(Button.primary("-18", "-18"));

        return buttons;
    }
}
