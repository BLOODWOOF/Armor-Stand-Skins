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

		float t = state.ageInTicks;
		model.rightArm.zRot += Mth.cos(t * 0.09F) * 0.05F + 0.05F;
		model.leftArm.zRot -= Mth.cos(t * 0.09F) * 0.05F + 0.05F;
		model.rightArm.xRot += Mth.sin(t * 0.067F) * 0.05F;
		model.leftArm.xRot -= Mth.sin(t * 0.067F) * 0.05F;
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
