package net.jrdemiurge.simplyswordsoverhaul.mixin;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.sweenus.simplyswords.api.SimplySwordsAPI;
import net.sweenus.simplyswords.power.GemPowerComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(SimplySwordsAPI.class)
public abstract class MixinSimplySwordsAPI {

    @OnlyIn(Dist.CLIENT)
    @Inject(method = "appendTooltipGemSocketLogic", at = @At("TAIL"), remap = false)
    private static void simplySwordsOverhaul$modifyAppendTooltipGemSocketLogic(ItemStack itemStack, Item.TooltipContext tooltipContext, List<Component> tooltip, TooltipFlag tooltipFlag, CallbackInfo ci) {
        GemPowerComponent component = SimplySwordsAPI.getComponent(itemStack);
        boolean runicFilled = component.hasRunicSlotFilled();
        boolean netherFilled = component.hasNetherSlotFilled();

        // New body: the API adds no separator when no socket is filled, but appends one
        // "empty slot" label per existing socket. Remove those trailing placeholders to
        // match the old cleanup behaviour.
        if (!runicFilled && !netherFilled) {
            int placeholders = (component.hasRunicPower() ? 1 : 0) + (component.hasNetherPower() ? 1 : 0);
            for (int i = 0; i < placeholders && !tooltip.isEmpty(); i++) {
                tooltip.remove(tooltip.size() - 1);
            }
            return;
        }

        // With Alt held and both sockets filled, collapse the blank separator line.
        if (Screen.hasAltDown() && runicFilled && netherFilled) {
            for (int i = tooltip.size() - 1; i >= 0; i--) {
                if (tooltip.get(i).getString().isEmpty()) {
                    tooltip.remove(i);
                    break;
                }
            }
        }
    }
}
