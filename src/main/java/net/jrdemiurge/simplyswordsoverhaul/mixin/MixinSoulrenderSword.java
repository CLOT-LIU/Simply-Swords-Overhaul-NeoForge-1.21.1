package net.jrdemiurge.simplyswordsoverhaul.mixin;

import net.jrdemiurge.simplyswordsoverhaul.Config;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.sweenus.simplyswords.api.SimplySwordsAPI;
import net.sweenus.simplyswords.item.UniqueSwordItem;
import net.sweenus.simplyswords.item.custom.SoulrenderSwordItem;
import net.sweenus.simplyswords.registry.SoundRegistry;
import net.sweenus.simplyswords.util.HelperMethods;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(SoulrenderSwordItem.class)
public abstract class MixinSoulrenderSword extends UniqueSwordItem {

    public MixinSoulrenderSword(Tier toolMaterial, Properties settings) {
        super(toolMaterial, settings);
    }

    @Inject(method = "hurtEnemy", at = @At("HEAD"), cancellable = true)
    public void modifyHurtEnemyMethod(ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfoReturnable<Boolean> cir) {
        if (!Config.enableSoulrenderChanges){
            return;
        }
        if (!attacker.level().isClientSide()) {
            ServerLevel world = (ServerLevel) attacker.level();

            int choose_sound = (int)(Math.random() * 30.0);
            if (choose_sound <= 10) {
                world.playSound(null, target, SoundRegistry.DARK_SWORD_ATTACK_WITH_BLOOD_01.get(), target.getSoundSource(), 0.4F, 1.5F);
            } else if (choose_sound <= 20) {
                world.playSound(null, target, SoundRegistry.DARK_SWORD_ATTACK_WITH_BLOOD_02.get(), target.getSoundSource(), 0.4F, 1.5F);
            } else if (choose_sound <= 30) {
                world.playSound(null, target, SoundRegistry.DARK_SWORD_ATTACK_WITH_BLOOD_03.get(), target.getSoundSource(), 0.4F, 1.5F);
            }

            applyDebuff(target);
        }
        cir.setReturnValue(super.hurtEnemy(stack, target, attacker));
    }

    private void applyDebuff(LivingEntity target) {
        int SlownessMaxLevel = Config.soulrenderMaxSlownessLevel;
        int WeaknessMaxLevel = Config.soulrenderMaxWeaknessLevel;
        int UnluckMaxLevel = Config.soulrenderMaxUnluckLevel;

        if (target.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) {
            MobEffectInstance slowness = target.getEffect(MobEffects.MOVEMENT_SLOWDOWN);
            int currentLevel = slowness.getAmplifier();
            if (currentLevel < SlownessMaxLevel - 1) {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 500, currentLevel + 1));
                return;
            } else if (!target.hasEffect(MobEffects.WEAKNESS)) {
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 500, 0));
                return;
            }
        } else {
            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 500, 0));
            return;
        }

        if (target.hasEffect(MobEffects.WEAKNESS)) {
            MobEffectInstance weakness = target.getEffect(MobEffects.WEAKNESS);
            int currentLevel = weakness.getAmplifier();
            if (currentLevel < WeaknessMaxLevel - 1) {
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 500, currentLevel + 1));
                return;
            } else if (!target.hasEffect(MobEffects.UNLUCK)) {
                target.addEffect(new MobEffectInstance(MobEffects.UNLUCK, 500, 0));
                return;
            }
        }

        if (target.hasEffect(MobEffects.UNLUCK)) {
            MobEffectInstance unlucky = target.getEffect(MobEffects.UNLUCK);
            int currentLevel = unlucky.getAmplifier();
            if (currentLevel < UnluckMaxLevel - 1) {
                target.addEffect(new MobEffectInstance(MobEffects.UNLUCK, 500, currentLevel + 1));
            }
        }
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    public void modifyUseMethod(Level world, Player user, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        if (!Config.enableSoulrenderChanges){
            return;
        }
        if (!user.level().isClientSide()) {
            ItemStack offHandItem = user.getItemInHand(InteractionHand.OFF_HAND);
            boolean isMainHandUse = hand == InteractionHand.MAIN_HAND;
            boolean isOffHandItemNotOnCooldown = !user.getCooldowns().isOnCooldown(offHandItem.getItem());
            if (isMainHandUse && isOffHandItemNotOnCooldown) {
                user.getCooldowns().addCooldown(offHandItem.getItem(), 4);
            }

            int baseDamage = Config.soulrenderBaseDamage;
            int stackPerStage = Config.soulrenderStacksPerStage;
            double maxHealPercent = Config.soulrenderMaxHealPercent;
            float healMultiplier = (float) Config.soulrenderHealMultiplier;
            int hradius = Config.soulrenderEffectRadius;
            int vradius = hradius / 2;

            int totalEffectLevel = 0;
            boolean hasDebuffedTarget = false;

            double x = user.getX();
            double y = user.getY();
            double z = user.getZ();
            ServerLevel sworld = (ServerLevel) world;

            AABB box = new AABB(x - hradius, y - vradius, z - hradius, x + hradius, y + vradius, z + hradius);
            List<LivingEntity> targets = sworld.getEntitiesOfClass(LivingEntity.class, box, EntitySelector.LIVING_ENTITY_STILL_ALIVE);

            for (LivingEntity target : targets) {
                if (!HelperMethods.checkFriendlyFire(target, user)) {
                    continue;
                }

                int debuffSum = 0;

                if (target.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) {
                    debuffSum += target.getEffect(MobEffects.MOVEMENT_SLOWDOWN).getAmplifier() + 1;
                    target.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
                }
                if (target.hasEffect(MobEffects.WEAKNESS)) {
                    debuffSum += target.getEffect(MobEffects.WEAKNESS).getAmplifier() + 1;
                    target.removeEffect(MobEffects.WEAKNESS);
                }
                if (target.hasEffect(MobEffects.UNLUCK)) {
                    debuffSum += target.getEffect(MobEffects.UNLUCK).getAmplifier() + 1;
                    target.removeEffect(MobEffects.UNLUCK);
                }

                if (debuffSum > 0) {
                    hasDebuffedTarget = true;
                    totalEffectLevel += debuffSum;

                    int damage = 0;
                    int remainingLevels = debuffSum;
                    int stage = baseDamage;

                    while (remainingLevels > 0) {
                        int levelsInStage = Math.min(stackPerStage, remainingLevels);
                        damage += levelsInStage * stage;
                        remainingLevels -= levelsInStage;
                        stage++;
                    }

                    target.invulnerableTime = 0;
                    target.hurt(user.damageSources().indirectMagic(user, user), damage);
                    world.playSound(null, target, SoundRegistry.DARK_SWORD_SPELL.get(), target.getSoundSource(), 0.5F, 2.0F);
                }
            }

            if (hasDebuffedTarget) {
                int healAmount = (int) (totalEffectLevel * healMultiplier);
                float maxHeal = user.getMaxHealth() * (float) maxHealPercent;
                user.heal(Math.min(healAmount, maxHeal));
            }
        }
        cir.setReturnValue(super.use(world, user, hand));
    }

    @OnlyIn(Dist.CLIENT)
    @Inject(method = "appendHoverText", at = @At("HEAD"), cancellable = true)
    private void modifyTooltip(ItemStack itemStack, Item.TooltipContext tooltipContext, List<Component> tooltip, TooltipFlag tooltipFlag, CallbackInfo ci) {
        if (!Config.enableSoulrenderChanges){
            return;
        }
        ci.cancel();

        int baseDamage = Config.soulrenderBaseDamage;
        int stackPerStage = Config.soulrenderStacksPerStage;
        double maxHealPercent = Config.soulrenderMaxHealPercent * 100;
        float healMultiplier = (float) Config.soulrenderHealMultiplier;

        if (Screen.hasAltDown()){
            String translatedText = Component.translatable("tooltip.simply_swords_overhaul.rendsworditem").getString();
            for (String line : translatedText.split("\n"))tooltip.add(Component.literal(line));

            SimplySwordsAPI.appendTooltipGemSocketLogic(itemStack, tooltipContext, tooltip, tooltipFlag);

        } else if (Screen.hasShiftDown()) {
            String translatedText = Component.translatable("tooltip.simply_swords_overhaul.rendsworditem").getString();
            for (String line : translatedText.split("\n")) tooltip.add(Component.literal(line));

            translatedText = Component.translatable("tooltip.simply_swords_overhaul.rendsworditem_shift",baseDamage, stackPerStage, baseDamage + 1, stackPerStage, baseDamage + 2, healMultiplier, maxHealPercent).getString();
            for (String line : translatedText.split("\n")) tooltip.add(Component.literal(line)
                        .withStyle(ChatFormatting.GRAY));

        } else {
            String translatedText = Component.translatable("tooltip.simply_swords_overhaul.rendsworditem").getString();
            for (String line : translatedText.split("\n")) tooltip.add(Component.literal(line));

            SimplySwordsAPI.appendTooltipGemSocketLogic(itemStack, tooltipContext, tooltip, tooltipFlag);

            if (!tooltip.get(tooltip.size() - 1)
                    .getString().equals(Component.translatable("item.simplyswords.common.showtooltip").getString())) {
                tooltip.add(Component.literal(""));
            }
            tooltip.add(Component.translatable("tooltip.simply_swords_overhaul.shift"));
        }
    }
}
