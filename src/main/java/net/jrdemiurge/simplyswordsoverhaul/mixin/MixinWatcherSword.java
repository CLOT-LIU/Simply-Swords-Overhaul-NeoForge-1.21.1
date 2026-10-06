package net.jrdemiurge.simplyswordsoverhaul.mixin;

import net.jrdemiurge.simplyswordsoverhaul.Config;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.sweenus.simplyswords.api.SimplySwordsAPI;
import net.sweenus.simplyswords.item.UniqueSwordItem;
import net.sweenus.simplyswords.item.custom.WatcherSwordItem;
import net.sweenus.simplyswords.registry.SoundRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(WatcherSwordItem.class)
public abstract class MixinWatcherSword extends UniqueSwordItem {

    public MixinWatcherSword(Tier toolMaterial, Properties settings) {
        super(toolMaterial, settings);
    }

    @Inject(method = "hurtEnemy", at = @At("HEAD"), cancellable = true)
    public void modifyHurtEnemyMethod(ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfoReturnable<Boolean> cir) {
        if (!Config.enableWatcherChanges){
            return;
        }
        if (!attacker.level().isClientSide()) {
            double hitHealAmount = Config.watcherHitHealAmount;
            double killHealPercent = Config.watcherKillHealPercent;

            ServerLevel world = (ServerLevel) attacker.level();

            attacker.heal((float) hitHealAmount);

            if (target.getHealth() == 0.0F) {
                float healAmount = target.getMaxHealth() * (float) killHealPercent;
                attacker.heal(healAmount);
                world.playSound(null, target, SoundRegistry.ELEMENTAL_BOW_SCIFI_SHOOT_IMPACT_03.get(), target.getSoundSource(), 0.7F, 1.2F);
            }

        }
        cir.setReturnValue(super.hurtEnemy(stack, target, attacker));
    }

    @OnlyIn(Dist.CLIENT)
    @Inject(method = "appendHoverText", at = @At("HEAD"), cancellable = true)
    private void modifyTooltip(ItemStack itemStack, Item.TooltipContext tooltipContext, List<Component> tooltip, TooltipFlag tooltipFlag, CallbackInfo ci) {
        if (!Config.enableWatcherChanges){
            return;
        }
        ci.cancel();

        double hitHealAmount = Config.watcherHitHealAmount;
        double killHealPercent = Config.watcherKillHealPercent;

        String translatedText = Component.translatable("tooltip.simply_swords_overhaul.watchersworditem", killHealPercent * 100, hitHealAmount).getString();

        for (String line : translatedText.split("\n")) {
            tooltip.add(Component.literal(line));
        }

        SimplySwordsAPI.appendTooltipGemSocketLogic(itemStack, tooltipContext, tooltip, tooltipFlag);
    }
}
