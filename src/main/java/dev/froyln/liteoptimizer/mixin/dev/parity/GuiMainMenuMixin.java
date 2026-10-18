package dev.froyln.liteoptimizer.mixin.dev.parity;

import java.io.File;

import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.world.storage.ISaveHandler;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.froyln.liteoptimizer.config.Config;
import dev.froyln.liteoptimizer.dev.ParityRun;

/** Loads the harness world straight from the main menu (once). */
@Mixin(GuiMainMenu.class)
public abstract class GuiMainMenuMixin extends GuiScreen {
    private static boolean liteoptimizer$launched;

    @Inject(method = "initGui", at = @At("RETURN"))
    private void liteoptimizer$autoLoadWorld(CallbackInfo ci) {
        if (ParityRun.WORLD != null && !liteoptimizer$launched) {
            liteoptimizer$launched = true;
            File worldDir = new File(new File(this.mc.gameDir, "saves"), ParityRun.WORLD);
            if (!new File(worldDir, "level.dat").isFile()) {
                Config.LOG.error("[LiteOptimizer] parity world is missing level.dat: {}", worldDir.getAbsolutePath());
                return;
            }
            ISaveHandler saveHandler = this.mc.getSaveLoader().getSaveLoader(ParityRun.WORLD, false);
            if (saveHandler.loadWorldInfo() == null) {
                Config.LOG.error("[LiteOptimizer] parity world has unreadable level metadata: {}", worldDir.getAbsolutePath());
                return;
            }
            this.mc.launchIntegratedServer(ParityRun.WORLD, ParityRun.WORLD, null);
        }
    }
}
