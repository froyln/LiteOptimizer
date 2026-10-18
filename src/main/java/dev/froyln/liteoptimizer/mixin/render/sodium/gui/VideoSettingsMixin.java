package dev.froyln.liteoptimizer.mixin.render.sodium.gui;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiVideoSettings;
import net.minecraft.client.resources.I18n;
import me.jellysquid.mods.sodium.client.gui.SodiumOptionsGUI;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Adds an entry point to Sodium's existing options screen on vanilla's Video Settings page. */
@Mixin(GuiVideoSettings.class)
public abstract class VideoSettingsMixin extends GuiScreen {
    private static final int LITEOPTIMIZER$SODIUM_BUTTON = 0x534F;

    @Inject(method = "initGui()V", at = @At("RETURN"))
    private void liteoptimizer$addSodiumButton(CallbackInfo ci) {
        this.buttonList.add(new GuiButton(LITEOPTIMIZER$SODIUM_BUTTON, this.width - 160,
                4, 150, 20, I18n.format("sodium.options.open_button")));
    }

    @Inject(method = "actionPerformed(Lnet/minecraft/client/gui/GuiButton;)V", at = @At("HEAD"), cancellable = true)
    private void liteoptimizer$openSodiumOptions(GuiButton button, CallbackInfo ci) {
        if (button.id == LITEOPTIMIZER$SODIUM_BUTTON) {
            ci.cancel();
            this.mc.displayGuiScreen(new SodiumOptionsGUI(this));
        }
    }
}
