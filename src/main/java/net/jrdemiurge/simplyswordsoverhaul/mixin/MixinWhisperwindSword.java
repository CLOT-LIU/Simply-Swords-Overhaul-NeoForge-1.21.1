package net.jrdemiurge.simplyswordsoverhaul.mixin;

import net.jrdemiurge.simplyswordsoverhaul.Config;
import net.jrdemiurge.simplyswordsoverhaul.scheduler.Scheduler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.sweenus.simplyswords.api.SimplySwordsAPI;
import net.sweenus.simplyswords.item.UniqueSwordItem;
import net.sweenus.simplyswords.item.custom.WhisperwindSwordItem;
import net.sweenus.simplyswords.registry.SoundRegistry;
import net.sweenus.simplyswords.util.HelperMethods;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Mixin(WhisperwindSwordItem.class)
public abstract class MixinWhisperwindSword extends UniqueSwordItem {

    @Unique
    private static final String WHISPERWIND_DASHING = "SimplySwordsWhisperwindDashing";

    @Unique
    private static final String WHISPERWIND_MOB_KILLED = "SimplySwordsWhisperwindMobKilled";

    @Unique
    private static final Map<UUID, Set<UUID>> DASH_HIT_MAP = new HashMap<>();

    public MixinWhisperwindSword(Tier toolMaterial, Properties settings) {
        super(toolMaterial, settings);
    }

    @Inject(method = "hurtEnemy", at = @At("HEAD"), cancellable = true)
    public void modifyHurtEnemyMethod(ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfoReturnable<Boolean> cir) {
        if (!Config.enableWhisperwindChanges){
            return;
        }
        if (!attacker.level().isClientSide()) {
            if (target.getHealth() == 0.0F) {
                attacker.level().playSound(null, attacker, SoundRegistry.MAGIC_SWORD_SPELL_02.get(), attacker.getSoundSource(), 0.3F, 1.8F);

                Player player = (Player) attacker;
                player.getCooldowns().addCooldown((WhisperwindSwordItem) (Object) this, 0);
            }
        }
        cir.setReturnValue(super.hurtEnemy(stack, target, attacker));
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    public void modifyUseMethod(Level world, Player user, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        if (!Config.enableWhisperwindChanges){
            return;
        }
        if (!user.level().isClientSide()) {
            ItemStack offHandItem = user.getItemInHand(InteractionHand.OFF_HAND);

            boolean isMainHandUse = hand == InteractionHand.MAIN_HAND;
            boolean isOffHandItemNotOnCooldown = !user.getCooldowns().isOnCooldown(offHandItem.getItem());
            if (isMainHandUse && isOffHandItemNotOnCooldown) {
                user.getCooldowns().addCooldown(offHandItem.getItem(), 4);
            }

            int cooldown = Config.whisperwindCooldownTicks;
            double scale = Config.whisperwindDashDistance / 11D;

            boolean isDashing = user.getPersistentData().getBoolean(WHISPERWIND_DASHING);
            if (isDashing) {
                user.getPersistentData().remove(WHISPERWIND_DASHING);
                user.setDeltaMovement(Vec3.ZERO);
                user.setNoGravity(false);
                user.hurtMarked = true;
                ((ServerPlayer) user).connection.send(new ClientboundSetEntityMotionPacket(user));
                if (!user.getPersistentData().getBoolean(WHISPERWIND_MOB_KILLED)) {
                    user.getCooldowns().addCooldown((WhisperwindSwordItem) (Object) this, cooldown);
                } else {
                    user.getPersistentData().remove(WHISPERWIND_MOB_KILLED);
                }
                DASH_HIT_MAP.remove(user.getUUID());
                cir.setReturnValue(super.use(world, user, hand));
                return;
            } else {
                user.getPersistentData().putBoolean(WHISPERWIND_DASHING, true);
            }

            user.getCooldowns().addCooldown((WhisperwindSwordItem) (Object) this, 2);
            world.playSound(null, user, SoundRegistry.ELEMENTAL_BOW_SCIFI_SHOOT_IMPACT_01.get(), user.getSoundSource(), 0.6F, 1.0F);

            Vec3 look = user.getLookAngle().normalize();
            look = new Vec3(look.x, 0, look.z).normalize().scale(scale);
            Vec3 finalLook = look;

            user.invulnerableTime = 20;
            user.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20, 4, false, false));

            int dashDuration = 7;

            user.setNoGravity(true);
            user.hurtMarked = true;

            for (int i = 0; i < dashDuration; i++) {
                int delay = i * 2;

                Scheduler.schedule(() -> {
                    if (!user.getPersistentData().getBoolean(WHISPERWIND_DASHING)) return;

                    user.setDeltaMovement(finalLook);
                    ((ServerPlayer) user).connection.send(new ClientboundSetEntityMotionPacket(user));

                    UUID playerId = user.getUUID();
                    Set<UUID> hitMobs = DASH_HIT_MAP.computeIfAbsent(playerId, k -> new HashSet<>());

                    AABB area = user.getBoundingBox().inflate(2);
                    List<LivingEntity> nearbyMobs = world.getEntitiesOfClass(LivingEntity.class, area, entity -> entity != user);

                    for (LivingEntity mob : nearbyMobs) {
                        if (HelperMethods.checkFriendlyFire(mob, user)) {

                            if (hitMobs.contains(mob.getUUID())) {
                                continue;
                            }

                            mob.hurt(user.damageSources().playerAttack(user), calculateDamage(world, user, mob));

                            hitMobs.add(mob.getUUID());

                            if (mob.getHealth() == 0.0F) {
                                user.level().playSound(null, user, SoundRegistry.MAGIC_SWORD_SPELL_02.get(), user.getSoundSource(), 0.3F, 1.8F);
                                user.getPersistentData().putBoolean(WHISPERWIND_MOB_KILLED, true);
                            }
                        }
                    }

                    int particleRadius = (int)(1.5);
                    double xpos = user.getX() - (double)(particleRadius + 1);
                    double ypos = user.getY();
                    double zpos = user.getZ() - (double)(particleRadius + 1);

                    for(int b = particleRadius * 2; b > 0; --b) {
                        for(int j = particleRadius * 2; j > 0; --j) {
                            float choose = (float)(Math.random() * 1.0);
                            HelperMethods.spawnParticle(world, ParticleTypes.ELECTRIC_SPARK, xpos + (double)b + (double)choose, ypos + 0.4, zpos + (double)j + (double)choose, 0.0, 0.1, 0.0);
                            HelperMethods.spawnParticle(world, ParticleTypes.CLOUD, xpos + (double)b + (double)choose, ypos + 0.1, zpos + (double)j + (double)choose, 0.0, 0.0, 0.0);
                            HelperMethods.spawnParticle(world, ParticleTypes.WARPED_SPORE, xpos + (double)b + (double)choose, ypos, zpos + (double)j + (double)choose, 0.0, 0.1, 0.0);
                        }
                    }
                }, delay, 0);
            }

            Scheduler.schedule(() -> {
                if (!user.getPersistentData().getBoolean(WHISPERWIND_DASHING)) return;
                user.getPersistentData().remove(WHISPERWIND_DASHING);
                user.setDeltaMovement(Vec3.ZERO);
                user.setNoGravity(false);
                user.hurtMarked = true;
                ((ServerPlayer) user).connection.send(new ClientboundSetEntityMotionPacket(user));
                if (!user.getPersistentData().getBoolean(WHISPERWIND_MOB_KILLED)) {
                    user.getCooldowns().addCooldown((WhisperwindSwordItem) (Object) this, cooldown);
                } else {
                    user.getPersistentData().remove(WHISPERWIND_MOB_KILLED);
                }
                DASH_HIT_MAP.remove(user.getUUID());
            }, dashDuration * 2, 0);

        }
        cir.setReturnValue(super.use(world, user, hand));
    }

    private static float calculateDamage(Level level, Player player, Entity target) {
        float baseDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        float enchantmentBonus = 0.0F;
        if (level instanceof ServerLevel serverLevel) {
            enchantmentBonus = EnchantmentHelper.modifyDamage(serverLevel, player.getMainHandItem(), target,
                    player.damageSources().playerAttack(player), 0.0F);
        }

        CriticalHitEvent hitResult = CommonHooks.fireCriticalHit(player, target, true, true ? 1.5F : 1.0F);
        boolean flag2 = hitResult != null;
        if (flag2) {
            baseDamage *= hitResult.getDamageMultiplier();
        }

        return baseDamage + enchantmentBonus;
    }

    @OnlyIn(Dist.CLIENT)
    @Inject(method = "appendHoverText", at = @At("HEAD"), cancellable = true)
    private void modifyTooltip(ItemStack itemStack, Item.TooltipContext tooltipContext, List<Component> tooltip, TooltipFlag tooltipFlag, CallbackInfo ci) {
        if (!Config.enableWhisperwindChanges){
            return;
        }
        ci.cancel();

        int dashDistance = Config.whisperwindDashDistance;
        String translatedText = Component.translatable("tooltip.simply_swords_overhaul.whisperwinditem", dashDistance).getString();

        for (String line : translatedText.split("\n")) {
            tooltip.add(Component.literal(line));
        }

        int cooldown = Config.whisperwindCooldownTicks;
        float floatCooldown = (float) cooldown / 20;


        if (Screen.hasShiftDown()) {
            tooltip.add(Component.translatable("tooltip.simply_swords_overhaul.whisperwinditem_1")
                    .withStyle(ChatFormatting.GRAY));

            tooltip.add(Component.literal(" "));
            tooltip.add(Component.translatable("tooltip.simply_swords_overhaul.cooldown", floatCooldown)
                    .withStyle(ChatFormatting.BLUE));
        } else {
            tooltip.add(Component.literal(" "));
            tooltip.add(Component.translatable("tooltip.simply_swords_overhaul.cooldown", floatCooldown)
                    .withStyle(ChatFormatting.BLUE));

            SimplySwordsAPI.appendTooltipGemSocketLogic(itemStack, tooltipContext, tooltip, tooltipFlag);

            if (!tooltip.get(tooltip.size() - 1)
                    .getString().equals(Component.translatable("item.simplyswords.common.showtooltip").getString())) {
                tooltip.add(Component.literal(""));
            }
            tooltip.add(Component.translatable("tooltip.simply_swords_overhaul.shift"));
        }
    }
}
