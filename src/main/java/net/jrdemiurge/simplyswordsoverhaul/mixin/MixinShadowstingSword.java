package net.jrdemiurge.simplyswordsoverhaul.mixin;

import net.jrdemiurge.simplyswordsoverhaul.Config;
import net.jrdemiurge.simplyswordsoverhaul.util.DamageTracker;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
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
import net.sweenus.simplyswords.item.custom.ShadowstingSwordItem;
import net.sweenus.simplyswords.registry.SoundRegistry;
import net.sweenus.simplyswords.util.HelperMethods;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ShadowstingSwordItem.class)
public abstract class MixinShadowstingSword extends UniqueSwordItem {

    public MixinShadowstingSword(Tier toolMaterial, Properties settings) {
        super(toolMaterial, settings);
    }

    @Inject(method = "hurtEnemy", at = @At("HEAD"), cancellable = true)
    public void modifyHurtEnemyMethod(ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfoReturnable<Boolean> cir) {
        if (!Config.enableShadowstingChanges){
            return;
        }
        if (!attacker.level().isClientSide()) {
            float armorDamageMultiplier = (float) Config.shadowstingArmorDamageMultiplier;

            float previousHealth = DamageTracker.getLastHealth(target.getUUID());
            if (previousHealth != -1.0F) {

                float damage= 0.0F;

                if (attacker instanceof Player player) {
                    ServerLevel serverLevel = (ServerLevel) attacker.level();
                    float f = (float)player.getAttributeValue(Attributes.ATTACK_DAMAGE);
                    float f1 = EnchantmentHelper.modifyDamage(serverLevel, player.getMainHandItem(), target,
                            player.damageSources().playerAttack(player), 0.0F);

                    float f2 = player.getAttackStrengthScale(0.5F);
                    f *= 0.2F + f2 * f2 * 0.8F;
                    f1 *= f2;
                    if (f > 0.0F || f1 > 0.0F) {
                        boolean flag = f2 > 0.9F;

                        boolean flag2 = flag && player.fallDistance > 0.0F && !player.onGround() && !player.onClimbable() && !player.isInWater() && !player.hasEffect(MobEffects.BLINDNESS) && !player.isPassenger() && target instanceof LivingEntity;
                        flag2 = flag2 && !player.isSprinting();
                        CriticalHitEvent hitResult = CommonHooks.fireCriticalHit(player, target, flag2, flag2 ? 1.5F : 1.0F);
                        flag2 = hitResult != null;
                        if (flag2) {
                            f *= hitResult.getDamageMultiplier();
                        }

                        f += f1;

                        damage = f;
                    }
                } else {
                    ServerLevel serverLevel = (ServerLevel) attacker.level();
                    float f = (float)attacker.getAttributeValue(Attributes.ATTACK_DAMAGE);
                    float f1 = EnchantmentHelper.modifyDamage(serverLevel, attacker.getMainHandItem(), target,
                            attacker.damageSources().mobAttack(attacker), 0.0F);
                    f += f1;

                    damage = f;
                }

                double armorValue = target.getAttributeValue(Attributes.ARMOR);
                damage *= (1.0F + (float)(armorValue * armorDamageMultiplier));

                if (damage != 0.0F) {
                    target.setHealth(previousHealth);
                    target.invulnerableTime = 0;
                    target.hurt(attacker.damageSources().indirectMagic(attacker, attacker), damage);
                }
            }
        }
        cir.setReturnValue(super.hurtEnemy(stack, target, attacker));
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    public void modifyUseMethod(Level world, Player user, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        if (!Config.enableShadowstingChanges){
            return;
        }
        if (!user.level().isClientSide()) {
            ItemStack offHandItem = user.getItemInHand(InteractionHand.OFF_HAND);
            boolean isMainHandUse = hand == InteractionHand.MAIN_HAND;
            boolean isOffHandItemNotOnCooldown = !user.getCooldowns().isOnCooldown(offHandItem.getItem());
            if (isMainHandUse && isOffHandItemNotOnCooldown) {
                user.getCooldowns().addCooldown(offHandItem.getItem(), 4);
            }

            double teleportDistance = Config.shadowstingTeleportDistance;
            int blindnessDuration = Config.shadowstingBlindnessDuration;
            int cooldown = Config.shadowstingCooldownTicks;
            float radius = 4.0F;

            Vec3 lookVec = user.getLookAngle();
            Vec3 horizontalLookVec = new Vec3(lookVec.x, 0, lookVec.z).normalize().scale(teleportDistance);

            if (user.isShiftKeyDown()) {
                horizontalLookVec = horizontalLookVec.scale(-1);
            }

            Vec3 targetPos = user.position().add(horizontalLookVec);

            if (world.getBlockState(BlockPos.containing(targetPos)).getCollisionShape(world, BlockPos.containing(targetPos)).isEmpty()) {
                world.playSound(null, user, SoundRegistry.ELEMENTAL_SWORD_EARTH_ATTACK_01.get(), user.getSoundSource(), 0.4F, 1.6F);

                AABB box = new AABB(user.getX() + (double) radius, user.getY() + (double) radius, user.getZ() + (double) radius,
                        user.getX() - (double) radius, user.getY() - (double) radius, user.getZ() - (double) radius);

                for (Entity entity : world.getEntities(user, box, EntitySelector.LIVING_ENTITY_STILL_ALIVE)) {
                    if (entity instanceof LivingEntity le) {
                        if (HelperMethods.checkFriendlyFire(le, user)) {
                            le.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 20 * blindnessDuration, 0), user);
                        }
                    }
                }

                double xpos = user.getX() - (double)(radius + 1);
                double ypos = user.getY();
                double zpos = user.getZ() - (double)(radius + 1);

                for(int i = (int) radius * 2; i > 0; --i) {
                    for(int j = (int) radius * 2; j > 0; --j) {
                        float choose = (float)(Math.random() * 1.0);
                        HelperMethods.spawnParticle(world, ParticleTypes.LARGE_SMOKE, xpos + (double)i + (double)choose, ypos + 0.4, zpos + (double)j + (double)choose, 0.0, 0.1, 0.0);
                        HelperMethods.spawnParticle(world, ParticleTypes.CAMPFIRE_COSY_SMOKE, xpos + (double)i + (double)choose, ypos + 0.1, zpos + (double)j + (double)choose, 0.0, 0.0, 0.0);
                        HelperMethods.spawnParticle(world, ParticleTypes.SMOKE, xpos + (double)i + (double)choose, ypos, zpos + (double)j + (double)choose, 0.0, 0.1, 0.0);
                    }
                }

                user.teleportTo(targetPos.x, user.getY(), targetPos.z);

                user.getCooldowns().addCooldown((ShadowstingSwordItem) (Object) this, cooldown);
            } else {
                user.getCooldowns().addCooldown((ShadowstingSwordItem) (Object) this, 10);
            }

        }
        cir.setReturnValue(super.use(world, user, hand));
    }

    @OnlyIn(Dist.CLIENT)
    @Inject(method = "appendHoverText", at = @At("HEAD"), cancellable = true)
    private void modifyTooltip(ItemStack itemStack, Item.TooltipContext tooltipContext, List<Component> tooltip, TooltipFlag tooltipFlag, CallbackInfo ci) {
        if (!Config.enableShadowstingChanges){
            return;
        }
        ci.cancel();

        float armorDamageMultiplier = (float) Config.shadowstingArmorDamageMultiplier;
        double teleportDistance = Config.shadowstingTeleportDistance;
        int cooldown = Config.shadowstingCooldownTicks;
        float floatCooldown = (float) cooldown / 20;

        if (Screen.hasAltDown()){
            String translatedText = Component.translatable("tooltip.simply_swords_overhaul.shadowstingitem_1").getString();
            for (String line : translatedText.split("\n")) tooltip.add(Component.literal(line));

            translatedText = Component.translatable("tooltip.simply_swords_overhaul.shadowstingitem_2", teleportDistance).getString();
            for (String line : translatedText.split("\n")) tooltip.add(Component.literal(line));

            tooltip.add(Component.literal(" "));
            tooltip.add(Component.translatable("tooltip.simply_swords_overhaul.cooldown", floatCooldown)
                    .withStyle(ChatFormatting.BLUE));

            SimplySwordsAPI.appendTooltipGemSocketLogic(itemStack, tooltipContext, tooltip, tooltipFlag);

        } else if (Screen.hasShiftDown()) {
            String translatedText = Component.translatable("tooltip.simply_swords_overhaul.shadowstingitem_1").getString();
            for (String line : translatedText.split("\n")) tooltip.add(Component.literal(line));

            translatedText = Component.translatable("tooltip.simply_swords_overhaul.shadowstingitem_shift_1", armorDamageMultiplier * 100).getString();
            for (String line : translatedText.split("\n")) tooltip.add(Component.literal(line)
                    .withStyle(ChatFormatting.GRAY));


            translatedText = Component.translatable("tooltip.simply_swords_overhaul.shadowstingitem_2", teleportDistance).getString();
            for (String line : translatedText.split("\n")) tooltip.add(Component.literal(line));

            translatedText = Component.translatable("tooltip.simply_swords_overhaul.shadowstingitem_shift_2").getString();
            for (String line : translatedText.split("\n")) tooltip.add(Component.literal(line)
                    .withStyle(ChatFormatting.GRAY));


            tooltip.add(Component.literal(" "));
            tooltip.add(Component.translatable("tooltip.simply_swords_overhaul.cooldown", floatCooldown)
                    .withStyle(ChatFormatting.BLUE));

        } else {
            String translatedText = Component.translatable("tooltip.simply_swords_overhaul.shadowstingitem_1").getString();
            for (String line : translatedText.split("\n")) tooltip.add(Component.literal(line));

            translatedText = Component.translatable("tooltip.simply_swords_overhaul.shadowstingitem_2", teleportDistance).getString();
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
