package net.jrdemiurge.simplyswordsoverhaul.mixin;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.sweenus.simplyswords.util.HelperMethods;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HelperMethods.class)
public abstract class MixinHelperMethods {

    @Inject(method = "incrementStatusEffect", at = @At("HEAD"), cancellable = true, remap = false)
    private static void simplySwordsOverhaul$modifyIncrementStatusEffect(LivingEntity livingEntity, Holder<MobEffect> statusEffect, int duration, int amplifier, int amplifierMax, CallbackInfo ci) {
        if (livingEntity.hasEffect(statusEffect)) {
            int currentAmplifier = livingEntity.getEffect(statusEffect).getAmplifier();
            livingEntity.addEffect(new MobEffectInstance(statusEffect, duration, Math.min(amplifierMax, currentAmplifier + amplifier), false, false, true));
        } else {
            livingEntity.addEffect(new MobEffectInstance(statusEffect, duration, 0, false, false, true));
        }

        ci.cancel();
    }
}
