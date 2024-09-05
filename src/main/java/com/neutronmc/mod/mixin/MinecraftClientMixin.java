package com.neutronmc.mod.mixin;

import com.neutronmc.mod.Main;
import com.neutronmc.mod.imgui.ImGuiImpl;
import imgui.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.util.Window;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
	@Shadow
	@Final
	private Window window;

	@Inject(method = "<init>", at = @At("RETURN"))
	public void initImGui(RunArgs args, CallbackInfo ci) {
		ImGuiImpl.create(window.getHandle());
		Main.initFonts();
	}

	@Inject(at = @At("HEAD"), method = "getLauncherBrand", cancellable = true)
	private static void getLauncherBrand(CallbackInfoReturnable<String> cir) {
		cir.setReturnValue("NeutronMC");
		cir.cancel();
	}
}