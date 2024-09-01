package com.neutronmc.mod.mixin;

import net.minecraft.client.util.Window;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Window.class)
public class WindowMixin {
    @Shadow @Final private long handle;

    @Inject(at = @At("HEAD"), method = "setTitle", cancellable = true)
    private void setTitle(String title, CallbackInfo ci) {
        GLFW.glfwSetWindowTitle(this.handle, title.replace("Minecraft", "NeutronMc"));
        ci.cancel();
    }
}
