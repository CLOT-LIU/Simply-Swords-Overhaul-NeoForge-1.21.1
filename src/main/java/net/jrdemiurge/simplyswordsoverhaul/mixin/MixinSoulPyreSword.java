package net.jrdemiurge.simplyswordsoverhaul.mixin;

import net.jrdemiurge.simplyswordsoverhaul.Config;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.sweenus.simplyswords.api.SimplySwordsAPI;
import net.sweenus.simplyswords.item.UniqueSwordItem;
import net.sweenus.simplyswords.item.custom.SoulPyreSwordItem;
import net.sweenus.simplyswords.registry.SoundRegistry;
import net.sweenus.simplyswords.util.HelperMethods;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(SoulPyreSwordItem.class)
public abstract class MixinSoulPyreSword extends UniqueSwordItem {

    public MixinSoulPyreSword(Tier toolMaterial, Properties settings) {
        super(toolMaterial, settings);
    }

    @Inject(method = "hurtEnemy", at = @At("HEAD"), cancellable = true)
    public void modifyHurtEnemyMethod(ItemStack stack, LivingEntity target, LivingEntity attacker, CallbackInfoReturnable<Boolean> cir) {
        if (!Config.enableSoulPyreChanges){
            return;
        }
        if (!attacker.level().isClientSide()) {
            int witherDuration = Config.soulPyreWitherDuration;
            int witherLevel = Config.soulPyreWitherLevel;

            target.addEffect(new MobEffectInstance(MobEffects.WITHER, 20*witherDuration, witherLevel - 1));
        }
        cir.setReturnValue(super.hurtEnemy(stack, target, attacker));
    }

    @Inject(method = "inventoryTick", at = @At("HEAD"), cancellable = true)
    private void modifyInventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean selected, CallbackInfo ci) {
        if (!Config.enableSoulPyreChanges){
            return;
        }

        HelperMethods.createFootfalls(entity, stack, world, ParticleTypes.SOUL_FIRE_FLAME, ParticleTypes.SOUL_FIRE_FLAME, ParticleTypes.MYCELIUM, true);
        HelperMethods.createFootfalls(entity, stack, world, ParticleTypes.SMALL_FLAME, ParticleTypes.SMALL_FLAME, ParticleTypes.MYCELIUM, false);
        super.inventoryTick(stack, world, entity, slot, selected);
        ci.cancel();
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    public void modifyUseMethod(Level world, Player user, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        if (!Config.enableSoulPyreChanges){
            return;
        }
        if (!user.level().isClientSide()) {
            ItemStack offHandItem = user.getItemInHand(InteractionHand.OFF_HAND);
            boolean isMainHandUse = hand == InteractionHand.MAIN_HAND;
            boolean isOffHandItemNotOnCooldown = !user.getCooldowns().isOnCooldown(offHandItem.getItem());
            if (isMainHandUse && isOffHandItemNotOnCooldown) {
                user.getCooldowns().addCooldown(offHandItem.getItem(), 4);
            }

            double maxAbilityDistance = Config.soulPyreMaxAbilityDistance;
            double teleportDistance = Config.soulPyreTeleportDistance;
            int cooldown = Config.soulPyreCooldownTicks;
            Vec3 lookVec = user.getLookAngle().normalize();
            Vec3 start = user.position().add(0, user.getEyeHeight(), 0);
            Vec3 end = start.add(lookVec.normalize().scale(maxAbilityDistance));

            EntityHitResult hitResult = ProjectileUtil.getEntityHitResult(world, user, start, end, user.getBoundingBox().inflate(32), e -> e instanceof LivingEntity && e != user);

            if (hitResult != null) {
                Entity target = hitResult.getEntity();
                Vec3 horizontalLookVec = new Vec3(lookVec.x, 0, lookVec.z).normalize().scale(teleportDistance);
                Vec3 teleportPos = user.position().add(horizontalLookVec);
                if (world.getBlockState(BlockPos.containing(teleportPos)).getCollisionShape(world, BlockPos.containing(teleportPos)).isEmpty()) {
                    target.teleportTo(teleportPos.x, teleportPos.y, teleportPos.z);
                    world.playSound(null, user, SoundRegistry.ELEMENTAL_SWORD_SCIFI_ATTACK_01.get(), user.getSoundSource(), 0.3F, 1.0F);
                    user.getCooldowns().addCooldown((SoulPyreSwordItem) (Object) this, cooldown);
                } else {
                    user.getCooldowns().addCooldown((SoulPyreSwordItem) (Object) this, 5);
                }
            } else {
                user.getCooldowns().addCooldown((SoulPyreSwordItem) (Object) this, 5);
            }
        }
        cir.setReturnValue(super.use(world, user, hand));
    }

    @OnlyIn(Dist.CLIENT)
    @Inject(method = "appendHoverText", at = @At("HEAD"), cancellable = true)
    private void modifyTooltip(ItemStack itemStack, Item.TooltipContext tooltipContext, List<Component> tooltip, TooltipFlag tooltipFlag, CallbackInfo ci) {
        if (!Config.enableSoulPyreChanges){
            return;
        }
        ci.cancel();

        int witherDuration = Config.soulPyreWitherDuration;
        int witherLevel = Config.soulPyreWitherLevel;
        double maxAbilityDistance = Config.soulPyreMaxAbilityDistance;
        int cooldown = Config.soulPyreCooldownTicks;
        float floatCooldown = (float) cooldown / 20;

        String translatedText = Component.translatable("tooltip.simply_swords_overhaul.solupyreitem", witherLevel, witherDuration, maxAbilityDistance).getString();

        for (String line : translatedText.split("\n")) {
            tooltip.add(Component.literal(line));
        }

        tooltip.add(Component.literal(" "));
        tooltip.add(Component.translatable("tooltip.simply_swords_overhaul.cooldown", floatCooldown)
                .withStyle(ChatFormatting.BLUE));

        SimplySwordsAPI.appendTooltipGemSocketLogic(itemStack, tooltipContext, tooltip, tooltipFlag);
    }
}
