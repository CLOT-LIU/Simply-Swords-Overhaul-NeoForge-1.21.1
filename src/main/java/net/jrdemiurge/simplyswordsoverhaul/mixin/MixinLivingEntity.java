package net.jrdemiurge.simplyswordsoverhaul.mixin;

import net.jrdemiurge.simplyswordsoverhaul.Config;
import net.jrdemiurge.simplyswordsoverhaul.util.DamageTracker;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.sweenus.simplyswords.registry.ItemsRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity {

    @Inject(method = "hurt", at = @At("HEAD"))
    private void simplySwordsOverhaul$recordPreDamageHealth(DamageSource pSource, float pAmount, CallbackInfoReturnable<Boolean> cir) {
        if (!Config.enableShadowstingChanges) {
            return;
        }
        LivingEntity target = (LivingEntity) (Object) this;
        Entity attacker = pSource.getEntity();

        if (attacker instanceof LivingEntity livingAttacker) {
            boolean hasShadowsting =
                    livingAttacker.getMainHandItem().is(ItemsRegistry.SHADOWSTING.get()) ||
                    livingAttacker.getOffhandItem().is(ItemsRegistry.SHADOWSTING.get());

            if (hasShadowsting) {
                DamageTracker.setLastHealth(target.getUUID(), target.getHealth());
            }
        }
    }
}
