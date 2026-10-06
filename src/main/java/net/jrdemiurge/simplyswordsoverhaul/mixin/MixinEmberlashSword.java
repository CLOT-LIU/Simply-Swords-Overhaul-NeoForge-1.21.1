package net.jrdemiurge.simplyswordsoverhaul.mixin;

import net.jrdemiurge.simplyswordsoverhaul.Config;
import net.jrdemiurge.simplyswordsoverhaul.scheduler.Scheduler;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
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
import net.sweenus.simplyswords.item.custom.EmberlashSwordItem;
import net.sweenus.simplyswords.registry.EffectRegistry;
import net.sweenus.simplyswords.registry.ItemsRegistry;
import net.sweenus.simplyswords.registry.SoundRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(EmberlashSwordItem.class)
public abstract class MixinEmberlashSword extends UniqueSwordItem {

    public MixinEmberlashSword(Tier toolMaterial, Properties settings) {
        super(toolMaterial, settings);
    }

    @Inject(method = "hurtEnemy", at = @At("HEAD"), cancellable = true)
    public void modifyHurtEnemyMethod(ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfoReturnable<Boolean> cir) {
        if (!Config.enableEmberlashChanges){
            return;
        }
        if (!attacker.level().isClientSide()) {

            int maxEffectLevel = Config.emberlashMaxSmoulderLevel - 2;
            float smoulderDamage = (float) Config.emberlashSmoulderDamageMultiplier;

            ServerLevel world = (ServerLevel) attacker.level();
            DamageSource damageSource = world.damageSources().generic();

            if (attacker instanceof Player player) {
                damageSource = attacker.damageSources().playerAttack(player);
            }

            MobEffect smoulderingType = EffectRegistry.SMOULDERING.get();
            Holder<MobEffect> smouldering = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(smoulderingType);

            if (target.hasEffect(smouldering)) {
                target.invulnerableTime = 0;
                MobEffectInstance smoulderingEffect = target.getEffect(smouldering);
                if (smoulderingEffect != null) {
                    target.hurt(damageSource, smoulderDamage * (smoulderingEffect.getAmplifier() + 1));
                }
            }

            if (target.hasEffect(smouldering)) {
                int effectLevel = target.getEffect(smouldering).getAmplifier();
                if (effectLevel > maxEffectLevel) effectLevel = maxEffectLevel;
                target.addEffect(new MobEffectInstance(smouldering, 20 * (effectLevel + 3), 0, false, true));
                for (int i = 0; i <= effectLevel; i ++){
                    target.addEffect(new MobEffectInstance(smouldering, 20 * (effectLevel - i + 2), i + 1, false, true));
                }
            } else {
                target.addEffect(new MobEffectInstance(smouldering, 20 * 2, 0, false, true));
            }
        }
        cir.setReturnValue(super.hurtEnemy(stack, target, attacker));
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    public void modifyUseMethod(Level world, Player user, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        if (!Config.enableEmberlashChanges){
            return;
        }
        if (!user.level().isClientSide()) {

            int cooldown = Config.emberlashCooldownTicks;
            float healPercentage = (float) Config.emberlashHealPercentage / 100F;
            double scale = Config.emberlashDashDistance / 3D;

            ItemStack mainHandItem = user.getItemInHand(InteractionHand.MAIN_HAND);
            ItemStack offHandItem = user.getItemInHand(InteractionHand.OFF_HAND);

            boolean hasTwoSwords = mainHandItem.is(ItemsRegistry.EMBERLASH.get()) && offHandItem.is(ItemsRegistry.EMBERLASH.get());
            boolean isFirstUse = !user.getPersistentData().getBoolean("EmberlashFirstDashUsed");

            boolean isMainHandUse = hand == InteractionHand.MAIN_HAND;
            boolean isOffHandItemNotOnCooldown = !user.getCooldowns().isOnCooldown(offHandItem.getItem());
            if (isMainHandUse && isOffHandItemNotOnCooldown) {
                user.getCooldowns().addCooldown(offHandItem.getItem(), 4);
            }

            user.swing(hand);
            world.playSound(null, user.blockPosition(), SoundRegistry.SPELL_FIRE.get(), user.getSoundSource(), 0.5F, 1.0F);

            Vec3 look = user.getLookAngle().normalize();
            if (user.isShiftKeyDown()) {
                look = look.scale(-1);
            }

            double angleY = Math.toDegrees(Math.asin(look.y));
            if ((Math.abs(angleY) <= 10) ||
                    (Config.emberlashIgnoreDownwardAngleOnGround && user.onGround() && angleY < 0)) {
                look = new Vec3(look.x, 0, look.z).normalize();
            }

            Vec3 dashVelocity = look.scale(scale);

            user.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 10, 4, false, false));

            user.setDeltaMovement(dashVelocity);
            ((ServerPlayer) user).connection.send(new ClientboundSetEntityMotionPacket(user));

            Scheduler.schedule(() -> {
                user.setDeltaMovement(dashVelocity);
                ((ServerPlayer) user).connection.send(new ClientboundSetEntityMotionPacket(user));

            }, 2, 0);

            Scheduler.schedule(() -> {
                user.setDeltaMovement(Vec3.ZERO);
                ((ServerPlayer) user).connection.send(new ClientboundSetEntityMotionPacket(user));
            }, 4, 0);

            boolean[] shouldDisableGravity = {false};

            Scheduler.schedule(() -> {
                if (!user.onGround()) {
                    user.setNoGravity(true);
                    shouldDisableGravity[0] = true;
                    user.hurtMarked = true;
                }
            }, 4, 0);

            Scheduler.schedule(() -> {
                if (shouldDisableGravity[0]) {
                    user.setNoGravity(false);
                    user.hurtMarked = true;
                }
            }, 10, 0);

            if (isFirstUse) {
                user.heal(user.getMaxHealth() * healPercentage);
            }

            if (hasTwoSwords && isFirstUse) {
                user.getCooldowns().addCooldown((EmberlashSwordItem) (Object) this, 8);
                user.getPersistentData().putBoolean("EmberlashFirstDashUsed", true);
            } else {
                user.getCooldowns().addCooldown((EmberlashSwordItem) (Object) this, cooldown);
                user.getPersistentData().putBoolean("EmberlashFirstDashUsed", false);
            }
        }
        cir.setReturnValue(super.use(world, user, hand));
    }

    @OnlyIn(Dist.CLIENT)
    @Inject(method = "appendHoverText", at = @At("HEAD"), cancellable = true)
    private void modifyTooltip(ItemStack itemStack, Item.TooltipContext tooltipContext, List<Component> tooltip, TooltipFlag tooltipFlag, CallbackInfo ci) {
        if (!Config.enableEmberlashChanges){
            return;
        }

        ci.cancel();

        int cooldown = Config.emberlashCooldownTicks;
        float floatCooldown = (float) cooldown / 20;
        int healPercentage = Config.emberlashHealPercentage;
        double distance = Config.emberlashDashDistance;
        float smoulderDamage = (float) Config.emberlashSmoulderDamageMultiplier;

        if (Screen.hasAltDown()){
            String translatedText = Component.translatable("tooltip.simply_swords_overhaul.emberlashsworditem_1").getString();
            for (String line : translatedText.split("\n")) tooltip.add(Component.literal(line));

            translatedText = Component.translatable("tooltip.simply_swords_overhaul.emberlashsworditem_2", distance, healPercentage).getString();
            for (String line : translatedText.split("\n")) tooltip.add(Component.literal(line));

            tooltip.add(Component.literal(" "));
            tooltip.add(Component.translatable("tooltip.simply_swords_overhaul.cooldown", floatCooldown)
                    .withStyle(ChatFormatting.BLUE));

            SimplySwordsAPI.appendTooltipGemSocketLogic(itemStack, tooltipContext, tooltip, tooltipFlag);

        } else if (Screen.hasShiftDown()) {
            String translatedText = Component.translatable("tooltip.simply_swords_overhaul.emberlashsworditem_1").getString();
            for (String line : translatedText.split("\n")) tooltip.add(Component.literal(line));

            translatedText = Component.translatable("tooltip.simply_swords_overhaul.emberlashsworditem_shift_1", smoulderDamage).getString();
            for (String line : translatedText.split("\n")) tooltip.add(Component.literal(line)
                    .withStyle(ChatFormatting.GRAY));


            translatedText = Component.translatable("tooltip.simply_swords_overhaul.emberlashsworditem_2", distance, healPercentage).getString();
            for (String line : translatedText.split("\n")) tooltip.add(Component.literal(line));

            translatedText = Component.translatable("tooltip.simply_swords_overhaul.emberlashsworditem_shift_2").getString();
            for (String line : translatedText.split("\n")) tooltip.add(Component.literal(line)
                    .withStyle(ChatFormatting.GRAY));


            tooltip.add(Component.literal(" "));
            tooltip.add(Component.translatable("tooltip.simply_swords_overhaul.cooldown", floatCooldown)
                    .withStyle(ChatFormatting.BLUE));

        } else {
            String translatedText = Component.translatable("tooltip.simply_swords_overhaul.emberlashsworditem_1").getString();
            for (String line : translatedText.split("\n")) tooltip.add(Component.literal(line));

            translatedText = Component.translatable("tooltip.simply_swords_overhaul.emberlashsworditem_2", distance, healPercentage).getString();
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
