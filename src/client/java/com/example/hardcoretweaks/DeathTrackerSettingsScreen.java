package com.example.hardcoretweaks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.joml.Matrix3x2fStack;

public class DeathTrackerSettingsScreen extends Screen {
   private final Screen parent;
   private final DeathTrackerConfig cfg;
   private int counterX;
   private int counterY;
   private float scale;
   private DeathTrackerLanguage language;
   private DeathTrackerFont currentFont;
   private DeathTrackerTextCase currentCase;
   private boolean dragging;
   private int dragOffX;
   private int dragOffY;
   private static final int PAD = 4;
   private int labelW;
   private int labelH;
   private Button scaleDisplayBtn;
   private Button langDisplayBtn;
   private Button fontDisplayBtn;
   private Button caseDisplayBtn;
   private ColorSliderButton rSlider;
   private ColorSliderButton gSlider;
   private ColorSliderButton bSlider;

   private class ColorSliderButton extends AbstractSliderButton {
      private final String prefix;
      private final java.util.function.Consumer<Integer> setter;

      public ColorSliderButton(int x, int y, int width, int height, String prefix, int initialValue, java.util.function.Consumer<Integer> setter) {
         super(x, y, width, height, Component.literal(prefix + initialValue), initialValue / 255.0);
         this.prefix = prefix;
         this.setter = setter;
      }

      @Override
      protected void updateMessage() {
         this.setMessage(Component.literal(this.prefix + (int)(this.value * 255.0)));
      }

      @Override
      protected void applyValue() {
         this.setter.accept((int)(this.value * 255.0));
      }

      public void nudge(int delta) {
         int current = (int)(this.value * 255.0);
         int next = Math.max(0, Math.min(255, current + delta));
         this.value = next / 255.0;
         this.updateMessage();
         this.applyValue();
      }
   }

   public DeathTrackerSettingsScreen(Screen parent, DeathTrackerConfig cfg) {
      super(Component.literal("Death Tracker Settings"));
      this.parent = parent;
      this.cfg = cfg;
      this.counterX = cfg.x;
      this.counterY = cfg.y;
      this.scale = cfg.scale;
      this.language = cfg.getLanguage();
      this.currentFont = cfg.getFont();
      this.currentCase = cfg.getTextCase();
   }

   protected void init() {
      this.recomputeLabelSize();
      this.counterX = this.clampX(this.counterX);
      this.counterY = this.clampY(this.counterY);
      int gap = 4;
      int smallW = 24;
      int midW = 90;
      int centerY = this.height / 2;

      int totalW = smallW + gap + midW + gap + smallW;
      int startX = (this.width - totalW) / 2;

      int rY = centerY - 110;
      this.addRenderableWidget(Button.builder(Component.literal("‹"), b -> this.rSlider.nudge(-1)).bounds(startX, rY, smallW, 20).build());
      this.rSlider = this.addRenderableWidget(new ColorSliderButton(startX + smallW + gap, rY, midW, 20, "R: ", this.cfg.r, val -> this.cfg.r = val));
      this.addRenderableWidget(Button.builder(Component.literal("›"), b -> this.rSlider.nudge(1)).bounds(startX + smallW + gap + midW + gap, rY, smallW, 20).build());

      int gY = centerY - 84;
      this.addRenderableWidget(Button.builder(Component.literal("‹"), b -> this.gSlider.nudge(-1)).bounds(startX, gY, smallW, 20).build());
      this.gSlider = this.addRenderableWidget(new ColorSliderButton(startX + smallW + gap, gY, midW, 20, "G: ", this.cfg.g, val -> this.cfg.g = val));
      this.addRenderableWidget(Button.builder(Component.literal("›"), b -> this.gSlider.nudge(1)).bounds(startX + smallW + gap + midW + gap, gY, smallW, 20).build());

      int bY = centerY - 58;
      this.addRenderableWidget(Button.builder(Component.literal("‹"), b -> this.bSlider.nudge(-1)).bounds(startX, bY, smallW, 20).build());
      this.bSlider = this.addRenderableWidget(new ColorSliderButton(startX + smallW + gap, bY, midW, 20, "B: ", this.cfg.b, val -> this.cfg.b = val));
      this.addRenderableWidget(Button.builder(Component.literal("›"), b -> this.bSlider.nudge(1)).bounds(startX + smallW + gap + midW + gap, bY, smallW, 20).build());

      int langRowY = centerY - 32;
      this.addRenderableWidget(Button.builder(Component.literal("‹"), b -> this.cycleLanguage(-1)).bounds(startX, langRowY, smallW, 20).build());
      this.langDisplayBtn = this.addRenderableWidget(
         Button.builder(Component.literal("Language: " + this.cfg.getLanguage().displayName), (btn) -> {}).bounds(startX + smallW + gap, langRowY, midW, 20).build()
      );
      this.langDisplayBtn.active = false;
      this.addRenderableWidget(
         Button.builder(Component.literal("›"), b -> this.cycleLanguage(1)).bounds(startX + smallW + gap + midW + gap, langRowY, smallW, 20).build()
      );
      int fontRowY = centerY - 6;
      this.addRenderableWidget(Button.builder(Component.literal("‹"), b -> this.cycleFont(-1)).bounds(startX, fontRowY, smallW, 20).build());
      this.fontDisplayBtn = this.addRenderableWidget(
         Button.builder(Component.literal("Font: " + this.cfg.getFont().displayName), (btn) -> {}).bounds(startX + smallW + gap, fontRowY, midW, 20).build()
      );
      this.fontDisplayBtn.active = false;
      this.addRenderableWidget(
         Button.builder(Component.literal("›"), b -> this.cycleFont(1)).bounds(startX + smallW + gap + midW + gap, fontRowY, smallW, 20).build()
      );
      int caseRowY = centerY + 20;
      this.addRenderableWidget(Button.builder(Component.literal("‹"), b -> this.cycleCase(-1)).bounds(startX, caseRowY, smallW, 20).build());
      this.caseDisplayBtn = this.addRenderableWidget(
         Button.builder(Component.literal("Text Case: " + this.cfg.getTextCase().displayName), (btn) -> {}).bounds(startX + smallW + gap, caseRowY, midW, 20).build()
      );
      this.caseDisplayBtn.active = false;
      this.addRenderableWidget(
         Button.builder(Component.literal("›"), b -> this.cycleCase(1)).bounds(startX + smallW + gap + midW + gap, caseRowY, smallW, 20).build()
      );
      int scaleRowY = centerY + 46;
      int scaleMidW = 70;
      int scaleTotalW = smallW + gap + scaleMidW + gap + smallW;
      int scaleStartX = (this.width - scaleTotalW) / 2;
      this.addRenderableWidget(Button.builder(Component.literal("−"), b -> this.changeScale(-0.25F)).bounds(scaleStartX, scaleRowY, smallW, 20).build());
      this.scaleDisplayBtn = this.addRenderableWidget(
         Button.builder(Component.literal(this.scaleText()), b -> {}).bounds(scaleStartX + smallW + gap, scaleRowY, scaleMidW, 20).build()
      );
      this.scaleDisplayBtn.active = false;
      this.addRenderableWidget(
         Button.builder(Component.literal("+"), b -> this.changeScale(0.25F))
            .bounds(scaleStartX + smallW + gap + scaleMidW + gap, scaleRowY, smallW, 20)
            .build()
      );
      int btnW = 90;
      int btnH = 20;
      int presetY = centerY + 72;
      DeathTrackerSettingsScreen.Preset[] presets = DeathTrackerSettingsScreen.Preset.values();
      int presetTotalW = presets.length * btnW + (presets.length - 1) * 8;
      int presetStartX = (this.width - presetTotalW) / 2;

      for (int i = 0; i < presets.length; i++) {
         DeathTrackerSettingsScreen.Preset p = presets[i];
         this.addRenderableWidget(
            Button.builder(Component.literal(p.label), b -> this.applyPreset(p)).bounds(presetStartX + i * (btnW + 8), presetY, btnW, btnH).build()
         );
      }

      int doneY = centerY + 98;
      int ctrW = 80;
      int gap2 = 8;
      int trioW = ctrW + gap2 + ctrW + gap2 + 100;
      int trioX = (this.width - trioW) / 2;
      this.addRenderableWidget(
         Button.builder(Component.literal("Center H"), b -> this.counterX = this.clampX((this.width - this.scaledW()) / 2))
            .bounds(trioX, doneY, ctrW, 20)
            .build()
      );
      this.addRenderableWidget(
         Button.builder(Component.literal("Center V"), b -> this.counterY = this.clampY((this.height - this.scaledH()) / 2))
            .bounds(trioX + ctrW + gap2, doneY, ctrW, 20)
            .build()
      );
      this.addRenderableWidget(
         Button.builder(Component.literal("Done"), b -> this.saveAndClose()).bounds(trioX + ctrW + gap2 + ctrW + gap2, doneY, 100, 20).build()
      );
   }

   public void extractRenderState(GuiGraphicsExtractor gfx, int mouseX, int mouseY, float delta) {
      gfx.centeredText(this.font, this.title, this.width / 2, 10, -1);
      gfx.centeredText(this.font, "Drag to move  •  Scroll or +/− to resize  •  Arrow keys to nudge", this.width / 2, 24, -5592406);
      int labelColor = -3355444;
      gfx.centeredText(this.font, "Color R:", this.width / 2 - 120, this.height / 2 - 105, labelColor);
      gfx.centeredText(this.font, "Color G:", this.width / 2 - 120, this.height / 2 - 79, labelColor);
      gfx.centeredText(this.font, "Color B:", this.width / 2 - 120, this.height / 2 - 53, labelColor);
      gfx.centeredText(this.font, "Language:", this.width / 2 - 120, this.height / 2 - 27, labelColor);
      gfx.centeredText(this.font, "Font:", this.width / 2 - 120, this.height / 2 - 1, labelColor);
      gfx.centeredText(this.font, "Case:", this.width / 2 - 120, this.height / 2 + 25, labelColor);
      gfx.centeredText(this.font, "Size:", this.width / 2 - 120, this.height / 2 + 51, labelColor);
      this.drawPreviewLabel(gfx);
      super.extractRenderState(gfx, mouseX, mouseY, delta);
   }

   private void drawPreviewLabel(GuiGraphicsExtractor gfx) {
      int sw = this.scaledW();
      int sh = this.scaledH();
      gfx.fill(this.counterX, this.counterY, this.counterX + sw, this.counterY + sh, -1442840576);
      gfx.outline(this.counterX, this.counterY, sw, sh, -12276993);
      String preview = this.currentCase.apply(this.language.format(0L));
      Matrix3x2fStack ps = gfx.pose();
      ps.pushMatrix();
      ps.translate(this.counterX + 4.0F * this.scale, this.counterY + 4.0F * this.scale);
      ps.scale(this.scale, this.scale);
      int color = 0xFF000000 | (this.cfg.r << 16) | (this.cfg.g << 8) | this.cfg.b;
      gfx.text(this.currentFont.getFont(this.font, this.minecraft.fontFilterFishy), preview, 0, 0, color, true);
      ps.popMatrix();
   }

   public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
      if (event.button() == 0 && this.isOverLabel((int)event.x(), (int)event.y())) {
         this.dragging = true;
         this.dragOffX = (int)event.x() - this.counterX;
         this.dragOffY = (int)event.y() - this.counterY;
         return true;
      } else {
         return super.mouseClicked(event, doubleClick);
      }
   }

   public boolean mouseReleased(MouseButtonEvent event) {
      if (event.button() == 0 && this.dragging) {
         this.dragging = false;
         return true;
      } else {
         return super.mouseReleased(event);
      }
   }

   public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
      if (this.dragging) {
         this.counterX = this.clampX((int)event.x() - this.dragOffX);
         this.counterY = this.clampY((int)event.y() - this.dragOffY);
         return true;
      } else {
         return super.mouseDragged(event, dx, dy);
      }
   }

   public boolean mouseScrolled(double mx, double my, double scrollX, double scrollY) {
      if (this.isOverLabel((int)mx, (int)my)) {
         this.changeScale((float)scrollY * 0.25F);
         return true;
      } else {
         return super.mouseScrolled(mx, my, scrollX, scrollY);
      }
   }

   public boolean keyPressed(KeyEvent event) {
      int step = (event.modifiers() & 1) != 0 ? 5 : 1;
      switch (event.key()) {
         case 262:
            this.counterX = this.clampX(this.counterX + step);
            return true;
         case 263:
            this.counterX = this.clampX(this.counterX - step);
            return true;
         case 264:
            this.counterY = this.clampY(this.counterY + step);
            return true;
         case 265:
            this.counterY = this.clampY(this.counterY - step);
            return true;
         default:
            return super.keyPressed(event);
      }
   }

   public void onClose() {
      this.saveAndClose();
   }

   private void recomputeLabelSize() {
      Font f = this.currentFont.getFont(this.font, this.minecraft.fontFilterFishy);
      int maxW = 0;

      for (DeathTrackerLanguage l : DeathTrackerLanguage.values()) {
         maxW = Math.max(maxW, f.width(this.currentCase.apply(l.format(8888L))));
      }

      this.labelW = maxW + 8;
      this.labelH = 9 + 8;
   }

   private int scaledW() {
      return Math.round(this.labelW * this.scale);
   }

   private int scaledH() {
      return Math.round(this.labelH * this.scale);
   }

   private boolean isOverLabel(int mx, int my) {
      return mx >= this.counterX && mx <= this.counterX + this.scaledW() && my >= this.counterY && my <= this.counterY + this.scaledH();
   }

   private int clampX(int x) {
      return Math.max(0, Math.min(x, this.width - this.scaledW()));
   }

   private int clampY(int y) {
      return Math.max(0, Math.min(y, this.height - this.scaledH()));
   }

   private void cycleLanguage(int dir) {
      this.language = dir > 0 ? this.language.next() : this.language.prev();
      if (this.langDisplayBtn != null) {
         this.langDisplayBtn.setMessage(Component.literal(this.language.displayName));
      }

      this.recomputeLabelSize();
      this.counterX = this.clampX(this.counterX);
      this.counterY = this.clampY(this.counterY);
   }

   private void cycleFont(int dir) {
      this.currentFont = dir > 0 ? this.currentFont.next() : this.currentFont.prev();
      if (this.fontDisplayBtn != null) {
         this.fontDisplayBtn.setMessage(Component.literal(this.currentFont.displayName));
      }

      this.recomputeLabelSize();
      this.counterX = this.clampX(this.counterX);
      this.counterY = this.clampY(this.counterY);
   }

   private void cycleCase(int dir) {
      this.currentCase = dir > 0 ? this.currentCase.next() : this.currentCase.prev();
      if (this.caseDisplayBtn != null) {
         this.caseDisplayBtn.setMessage(Component.literal(this.currentCase.displayName));
      }

      this.recomputeLabelSize();
      this.counterX = this.clampX(this.counterX);
      this.counterY = this.clampY(this.counterY);
   }

   private void changeScale(float delta) {
      this.scale = Math.round(Math.max(0.5F, Math.min(5.0F, this.scale + delta)) / 0.25F) * 0.25F;
      this.counterX = this.clampX(this.counterX);
      this.counterY = this.clampY(this.counterY);
      if (this.scaleDisplayBtn != null) {
         this.scaleDisplayBtn.setMessage(Component.literal(this.scaleText()));
      }
   }

   private String scaleText() {
      return String.format("%.2fx", this.scale);
   }

   private void applyPreset(DeathTrackerSettingsScreen.Preset p) {
      int x = p.rawX;
      int y = p.rawY;
      switch (p) {
         case TOP_CENTER:
            x = (this.width - this.scaledW()) / 2;
            break;
         case TOP_RIGHT:
            x = this.width - this.scaledW() - 2;
            break;
         case BOT_LEFT:
            y = this.height - this.scaledH() - 2;
            break;
         case BOT_RIGHT:
            x = this.width - this.scaledW() - 2;
            y = this.height - this.scaledH() - 2;
      }

      this.counterX = this.clampX(x);
      this.counterY = this.clampY(y);
   }

   private void saveAndClose() {
      this.cfg.x = this.counterX;
      this.cfg.y = this.counterY;
      this.cfg.scale = this.scale;
      this.cfg.setLanguage(this.language);
      this.cfg.setFont(this.currentFont);
      this.cfg.setTextCase(this.currentCase);
      this.cfg.save();
      Minecraft.getInstance().setScreen(this.parent);
   }

   private enum Preset {
      TOP_LEFT("Top-Left", 2, 2),
      TOP_CENTER("Top-Center", -1, 2),
      TOP_RIGHT("Top-Right", -2, 2),
      BOT_LEFT("Bot-Left", 2, -1),
      BOT_RIGHT("Bot-Right", -2, -1);

      final String label;
      final int rawX;
      final int rawY;

      Preset(String label, int rx, int ry) {
         this.label = label;
         this.rawX = rx;
         this.rawY = ry;
      }
   }
}
