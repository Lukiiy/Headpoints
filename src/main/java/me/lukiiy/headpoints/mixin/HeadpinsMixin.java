package me.lukiiy.headpoints.mixin;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.contextualbar.LocatorBarRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.level.Level;
import net.minecraft.world.waypoints.PartialTickSupplier;
import net.minecraft.world.waypoints.TrackedWaypoint;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LocatorBarRenderer.class)
public class HeadpinsMixin {
    @Shadow @Final private Minecraft minecraft;

    @Redirect(method = "method_70870", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lcom/mojang/blaze3d/pipeline/RenderPipeline;Lnet/minecraft/resources/ResourceLocation;IIIII)V"))
    private void headpoints$pins(GuiGraphics instance, RenderPipeline renderPipeline, ResourceLocation resourceLocation, int i, int j, int k, int l, int m, Entity entity, Level level, PartialTickSupplier pts, GuiGraphics instance2, int o, TrackedWaypoint t) {
        Player player = t.id().left().map(level::getPlayerByUUID).orElse(null);
        if (player == null) {
            instance.blitSprite(renderPipeline, resourceLocation, i, j, k, l, m);
            return;
        }

        PlayerSkin skin = minecraft.getSkinManager().createLookup(player.getGameProfile(), true).get();

        headpoints$head(instance, skin.body().texturePath(), i, j, 8, player);
    }

    @Unique
    private void headpoints$head(GuiGraphics instance, ResourceLocation tex, int x, int y, int s, Player player) {
        float uMin = 8f / 64f;
        float uMax = 16f / 64f;
        float vMin = 8f / 64f;
        float vMax = 16f / 64f;

        instance.blit(tex, x, y, x + s, y + s, uMin, uMax, vMin, vMax);

        if (player.isModelPartShown(PlayerModelPart.HAT)) {
            float huMin = 40f / 64f;
            float huMax = 48f / 64f;

            instance.blit(tex, x, y, x + s, y + s, huMin, huMax, vMin, vMax);
        }
    }
}
