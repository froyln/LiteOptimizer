package dev.froyln.liteoptimizer.mixin.dev.parity;

import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.froyln.liteoptimizer.dev.ParityRun;

/** Loads the harness world straight from the main menu (once). */
@Mixin(GuiMainMenu.class)
public abstract class GuiMainMenuMixin extends GuiScreen {
    private static boolean liteoptimizer$launched;

    @Inject(method = "initGui", at = @At("RETURN"))
    private void liteoptimizer$autoLoadWorld(CallbackInfo ci) {
        if (ParityRun.WORLD != null && !liteoptimizer$launched) {
            liteoptimizer$launched = true;
            this.mc.launchIntegratedServer(ParityRun.WORLD, ParityRun.WORLD, null);
        }
    }
}
