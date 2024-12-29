package net.bagaja.chunkeffects;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;

import java.util.*;

@Mod(ChunkEffectsMod.MODID)
public class ChunkEffectsMod {
    public static final String MODID = "chunkeffects";
    private static final Map<Long, ChunkEffectData> chunkEffects = new HashMap<>();
    private static final Random random = new Random();
    private static final int MAX_AMPLIFIER = 4; // Maximum effect level (0-based, so 4 means level 5)

    private static class ChunkEffectData {
        final MobEffect effect;
        final int amplifier;

        ChunkEffectData(MobEffect effect, int amplifier) {
            this.effect = effect;
            this.amplifier = amplifier;
        }
    }

    public ChunkEffectsMod() {
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Player player = event.player;
            if (!player.level().isClientSide) {
                long chunkPos = getChunkKey(player);

                // Get or generate effect for this chunk
                ChunkEffectData effectData = chunkEffects.computeIfAbsent(chunkPos, k -> {
                    // Get all registered effects and pick one randomly
                    List<MobEffect> allEffects = new ArrayList<>();
                    BuiltInRegistries.MOB_EFFECT.forEach(allEffects::add);
                    MobEffect randomEffect = allEffects.get(random.nextInt(allEffects.size()));

                    // Generate random amplifier (0 to MAX_AMPLIFIER)
                    int randomAmplifier = random.nextInt(MAX_AMPLIFIER + 1);

                    return new ChunkEffectData(randomEffect, randomAmplifier);
                });

                // Apply effect with stored amplifier
                player.addEffect(new MobEffectInstance(
                        effectData.effect,
                        40, // duration in ticks
                        effectData.amplifier,
                        false, // ambient
                        false, // visible
                        true  // showIcon
                ));
            }
        }
    }

    private long getChunkKey(Player player) {
        int chunkX = (int) player.getX() >> 4;
        int chunkZ = (int) player.getZ() >> 4;
        return ((long) chunkX << 32) | (chunkZ & 0xFFFFFFFFL);
    }
}

