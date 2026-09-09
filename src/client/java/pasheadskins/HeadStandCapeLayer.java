package pasheadskins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.armorstand.ArmorStandArmorModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.EquipmentAssetManager;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;

public class HeadStandCapeLayer extends RenderLayer<ArmorStandRenderState, ArmorStandArmorModel> {
	private final HeadStandCapeModel model;
	private final EquipmentAssetManager equipmentAssets;

	public HeadStandCapeLayer(ArmorStandRenderer renderer, EntityRendererProvider.Context context) {
		super(renderer);
		this.model = new HeadStandCapeModel(context.bakeLayer(ModelLayers.PLAYER_CAPE));
		this.equipmentAssets = context.getEquipmentAssets();
	}

	@Override
	public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, ArmorStandRenderState state, float limbSwing, float limbSwingAmount) {
		if (state.isInvisible || !(state instanceof HeadStandRender head) || !head.pasheadskins$usingHeadSkin()) {
			return;
		}

		Identifier cape = head.pasheadskins$capeTexture();
		if (cape == null || hasLayer(state.chestEquipment, EquipmentClientInfo.LayerType.WINGS)) {
			return;
		}

		this.model.follow(this.getParentModel());
		poseStack.pushPose();
		if (hasLayer(state.chestEquipment, EquipmentClientInfo.LayerType.HUMANOID)) {
			poseStack.translate(0.0F, -0.053125F, 0.06875F);
		}
		collector.submitModel(
			this.model,
			state,
			poseStack,
			RenderTypes.entitySolid(cape),
			light,
			OverlayTexture.NO_OVERLAY,
			state.outlineColor,
			null
		);
		poseStack.popPose();
	}

	private boolean hasLayer(ItemStack stack, EquipmentClientInfo.LayerType type) {
		Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
		if (equippable == null || equippable.assetId().isEmpty()) {
			return false;
		}
		return !this.equipmentAssets.get(equippable.assetId().get()).getLayers(type).isEmpty();
	}
}
