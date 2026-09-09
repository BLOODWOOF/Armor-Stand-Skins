package pasheadskins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.object.armorstand.ArmorStandArmorModel;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import net.minecraft.world.entity.EquipmentSlot;

// Player armor mesh + stand posing. Overlay cubes on the armor model get
// hidden so we dont draw a second jacket with the chestplate texture.
public final class HeadStandArmor {
	private static ArmorModelSet<ArmorStandArmorModel> wide;
	private static ArmorModelSet<ArmorStandArmorModel> slim;

	private HeadStandArmor() {
	}

	public static void bake(EntityModelSet models) {
		if (models == null || wide != null) {
			return;
		}
		wide = ArmorModelSet.bake(ModelLayers.PLAYER_ARMOR, models, HeadStandArmorModel::new);
		slim = ArmorModelSet.bake(ModelLayers.PLAYER_SLIM_ARMOR, models, HeadStandArmorModel::new);
	}

	public static ArmorStandArmorModel get(boolean slimSkin, EquipmentSlot slot) {
		if (wide == null) {
			Minecraft client = Minecraft.getInstance();
			if (client != null) {
				bake(client.getEntityModels());
			}
		}
		ArmorModelSet<ArmorStandArmorModel> set = slimSkin ? slim : wide;
		if (set == null) {
			set = wide;
		}
		return set == null ? null : set.get(slot);
	}

	public static void copyPose(HumanoidModel<?> from, HumanoidModel<?> to) {
		if (from == null || to == null) {
			return;
		}
		copyPart(from.head, to.head);
		copyPart(from.hat, to.hat);
		copyPart(from.body, to.body);
		copyPart(from.leftArm, to.leftArm);
		copyPart(from.rightArm, to.rightArm);
		copyPart(from.leftLeg, to.leftLeg);
		copyPart(from.rightLeg, to.rightLeg);
	}

	private static void copyPart(ModelPart from, ModelPart to) {
		if (from == null || to == null) {
			return;
		}
		to.x = from.x;
		to.y = from.y;
		to.z = from.z;
		to.xRot = from.xRot;
		to.yRot = from.yRot;
		to.zRot = from.zRot;
		to.xScale = from.xScale;
		to.yScale = from.yScale;
		to.zScale = from.zScale;
	}

	static void hideOverlays(HumanoidModel<?> model) {
		if (model == null) {
			return;
		}
		hideChild(model.leftArm, "left_sleeve");
		hideChild(model.rightArm, "right_sleeve");
		hideChild(model.leftLeg, "left_pants");
		hideChild(model.rightLeg, "right_pants");
		hideChild(model.body, "jacket");
	}

	private static void hideChild(ModelPart parent, String name) {
		if (parent != null && parent.hasChild(name)) {
			parent.getChild(name).visible = false;
		}
	}

	private static final class HeadStandArmorModel extends ArmorStandArmorModel {
		private HeadStandArmorModel(ModelPart root) {
			super(root);
			HeadStandArmor.hideOverlays(this);
		}

		@Override
		public void setupAnim(ArmorStandRenderState state) {
			super.setupAnim(state);
			if (state instanceof HeadStandRender head) {
				head.pasheadskins$applyLimbs(this);
			}
			HeadStandArmor.hideOverlays(this);
		}
	}
}
