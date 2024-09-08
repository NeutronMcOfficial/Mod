package com.neutronmc.mod.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.neutronmc.mod.Main;
import com.neutronmc.mod.imgui.ImGuiImpl;
import imgui.ImGui;
import imgui.ImGuiStyle;
import imgui.ImVec2;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImString;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.DirectConnectScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DirectConnectScreen.class)
public abstract class DirectConnectionMixin extends Screen {
    @Shadow @Final private BooleanConsumer callback;
    @Shadow @Final private ServerInfo serverEntry;

    @Shadow protected abstract void saveAndClose();

    private static ImString inputBuffer = new ImString(256);
    private static boolean isValid = false;

    protected DirectConnectionMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    public void init(CallbackInfo ci) {
        ci.cancel();
        applyImGuiStyles();
    }
    @Inject(method = "setInitialFocus", at = @At("HEAD"), cancellable = true)
    public void set(CallbackInfo ci) {
        ci.cancel();
    }
    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    public void key(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        cir.cancel();
    }
    @Inject(method = "resize", at = @At("HEAD"), cancellable = true)
    public void key(MinecraftClient client, int width, int height, CallbackInfo ci) {
        ci.cancel();
        this.init(client, width, height);
    }
    @Inject(method = "saveAndClose", at = @At("HEAD"), cancellable = true)
    public void saveAndClose(CallbackInfo ci) {
        ci.cancel();
        this.serverEntry.address = inputBuffer.get();
        this.callback.accept(true);
    }
    @Inject(method = "removed", at = @At("HEAD"), cancellable = true)
    public void removed(CallbackInfo ci) {
        ci.cancel();
        this.client.options.lastServer = inputBuffer.get();
        this.client.options.write();
    }

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        RenderSystem.enableBlend();
        context.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        context.fill(0, 0, width, height, ColorHelper.Argb.getArgb(255, 15, 17, 20));
        context.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
        ImGuiImpl.draw(io -> {
            ImGui.begin("NeutronMc", ImGuiWindowFlags.NoTitleBar | ImGuiWindowFlags.NoResize | ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoBackground | ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoScrollWithMouse);
            ImGui.setWindowSize(MinecraftClient.getInstance().getWindow().getWidth(), MinecraftClient.getInstance().getWindow().getHeight());
            ImGui.setWindowPos(0,0);
            ImVec2 windowSize = ImGui.getWindowSize();

            ImGui.setCursorPosY(windowSize.y * 0.2f);
            ImGui.setCursorPosX((windowSize.x - (windowSize.x * 0.3f)) / 2);
            ImGui.pushFont(Main.getButtons_font());
            ImGui.pushStyleColor(ImGuiCol.FrameBg, 22 / 255F, 24 / 255F, 29 / 255F, 1.0F);
            ImGui.pushStyleColor(ImGuiCol.Border, 0F, 0F, 0F, 0F);
            ImGui.pushItemWidth(windowSize.x * 0.3f);
            String placeholderText = "Server IP";
            ImGui.getStyle().setFramePadding(40, 10);
            ImGui.pushStyleColor(ImGuiCol.Text, 1F, 1F, 1F, 1F);
            ImGui.inputTextWithHint("##Server IP", placeholderText, inputBuffer);
            ImGui.popItemWidth();
            ImGui.popStyleColor();
            ImGui.popStyleColor();
            ImGui.popStyleColor();
            ImGui.popFont();

            if (windowSize.y > 800) {
                ImGui.setCursorPosY(windowSize.y * 0.85f);
            } else {
                ImGui.setCursorPosY(windowSize.y * 0.8f);
            }
            ImGui.pushFont(Main.getButtons_font());
            isValid = ServerAddress.isValid(inputBuffer.get());
            if (!isValid) {
                ImGui.pushStyleColor(ImGuiCol.Button, 8 / 255F, 9 / 255F, 11 / 255F, 1.0F);
                ImGui.pushStyleColor(ImGuiCol.ButtonHovered, 8 / 255F, 9 / 255F, 11 / 255F, 1.0F);
                ImGui.pushStyleColor(ImGuiCol.ButtonActive, 8 / 255F, 9 / 255F, 11 / 255F, 1.0F);
            } else {
                ImGui.pushStyleColor(ImGuiCol.Button, 22 / 255F, 24 / 255F, 29 / 255F, 1.0F);
                ImGui.pushStyleColor(ImGuiCol.ButtonHovered,  33 / 255F, 36 / 255F, 44 / 255F, 1);
                ImGui.pushStyleColor(ImGuiCol.ButtonActive,  52 / 255F, 56 / 255F, 69 / 255F, 1);
            }
            int buttonWidth = 350;
            int buttonHeight = 40;
            ImGui.setCursorPosX((windowSize.x - buttonWidth) / 2);
            if (ImGui.button(Text.translatable("selectServer.select").getString(), buttonWidth, buttonHeight)) {
                if (isValid) {
                    this.saveAndClose();
                }
            }
            ImGui.popStyleColor();
            ImGui.popStyleColor();
            ImGui.popStyleColor();
            ImGui.setCursorPosY(windowSize.y * 0.9f);
            ImGui.setCursorPosX((windowSize.x - buttonWidth) / 2);
            if (ImGui.button(ScreenTexts.CANCEL.getString(), buttonWidth, buttonHeight)) {
                this.callback.accept(false);
            }
            ImGui.popFont();
            ImGui.end();
        });

        ci.cancel();
    }

    private void applyImGuiStyles() {
        ImGuiStyle style = ImGui.getStyle();

        style.setColor(ImGuiCol.Button, 22 / 255F, 24 / 255F, 29 / 255F, 1);
        style.setColor(ImGuiCol.ButtonHovered, 33 / 255F, 36 / 255F, 44 / 255F, 1);
        style.setColor(ImGuiCol.ButtonActive, 52 / 255F, 56 / 255F, 69 / 255F, 1);

        style.setWindowPadding(15, 15);
        style.setFramePadding(10, 10);
        style.setWindowRounding(10.0f);
        style.setFrameRounding(10.0f);
        style.setFrameBorderSize(0f);
    }
}
