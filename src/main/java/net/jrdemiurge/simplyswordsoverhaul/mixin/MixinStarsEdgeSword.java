package net.jrdemiurge.simplyswordsoverhaul.mixin;

import net.jrdemiurge.simplyswordsoverhaul.Config;
import net.jrdemiurge.simplyswordsoverhaul.scheduler.Scheduler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.sweenus.simplyswords.api.SimplySwordsAPI;
import net.sweenus.simplyswords.item.UniqueSwordItem;
import net.sweenus.simplyswords.item.custom.StarsEdgeSwordItem;
import net.sweenus.simplyswords.registry.SoundRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

@Mixin(StarsEdgeSwordItem.class)
public abstract class MixinStarsEdgeSword extends UniqueSwordItem {

    private static final HashMap<UUID, Vec3> savedPositions = new HashMap<>();

    public MixinStarsEdgeSword(Tier toolMaterial, Properties settings) {
        super(toolMaterial, settings);
    }

    @Inject(method = "hurtEnemy", at = @At("HEAD"), cancellable = true)
    public void modifyHurtEnemyMethod(ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfoReturnable<Boolean> cir) {
        if (!Config.enableStarsEdgeChanges){
            return;
        }
        if (!attacker.level().isClientSide()) {

            float magicDamage = (float) Config.starsEdgeMagicDamage;
            target.invulnerableTime = 0;
            target.hurt(attacker.damageSources().indirectMagic(attacker, attacker), magicDamage);

        }
        cir.setReturnValue(super.hurtEnemy(stack, target, attacker));
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    public void modifyUseMethod(Level world, Player user, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        if (!Config.enableStarsEdgeChanges){
            return;
        }
        if (!user.level().isClientSide()) {
            UUID playerId = user.getUUID();

            ItemStack offHandItem = user.getItemInHand(InteractionHand.OFF_HAND);
            boolean isMainHandUse = hand == InteractionHand.MAIN_HAND;
            boolean isOffHandItemNotOnCooldown = !user.getCooldowns().isOnCooldown(offHandItem.getItem());

            if (isMainHandUse && isOffHandItemNotOnCooldown) {
                user.getCooldowns().addCooldown(offHandItem.getItem(), 4);
            }

            int teleportDelay = Config.starsEdgeTeleportDelay;
            int cooldown = Config.starsEdgeCooldownTicks;
            int speedDuration = Config.starsEdgeSpeedDuration;
            int speedLevel = Config.starsEdgeSpeedLevel;
            int hasteDuration = Config.starsEdgeHasteDuration;
            int hasteLevel = Config.starsEdgeHasteLevel;
            double dashForce = Config.starsEdgeDashForce;
            int resistanceDuration = Config.starsEdgeResistanceDuration;
            user.swing(hand);

            if (savedPositions.containsKey(playerId)) {
                Vec3 savedPos = savedPositions.remove(playerId);
                user.teleportTo(savedPos.x, savedPos.y, savedPos.z);
                world.playSound(null, user, SoundRegistry.ELEMENTAL_BOW_HOLY_SHOOT_IMPACT_01.get(), user.getSoundSource(), 0.5F, 1.3F);
                user.getCooldowns().addCooldown((StarsEdgeSwordItem) (Object) this, cooldown);
            } else {
                savedPositions.put(playerId, user.position());

                world.playSound(null, user, SoundRegistry.ELEMENTAL_BOW_HOLY_SHOOT_IMPACT_02.get(), user.getSoundSource(), 0.5F, 1.3F);

                if (user.isShiftKeyDown()) {
                    user.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * resistanceDuration, 4, false, false));
                } else {
                    Vec3 lookVec = user.getLookAngle();
                    Vec3 horizontalLookVec = new Vec3(lookVec.x, 0, lookVec.z).normalize().scale(dashForce);
                    user.setDeltaMovement(horizontalLookVec);
                    ((ServerPlayer) user).connection.send(new ClientboundSetEntityMotionPacket(user));
                    user.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * resistanceDuration, 4, false, false));
                }

                Scheduler.schedule(() -> {
                    if (savedPositions.containsKey(playerId)) {
                        Vec3 savedPos = savedPositions.remove(playerId);
                        user.teleportTo(savedPos.x, savedPos.y, savedPos.z);
                        world.playSound(null, user, SoundRegistry.ELEMENTAL_BOW_HOLY_SHOOT_IMPACT_01.get(), user.getSoundSource(), 0.5F, 1.3F);
                        user.getCooldowns().addCooldown((StarsEdgeSwordItem) (Object) this, cooldown);
                    }
                }, 20 * teleportDelay, 0);

                applyOrUpgradeEffect(user, MobEffects.MOVEMENT_SPEED, 20 * speedDuration, speedLevel - 1);
                applyOrUpgradeEffect(user, MobEffects.DIG_SPEED, 20 * hasteDuration, hasteLevel - 1);

                user.getCooldowns().addCooldown((StarsEdgeSwordItem) (Object) this, 10);
            }

        }
        cir.setReturnValue(super.use(world, user, hand));
    }

    private void applyOrUpgradeEffect(Player player, Holder<MobEffect> effect, int duration, int baseAmplifier) {
        MobEffectInstance existingEffect = player.getEffect(effect);
        int newAmplifier = existingEffect != null ? existingEffect.getAmplifier() + baseAmplifier + 1: baseAmplifier;
        player.addEffect(new MobEffectInstance(effect, duration, newAmplifier, false, true));
    }

    @OnlyIn(Dist.CLIENT)
    @Inject(method = "appendHoverText", at = @At("HEAD"), cancellable = true)
    private void modifyTooltip(ItemStack itemStack, Item.TooltipContext tooltipContext, List<Component> tooltip, TooltipFlag tooltipFlag, CallbackInfo ci) {
        if (!Config.enableStarsEdgeChanges){
            return;
        }
        ci.cancel();

        float magicDamage = (float) Config.starsEdgeMagicDamage;
        int teleportDelay = Config.starsEdgeTeleportDelay;
        int speedLevel = Config.starsEdgeSpeedLevel;
        int hasteDuration = Config.starsEdgeHasteDuration;
        int hasteLevel = Config.starsEdgeHasteLevel;
        int cooldown = Config.starsEdgeCooldownTicks;
        float floatCooldown = (float) cooldown / 20;

        if (Screen.hasAltDown()){
            String translatedText = Component.translatable("tooltip.simply_swords_overhaul.starsedgeitem", magicDamage,speedLevel, hasteLevel, hasteDuration, teleportDelay).getString();
            for (String line : translatedText.split("\n")) tooltip.add(Component.literal(line));

            tooltip.add(Component.literal(" "));
            tooltip.add(Component.translatable("tooltip.simply_swords_overhaul.cooldown", floatCooldown)
                    .withStyle(ChatFormatting.BLUE));

            SimplySwordsAPI.appendTooltipGemSocketLogic(itemStack, tooltipContext, tooltip, tooltipFlag);

        } else if (Screen.hasShiftDown()) {
            String translatedText = Component.translatable("tooltip.simply_swords_overhaul.starsedgeitem", magicDamage,speedLevel, hasteLevel, hasteDuration, teleportDelay).getString();
            for (String line : translatedText.split("\n")) tooltip.add(Component.literal(line));

            translatedText = Component.translatable("tooltip.simply_swords_overhaul.starsedgeitem_shift").getString();
            for (String line : translatedText.split("\n")) tooltip.add(Component.literal(line)
                    .withStyle(ChatFormatting.GRAY));

            tooltip.add(Component.literal(" "));
            tooltip.add(Component.translatable("tooltip.simply_swords_overhaul.cooldown", floatCooldown)
                    .withStyle(ChatFormatting.BLUE));

        } else {
            String translatedText = Component.translatable("tooltip.simply_swords_overhaul.starsedgeitem", magicDamage,speedLevel, hasteLevel, hasteDuration, teleportDelay).getString();
            for (String line : translatedText.split("\n")) tooltip.add(Component.literal(line));

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
