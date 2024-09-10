package com.neutronmc.mod.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.neutronmc.mod.Main;
import com.neutronmc.mod.imgui.ImGuiImpl;
import imgui.*;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiWindowFlags;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerWarningScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;

@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin extends Screen {
    private static Point2D[] points = new Point2D[0];
    private static Integer currentWidth = 0;
    private static Integer currentHeight = 0;

    protected TitleScreenMixin(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    public void init(CallbackInfo ci) {
        ci.cancel();
        applyImGuiStyles();
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

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        Screen currentScreen = MinecraftClient.getInstance().currentScreen;
        if (currentWidth != currentScreen.width || currentHeight != currentScreen.height) {
            points = getRandomPoints(this.width, this.height, 200);
            currentWidth = currentScreen.width;
            currentHeight = currentScreen.height;
        }
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
            ImGui.setCursorPosY(windowSize.y * 0.1F);
            ImGui.pushFont(Main.getTitle_font());
            String text = "NeutronMc";
            ImVec2 textSize = new ImGui().calcTextSize(text);
            ImGui.setCursorPosX((windowSize.x - textSize.x) * 0.5f);
            ImGui.text(text);
            ImGui.popFont();

            ImGui.setCursorPosY((windowSize.y - 125) / 2);
            int buttonWidth = 350;
            int buttonHeight = 40;
            ImGui.pushFont(Main.getButtons_font());
            ImGui.setCursorPosX((windowSize.x - buttonWidth) / 2);
            if (ImGui.button(Text.translatable("menu.singleplayer").getString(), buttonWidth, buttonHeight)) {
                this.client.setScreen(new SelectWorldScreen(this));
            }

            ImGui.spacing();

            ImGui.setCursorPosX((windowSize.x - buttonWidth) / 2);
            if (ImGui.button(Text.translatable("menu.multiplayer").getString(), buttonWidth, buttonHeight)) {
                Screen screen = this.client.options.skipMultiplayerWarning ? new MultiplayerScreen(this) : new MultiplayerWarningScreen(this);
                this.client.setScreen((Screen)screen);
            }

            ImGui.newLine();
            ImGui.setCursorPosX((windowSize.x - (2 * 165 + 20)) / 2);
            if (ImGui.button(Text.translatable("menu.options").getString(), 165, buttonHeight)) {
                this.client.setScreen(new OptionsScreen(this, this.client.options));
            }
            ImGui.sameLine(0, 20);
            if (ImGui.button(Text.translatable("menu.quit").getString(), 165, buttonHeight)) {
                this.client.scheduleStop();
            }
            ImGui.popFont();

            ImGui.pushFont(Main.getFooter_font());
            String footerText = "Not affiliated with Mojang or Microsoft. Do not distribute!";
            ImVec2 footerTextSize = new ImGui().calcTextSize(footerText);
            ImGui.setCursorPosX(windowSize.x - footerTextSize.x - 10);
            ImGui.setCursorPosY(windowSize.y - footerTextSize.y - 10);
            ImGui.text(footerText);
            ImGui.popFont();

            ImDrawList drawList = ImGui.getWindowDrawList();
            float cursorX = (float) MinecraftClient.getInstance().mouse.getX();
            float cursorY = (float) MinecraftClient.getInstance().mouse.getY();
            int color = ImGui.getColorU32(1, 1, 1, 1);

            for (Point2D point : points) {
                float x = (float) point.getX();
                float y = (float) point.getY();
                drawList.addCircleFilled(x, y, 5, color);
            }

            Point2D[] nearestPoints = getNearestPoints(points, cursorX, cursorY, 4);
            for (Point2D nearestPoint : nearestPoints) {
                double x = nearestPoint.getX();
                double y = nearestPoint.getY();
                drawList.addLine((float) x, (float) y, cursorX, cursorY, color, 2f);
            }

            ImGui.end();
        });
        ci.cancel();
    }

    private static Point2D[] getNearestPoints(Point2D[] points, double x, double y, int numPoints) {
        // Create a comparator to sort the points by their distance to (x, y)
        Comparator<Point2D> comparator = Comparator.comparingDouble(p -> p.distance(x, y));

        // Sort the points array based on their distance to the given (x, y)
        Arrays.sort(points, comparator);

        // Return the closest numPoints points (or fewer if the array has fewer points)
        return Arrays.copyOf(points, Math.min(numPoints, points.length));
    }


    private static Point2D[] getRandomPoints(double width, double height, int amount) {
        Point2D[] points = new Point2D[amount];
        for (int i = 0; i < amount; i++) {
            double x = ((Math.random() * (width - 0)) + 0) * 4;
            double y = ((Math.random() * (height - 0)) + 0) * 4;
            points[i] = new Point2D.Double(x, y);
        }
        return points;
    }

}
