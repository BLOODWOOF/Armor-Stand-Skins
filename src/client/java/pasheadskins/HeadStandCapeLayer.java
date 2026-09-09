package pasheadskins;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.object.armorstand.ArmorStandArmorModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ArmorStandRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;

public class HeadStandCapeLayer extends RenderLayer<ArmorStandRenderState, ArmorStandArmorModel> {
	private final HeadStandCapeModel model = new HeadStandCapeModel();

	public HeadStandCapeLayer(ArmorStandRenderer renderer) {
		super(renderer);
	}

	@Override
	public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, ArmorStandRenderState state, float limbSwing, float limbSwingAmount) {
		if (state.isInvisible || !(state instanceof HeadStandRender head) || !head.pasheadskins$usingHeadSkin()) {
			return;
		}

		Identifier cape = head.pasheadskins$capeTexture();
		if (cape == null || state.chestEquipment.is(Items.ELYTRA)) {
			return;
		}

		// Same parent attach as vanilla CapeLayer / Wavey Capes, snapshotted here so
		// deferred submit still hangs the cloak off the posed player mesh.
		poseStack.pushPose();
		this.getParentModel().root().translateAndRotate(poseStack);
		this.getParentModel().body.translateAndRotate(poseStack);
		if (!state.chestEquipment.isEmpty() && !state.chestEquipment.is(Items.ELYTRA)) {
			poseStack.translate(0.0F, -0.053125F, 0.06875F);
		}
		collector.submitModel(
			this.model,
			state,
			poseStack,
			RenderTypes.entityTranslucent(cape, false),
			light,
			OverlayTexture.NO_OVERLAY,
			state.outlineColor,
			null
		);
		poseStack.popPose();
	}
}
