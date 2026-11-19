package ru.onionsmp.scoreboard;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.numbers.BlankFormat;
import net.minecraft.network.protocol.game.ClientboundResetScorePacket;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;

@Mod("onionscoreboard")
public class OnionScoreboardMod {

    private int tickCounter = 0;

    public OnionScoreboardMod() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent.Post event) {
        if (++tickCounter < 20) return;
        tickCounter = 0;

        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            updateScoreboard(player);
        }
    }

    private void updateScoreboard(ServerPlayer player) {
        int kills = player.getStats().getValue(Stats.CUSTOM.get(Stats.PLAYER_KILLS));
        int deaths = player.getStats().getValue(Stats.CUSTOM.get(Stats.DEATHS));
        int ticksPlayed = player.getStats().getValue(Stats.CUSTOM.get(Stats.PLAY_TIME));

        int hours = ticksPlayed / 72000;
        int minutes = (ticksPlayed % 72000) / 1200;
        String timeStr = hours + "ч " + minutes + "м";

        Objective dummy = new Objective(
                player.getServer().getScoreboard(),
                "onion_sb",
                ObjectiveCriteria.DUMMY,
                Component.literal("§6§lONION §a§lSMP"),
                ObjectiveCriteria.RenderType.INTEGER,
                false,
                BlankFormat.INSTANCE
        );

        player.connection.send(new ClientboundSetObjectivePacket(dummy, 1));
        player.connection.send(new ClientboundSetObjectivePacket(dummy, 0));
        player.connection.send(new ClientboundSetDisplayObjectivePacket(DisplaySlot.SIDEBAR, dummy));

        sendLine(player, "§7§m----------------", 6);
        sendLine(player, "§fИгрок: §e" + player.getName().getString(), 5);
        sendLine(player, " ", 4);
        sendLine(player, "§fУбийств: §a" + kills, 3);
        sendLine(player, "§fСмертей: §c" + deaths, 2);
        sendLine(player, "§fВ игре: §b" + timeStr, 1);
        sendLine(player, "§8§m----------------", 0);
    }

    private void sendLine(ServerPlayer player, String text, int score) {
        player.connection.send(new ClientboundResetScorePacket(text, "onion_sb"));
        player.connection.send(new ClientboundSetScorePacket(
                text,
                "onion_sb",
                score,
                Optional.of(Component.literal(text)),
                Optional.of(BlankFormat.INSTANCE)
        ));
    }
}
