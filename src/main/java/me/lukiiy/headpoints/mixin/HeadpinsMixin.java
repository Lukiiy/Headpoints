package me.lukiiy.headpoints.mixin;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.contextualbar.LocatorBarRenderer;
import net.minecraft.client.resources.WaypointStyle;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Avatar;
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
        Entity pinnedEntity = t.id().left().map(level::getEntity).orElse(null);

        PlayerSkin skin = null;
        boolean hat = true;

        if (pinnedEntity instanceof Player player) {
            skin = minecraft.getSkinManager().createLookup(player.getGameProfile(), true).get();
            hat = player.isModelPartShown(PlayerModelPart.HAT);
        }
        if (pinnedEntity instanceof ClientAvatarEntity clientAvatar) {
            skin = clientAvatar.getSkin();
            hat = pinnedEntity instanceof Avatar avatar && avatar.isModelPartShown(PlayerModelPart.HAT);
        }

        if (skin == null) {
            instance.blitSprite(renderPipeline, resourceLocation, i, j, k, l, m);
            return;
        }

        float dist = Math.min(pinnedEntity.distanceTo(minecraft.player), minecraft.options.renderDistance().get() * 16);

        headpoints$head(instance, skin.body().texturePath(), i, j, 8, hat, headpoints$scale(dist, minecraft.getWaypointStyles().get(t.icon().style)));
    }

    @Unique
    private static float headpoints$scale(float dist, WaypointStyle style) {
        float nearDist = style.nearDistance();
        float farDist = style.farDistance();

        if (dist <= nearDist) return 1;
        if (dist >= farDist) return .5f;

        float normalized = (dist - nearDist) / (farDist - nearDist);

        return Mth.lerp(normalized, 1, .5f);
    }

    @Unique
    private void headpoints$head(GuiGraphics instance, ResourceLocation texture, int x, int y, int base, boolean hat, float scale) {
        int scaledSize = Mth.ceil(base * scale);
        int fX = (x + base / 2) - scaledSize / 2;
        int fY = (y + base / 2) - scaledSize / 2;

        float uMin = 8f / 64f;
        float uMax = 16f / 64f;
        float vMin = 8f / 64f;
        float vMax = 16f / 64f;

        instance.blit(texture, fX, fY, fX + scaledSize, fY + scaledSize, uMin, uMax, vMin, vMax);

        if (hat) {
            float hatUMin = 40f / 64f;
            float hatUMax = 48f / 64f;

            instance.blit(texture, fX, fY, fX + scaledSize, fY + scaledSize, hatUMin, hatUMax, vMin, vMax);
        }
    }
}
