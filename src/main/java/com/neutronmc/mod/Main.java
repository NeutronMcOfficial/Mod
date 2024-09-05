package com.neutronmc.mod;

import imgui.*;
import imgui.type.ImInt;
import lombok.Getter;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.MissingResourceException;

public class Main implements ClientModInitializer {
	@Getter
	private static ImFont title_font;
	@Getter
	private static ImFont buttons_font;
	@Getter
	private static ImFont footer_font;
	private static int gFontTexture = -1;

	@Override
	public void onInitializeClient() {
		MinecraftClient mc = MinecraftClient.getInstance();
	}

	public static void initFonts(){
		ImGuiIO io = ImGui.getIO();
		ImFontAtlas fonts = io.getFonts();
		fonts.clear();
		ImFontGlyphRangesBuilder rangesBuilder = new ImFontGlyphRangesBuilder();
		rangesBuilder.addRanges(fonts.getGlyphRangesDefault());

		rangesBuilder.addChar('\u2318'); // Mac CMD
		rangesBuilder.addChar('\u2303'); // CTRL
		rangesBuilder.addChar('\u2387'); // Alt
		rangesBuilder.addChar('\u21E7'); // Shift (up arrow)
		rangesBuilder.addChar('\u2756'); // Super
		rangesBuilder.addChar('\u26A0'); // Warning symbol
		rangesBuilder.addChar('\u2190'); // Left Arrow
		rangesBuilder.addChar('\u2191'); // Up Arrow
		rangesBuilder.addChar('\u2192'); // Right Arrow
		rangesBuilder.addChar('\u2193'); // Down Arrow

		short[] glyphRanges = rangesBuilder.buildRanges();

		final ImFontConfig fontConfig = new ImFontConfig();
		fontConfig.setOversampleH(2);
		fontConfig.setOversampleV(2);

		title_font = fonts.addFontFromMemoryTTF(loadFont("font.ttf"), 96, fontConfig, glyphRanges);
		buttons_font = fonts.addFontFromMemoryTTF(loadFont("font.ttf"), 20, fontConfig, glyphRanges);
		footer_font = fonts.addFontFromMemoryTTF(loadFont("font.ttf"), 16, fontConfig, glyphRanges);

		fontConfig.setMergeMode(false);

		fonts.build();

		fontConfig.destroy();
		fonts.clearTexData();
		updateFontsTexture(io.getFonts());
	}

	public static byte[] loadFont(String name) {
		try {
			var resource = MinecraftClient.getInstance().getResourceManager().getResource(Identifier.of("neutronmc", name));
			if (resource.isEmpty()) throw new MissingResourceException("Missing font: " + name, "Font", "");
			try (InputStream is = resource.get().getInputStream()) {
				return is.readAllBytes();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	private static void updateFontsTexture(ImFontAtlas atlas) {
		if (Main.gFontTexture != -1) {
			GL11.glDeleteTextures(Main.gFontTexture);
		}

		final ImFontAtlas fontAtlas = atlas;
		final ImInt width = new ImInt();
		final ImInt height = new ImInt();
		final ByteBuffer buffer = fontAtlas.getTexDataAsRGBA32(width, height);

		Main.gFontTexture = GL11.glGenTextures();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, Main.gFontTexture);

		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

		GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width.get(), height.get(), 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

		fontAtlas.setTexID(Main.gFontTexture);
	}

}