package pasheadskins;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import net.minecraft.core.Rotations;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.decoration.ArmorStand;

// Standing-still arm sway from HumanoidModel.setupAnim. Only used when the
// stand is still at the vanilla rest pose or a fully zeroed one.
final class HeadStandIdle {
	private static final float HEAD_EPS = 0.6F;
	private static final float PART_EPS = 0.15F;
	private static final Rotations ZERO = new Rotations(0.0F, 0.0F, 0.0F);

	private HeadStandIdle() {
	}

	static boolean isRestPose(ArmorStandRenderState state) {
		return matches(state, true) || matches(state, false);
	}

	static void apply(HumanoidModel<?> model, ArmorStandRenderState state) {
		reset(model.head);
		reset(model.hat);
		reset(model.body);
		reset(model.leftArm);
		reset(model.rightArm);
		reset(model.leftLeg);
		reset(model.rightLeg);

		if (struggling(state)) {
			applyStruggle(model, state);
		} else {
			float t = state.ageInTicks;
			model.rightArm.zRot += Mth.cos(t * 0.09F) * 0.05F + 0.05F;
			model.leftArm.zRot -= Mth.cos(t * 0.09F) * 0.05F + 0.05F;
			model.rightArm.xRot += Mth.sin(t * 0.067F) * 0.05F;
			model.leftArm.xRot -= Mth.sin(t * 0.067F) * 0.05F;
		}
	}

	// Torso stays planted. Breath is just a sickly squash/stretch on the
	// chest so 3d jacket layers follow the scale.
	static void overlayStruggle(HumanoidModel<?> model, ArmorStandRenderState state) {
		if (!struggling(state)) {
			return;
		}
		squashTorso(model, breath(state.ageInTicks), 0.0F);
	}

	private static boolean struggling(ArmorStandRenderState state) {
		return HeadSkinClientConfig.personality()
			&& state instanceof HeadStandRender head
			&& head.pasheadskins$asthmatic();
	}

	private static void applyStruggle(HumanoidModel<?> model, ArmorStandRenderState state) {
		float t = state.ageInTicks;
		float air = breath(t);
		float empty = 1.0F - air;
		float hitch = Mth.sin(t * 0.41F) > 0.93F ? 0.02F : 0.0F;
		squashTorso(model, air, hitch);

		model.head.xRot += 0.05F + empty * 0.06F - air * 0.045F + hitch * 1.4F;
		model.head.yRot += Mth.sin(t * 0.15F) * 0.015F;
		model.head.zRot += Mth.sin(t * 0.11F) * 0.01F;

		float lift = air * 0.12F;
		model.rightArm.zRot += 0.06F + lift * 0.14F;
		model.leftArm.zRot -= 0.06F + lift * 0.14F;
		model.rightArm.xRot += 0.04F + empty * 0.05F;
		model.leftArm.xRot += 0.04F + empty * 0.05F;
		model.rightArm.y -= lift * 0.18F;
		model.leftArm.y -= lift * 0.18F;

		model.rightArm.zRot += Mth.cos(t * 0.09F) * 0.02F;
		model.leftArm.zRot -= Mth.cos(t * 0.09F) * 0.02F;
	}

	private static void squashTorso(HumanoidModel<?> model, float air, float hitch) {
		float empty = 1.0F - air;
		model.body.xScale = 1.0F + air * 0.04F - empty * 0.028F;
		model.body.zScale = 1.0F + air * 0.055F - empty * 0.035F;
		model.body.yScale = 1.0F - air * 0.05F + empty * 0.032F - hitch;
	}

	// Uneven gasp: quick suck, a tiny hitch, then a long thin exhale.
	private static float breath(float t) {
		float wobble = Mth.sin(t * 0.017F) * 0.003F;
		float p = wrap01(t * (0.034F + wobble));
		if (p < 0.20F) {
			float g = p / 0.20F;
			return g * g * (3.0F - 2.0F * g);
		}
		if (p < 0.32F) {
			return 1.0F - 0.12F * ((p - 0.20F) / 0.12F);
		}
		float e = (p - 0.32F) / 0.68F;
		return 0.88F * (1.0F - e * e);
	}

	private static float wrap01(float value) {
		return value - (float) Math.floor(value);
	}

	private static boolean matches(ArmorStandRenderState state, boolean vanillaRest) {
		if (state.headPose == null || state.bodyPose == null
			|| state.leftArmPose == null || state.rightArmPose == null
			|| state.leftLegPose == null || state.rightLegPose == null) {
			return false;
		}
		Rotations head = vanillaRest ? ArmorStand.DEFAULT_HEAD_POSE : ZERO;
		Rotations body = vanillaRest ? ArmorStand.DEFAULT_BODY_POSE : ZERO;
		Rotations leftArm = vanillaRest ? ArmorStand.DEFAULT_LEFT_ARM_POSE : ZERO;
		Rotations rightArm = vanillaRest ? ArmorStand.DEFAULT_RIGHT_ARM_POSE : ZERO;
		Rotations leftLeg = vanillaRest ? ArmorStand.DEFAULT_LEFT_LEG_POSE : ZERO;
		Rotations rightLeg = vanillaRest ? ArmorStand.DEFAULT_RIGHT_LEG_POSE : ZERO;
		return closeHead(state.headPose, head)
			&& close(state.bodyPose, body)
			&& close(state.leftArmPose, leftArm)
			&& close(state.rightArmPose, rightArm)
			&& close(state.leftLegPose, leftLeg)
			&& close(state.rightLegPose, rightLeg);
	}

	private static boolean closeHead(Rotations pose, Rotations expected) {
		return Math.abs(pose.x() - expected.x()) <= HEAD_EPS
			&& Math.abs(pose.y() - expected.y()) <= HEAD_EPS
			&& Math.abs(StandSlotFlags.visualHeadZ(pose.z()) - expected.z()) <= HEAD_EPS;
	}

	private static boolean close(Rotations pose, Rotations expected) {
		return Math.abs(pose.x() - expected.x()) <= PART_EPS
			&& Math.abs(pose.y() - expected.y()) <= PART_EPS
			&& Math.abs(pose.z() - expected.z()) <= PART_EPS;
	}

	private static void reset(ModelPart part) {
		if (part != null) {
			part.resetPose();
		}
	}
}
