package github.spigotrce.paradiseclientfabric.command.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import github.spigotrce.paradiseclientfabric.Helper;
import github.spigotrce.paradiseclientfabric.command.Command;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.TabCompleteC2SPacket;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;

@Environment(EnvType.CLIENT)
public class FakeOpBypassCommand extends Command {

    public FakeOpBypassCommand(MinecraftClient mc) {
        super("fakeopbypass", "Sends fake /op requests to confuse moderation plugins.", mc);
    }

    @Override
    public void build(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> builder = literal(getName())
            .executes(context -> {
                startSpam();
                return 1;
            });

        dispatcher.register(builder);
    }

    private void startSpam() {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.getNetworkHandler() == null || client.player == null) {
            Helper.printChatMessage("§c[FakeOpBypass] Not connected to a server.");
            return;
        }

        Helper.printChatMessage("§b[FakeOpBypass] §fStarted sending fake op commands...");

        final Random random = new Random();

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            try {
                String target = getRandomName(random);
                String fakeCommand = "/op " + target;

                client.getNetworkHandler().sendPacket(new ChatMessageC2SPacket(fakeCommand));
                client.getNetworkHandler().sendPacket(new TabCompleteC2SPacket(fakeCommand, false));

            } catch (Exception e) {
                System.err.println("FakeOpBypass error: " + e.getMessage());
            }
        }, 0, 250, TimeUnit.MILLISECONDS); // Spam rate
    }

    private String getRandomName(Random random) {
        String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder name = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            name.append(chars.charAt(random.nextInt(chars.length())));
        }
        return name.toString();
    }

    @Override
    public String getName() {
        return "fakeopbypass";
    }

    @Override
    public String getDescription() {
        return "Sends fake /op tab completes and chat to confuse protection systems.";
    }
          }
