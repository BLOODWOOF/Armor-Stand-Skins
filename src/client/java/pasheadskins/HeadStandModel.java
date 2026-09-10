package pasheadskins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.object.armorstand.ArmorStandArmorModel;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;

public class HeadStandModel extends ArmorStandArmorModel {
	public final ModelPart leftSleeve;
	public final ModelPart rightSleeve;
	public final ModelPart leftPants;
	public final ModelPart rightPants;
	public final ModelPart jacket;
	private final boolean slim;

	public HeadStandModel(ModelPart root, boolean slim) {
		super(root);
		this.slim = slim;
		this.leftSleeve = this.leftArm.getChild("left_sleeve");
		this.rightSleeve = this.rightArm.getChild("right_sleeve");
		this.leftPants = this.leftLeg.getChild("left_pants");
		this.rightPants = this.rightLeg.getChild("right_pants");
		this.jacket = this.body.getChild("jacket");
	}

	public static HeadStandModel bake(boolean slim) {
		try {
			EntityModelSet models = Minecraft.getInstance().getEntityModels();
			if (models != null) {
				ModelPart root = models.bakeLayer(slim ? ModelLayers.PLAYER_SLIM : ModelLayers.PLAYER);
				return new HeadStandModel(root, slim);
			}
		} catch (Throwable ignored) {
		}
		ModelPart root = LayerDefinition.create(PlayerModel.createMesh(CubeDeformation.NONE, slim), 64, 64).bakeRoot();
		return new HeadStandModel(root, slim);
	}

	public boolean slim() {
		return this.slim;
	}

	@Override
	public void setupAnim(ArmorStandRenderState state) {
		boolean skinned = state instanceof HeadStandRender head && head.pasheadskins$usingHeadSkin();
		if (skinned && HeadStandIdle.isRestPose(state)) {
			HeadStandIdle.apply(this, state);
		} else {
			super.setupAnim(state);
		}
		showOverlay(this.hat);
		showOverlay(this.jacket);
		showOverlay(this.leftPants);
		showOverlay(this.rightPants);

		boolean arms = state.showArms;
		this.leftArm.visible = arms;
		this.rightArm.visible = arms;
		if (arms) {
			showOverlay(this.leftSleeve);
			showOverlay(this.rightSleeve);
		} else {
			this.leftSleeve.visible = false;
			this.rightSleeve.visible = false;
		}

		if (state instanceof HeadStandRender head && head.pasheadskins$usingHeadSkin()) {
			try {
				SkinLayerCompat.apply(this, head.pasheadskins$texture(), this.slim, state);
			} catch (Throwable ignored) {
			}
			if (!arms) {
				this.leftArm.visible = false;
				this.rightArm.visible = false;
				this.leftSleeve.visible = false;
				this.rightSleeve.visible = false;
			}
			head.pasheadskins$captureLimbs(this);
		}
	}

	private static void showOverlay(ModelPart part) {
		part.visible = true;
		part.skipDraw = false;
		part.xRot = 0.0F;
		part.yRot = 0.0F;
		part.zRot = 0.0F;
		part.xScale = 1.0F;
		part.yScale = 1.0F;
		part.zScale = 1.0F;
	}
}
