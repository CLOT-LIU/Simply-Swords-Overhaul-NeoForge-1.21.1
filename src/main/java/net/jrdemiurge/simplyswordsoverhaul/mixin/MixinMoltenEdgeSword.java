package net.jrdemiurge.simplyswordsoverhaul.mixin;

import net.jrdemiurge.simplyswordsoverhaul.Config;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.sweenus.simplyswords.api.SimplySwordsAPI;
import net.sweenus.simplyswords.item.UniqueSwordItem;
import net.sweenus.simplyswords.item.custom.MoltenEdgeSwordItem;
import net.sweenus.simplyswords.registry.ItemsRegistry;
import net.sweenus.simplyswords.registry.SoundRegistry;
import net.sweenus.simplyswords.util.HelperMethods;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(MoltenEdgeSwordItem.class)
public abstract class MixinMoltenEdgeSword extends UniqueSwordItem {

    public MixinMoltenEdgeSword(Tier toolMaterial, Properties settings) {
        super(toolMaterial, settings);
    }

    @Inject(method = "hurtEnemy", at = @At("HEAD"), cancellable = true)
    public void modifyHurtEnemyMethod(ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfoReturnable<Boolean> cir) {
        if (!Config.enableMoltenEdgeChanges){
            return;
        }
        if (!attacker.level().isClientSide()) {
            ServerLevel world = (ServerLevel)attacker.level();

            target.igniteForSeconds(3);
            world.playSound(null, attacker, SoundRegistry.ELEMENTAL_BOW_FIRE_SHOOT_IMPACT_03.get(), attacker.getSoundSource(), 0.4F, 2.0F);
        }
        cir.setReturnValue(super.hurtEnemy(stack, target, attacker));
    }

    @Inject(method = "inventoryTick", at = @At("HEAD"), cancellable = true)
    private void modifyInventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected, CallbackInfo ci) {
        if (!Config.enableMoltenEdgeChanges){
            return;
        }
        if (!world.isClientSide && entity instanceof Player player) {
            if (player.getItemBySlot(EquipmentSlot.MAINHAND) == stack || player.getItemBySlot(EquipmentSlot.OFFHAND) == stack) {
                ItemStack mainHandItem = player.getItemInHand(InteractionHand.MAIN_HAND);
                ItemStack offHandItem = player.getItemInHand(InteractionHand.OFF_HAND);
                boolean hasTwoSwords = mainHandItem.is(ItemsRegistry.MOLTEN_EDGE.get()) && offHandItem.is(ItemsRegistry.MOLTEN_EDGE.get());

                float missingHealthPercentage = 1.0F - (player.getHealth() / player.getMaxHealth());
                int effectLevel = (int) (missingHealthPercentage / 0.2F);
                effectLevel = effectLevel - 2;
                if (hasTwoSwords) effectLevel++;

                if (effectLevel >= 0) {
                    player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 100, effectLevel, false, false));
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 100, effectLevel, false, false));
                }
                if (effectLevel - 1 >= 0) {
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 100, effectLevel - 1, false, false));
                }
            }
        }

        Player player = (Player) entity;
        SimpleParticleType particlePassive = ParticleTypes.SMOKE;
        float healthPercentage = player.getHealth() / player.getMaxHealth();
        if (healthPercentage < 0.66) {
            particlePassive = ParticleTypes.LARGE_SMOKE;
        }
        if (healthPercentage < 0.33) {
            particlePassive = ParticleTypes.LAVA;
        }
        HelperMethods.createFootfalls(entity, stack, world, particlePassive, particlePassive, particlePassive, true);
        super.inventoryTick(stack, world, entity, slot, selected);
        ci.cancel();
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    public void modifyUseMethod(Level world, Player user, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        if (!Config.enableMoltenEdgeChanges){
            return;
        }
        if (!user.level().isClientSide()) {
            ItemStack offHandItem = user.getItemInHand(InteractionHand.OFF_HAND);
            boolean isMainHandUse = hand == InteractionHand.MAIN_HAND;
            boolean isOffHandItemNotOnCooldown = !user.getCooldowns().isOnCooldown(offHandItem.getItem());
            if (isMainHandUse && isOffHandItemNotOnCooldown) {
                user.getCooldowns().addCooldown(offHandItem.getItem(), 4);
            }

            world.playSound(null, user, SoundRegistry.ELEMENTAL_BOW_FIRE_SHOOT_IMPACT_03.get(), user.getSoundSource(), 0.4F, 2.0F);

            float newHealth = Math.max(1.0F, user.getHealth() - 4);
            user.setHealth(newHealth);
            user.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 20 * 10, 0, false, true));

            user.getCooldowns().addCooldown((MoltenEdgeSwordItem) (Object) this, 4);
        }
        cir.setReturnValue(super.use(world, user, hand));
    }

   @OnlyIn(Dist.CLIENT)
   @Inject(method = "appendHoverText", at = @At("HEAD"), cancellable = true)
   private void modifyTooltip(ItemStack itemStack, Item.TooltipContext tooltipContext, List<Component> tooltip, TooltipFlag tooltipFlag, CallbackInfo ci) {
       if (!Config.enableMoltenEdgeChanges){
           return;
       }
        ci.cancel();

        if (Screen.hasAltDown()){
            String translatedText = Component.translatable("tooltip.simply_swords_overhaul.moltenedgeitem_1").getString();
            for (String line : translatedText.split("\n"))tooltip.add(Component.literal(line));

            translatedText = Component.translatable("tooltip.simply_swords_overhaul.moltenedgeitem_2").getString();
            for (String line : translatedText.split("\n"))tooltip.add(Component.literal(line));

            SimplySwordsAPI.appendTooltipGemSocketLogic(itemStack, tooltipContext, tooltip, tooltipFlag);

        } else if (Screen.hasShiftDown()) {
            String translatedText = Component.translatable("tooltip.simply_swords_overhaul.moltenedgeitem_1").getString();
            for (String line : translatedText.split("\n")) tooltip.add(Component.literal(line));

            translatedText = Component.translatable("tooltip.simply_swords_overhaul.moltenedgeitem_shift").getString();
            for (String line : translatedText.split("\n")) tooltip.add(Component.literal(line)
                    .withStyle(ChatFormatting.GRAY));

            translatedText = Component.translatable("tooltip.simply_swords_overhaul.moltenedgeitem_2").getString();
            for (String line : translatedText.split("\n"))tooltip.add(Component.literal(line));

        } else {
            String translatedText = Component.translatable("tooltip.simply_swords_overhaul.moltenedgeitem_1").getString();
            for (String line : translatedText.split("\n")) tooltip.add(Component.literal(line));

            translatedText = Component.translatable("tooltip.simply_swords_overhaul.moltenedgeitem_2").getString();
            for (String line : translatedText.split("\n"))tooltip.add(Component.literal(line));

            SimplySwordsAPI.appendTooltipGemSocketLogic(itemStack, tooltipContext, tooltip, tooltipFlag);

            if (!tooltip.get(tooltip.size() - 1)
                    .getString().equals(Component.translatable("item.simplyswords.common.showtooltip").getString())) {
                tooltip.add(Component.literal(""));
            }
            tooltip.add(Component.translatable("tooltip.simply_swords_overhaul.shift"));
        }
    }
}
