package dev.froyln.liteoptimizer.mixin.render.sodium.gui;

import me.jellysquid.mods.sodium.client.gui.SodiumOptionsGUI;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Slice;

/** Opens LiteOptimizer's options from the standard Video Settings button. */
@Mixin(GuiOptions.class)
public abstract class VideoSettingsMixin extends GuiScreen {
    @ModifyArg(method = "actionPerformed(Lnet/minecraft/client/gui/GuiButton;)V",
            slice = @Slice(from = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiVideoSettings;<init>(Lnet/minecraft/client/gui/GuiScreen;Lnet/minecraft/client/settings/GameSettings;)V")),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;displayGuiScreen(Lnet/minecraft/client/gui/GuiScreen;)V", ordinal = 0))
    private GuiScreen liteoptimizer$openSodiumOptions(GuiScreen screen) {
        return new SodiumOptionsGUI(this);
    }
}
