package pasheadskins;

import com.danrus.pas.render.armorstand.PlayerArmorStandModel;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.model.geom.ModelPart;
import pasheadskins.mixin.ModelPartAccessor;

/**
 * Vanilla PlayerModel parents overlay cubes under the posed limb (hat under head,
 * jacket under body, sleeves under arms, pants under legs) with a local identity pose.
 * PAS instead keeps those overlays as root parts and copies the limb pose onto them.
 * Skin Layers 3D's offsets assume the vanilla child layout, so we re-parent PAS overlays
 * the same way and stop drawing them as extra root parts.
 */
public final class OverlayHierarchy {
	private static final Set<PlayerArmorStandModel> WIRED = Collections.newSetFromMap(new IdentityHashMap<>());

	private OverlayHierarchy() {
	}

	public static void wire(PlayerArmorStandModel model) {
		if (!WIRED.add(model)) {
			return;
		}

		adopt(model.head, "hat", model.hat);
		adopt(model.body, "jacket", model.jacket);
		adopt(model.leftArm, "left_sleeve", model.leftSleeve);
		adopt(model.rightArm, "right_sleeve", model.rightSleeve);
		adopt(model.leftLeg, "left_pants", model.leftPants);
		adopt(model.rightLeg, "right_pants", model.rightPants);
		adopt(model.leftSlimArm, "left_sleeve", model.leftSlimSleeve);
		adopt(model.rightSlimArm, "right_sleeve", model.rightSlimSleeve);
	}

	public static void flattenOverlays(PlayerArmorStandModel model) {
		flatten(model.hat);
		flatten(model.jacket);
		flatten(model.leftSleeve);
		flatten(model.rightSleeve);
		flatten(model.leftPants);
		flatten(model.rightPants);
		flatten(model.leftSlimSleeve);
		flatten(model.rightSlimSleeve);
	}

	public static boolean isRootOverlay(PlayerArmorStandModel model, ModelPart part) {
		return isChildOf(model.body, "jacket", model.jacket, part)
			|| isChildOf(model.leftArm, "left_sleeve", model.leftSleeve, part)
			|| isChildOf(model.rightArm, "right_sleeve", model.rightSleeve, part)
			|| isChildOf(model.leftLeg, "left_pants", model.leftPants, part)
			|| isChildOf(model.rightLeg, "right_pants", model.rightPants, part)
			|| isChildOf(model.leftSlimArm, "left_sleeve", model.leftSlimSleeve, part)
			|| isChildOf(model.rightSlimArm, "right_sleeve", model.rightSlimSleeve, part);
	}

	private static boolean isChildOf(ModelPart parent, String name, ModelPart child, ModelPart part) {
		return part == child && parent != null && parent.hasChild(name) && parent.getChild(name) == child;
	}

	private static void adopt(ModelPart parent, String name, ModelPart child) {
		if (parent == null || child == null || parent == child) {
			return;
		}
		if (parent.hasChild(name) && parent.getChild(name) == child) {
			return;
		}

		ModelPartAccessor access = (ModelPartAccessor) (Object) parent;
		Map<String, ModelPart> next = new LinkedHashMap<>(access.pasheadskins$getChildren());
		next.put(name, child);
		access.pasheadskins$setChildren(Collections.unmodifiableMap(next));
	}

	private static void flatten(ModelPart overlay) {
		overlay.x = 0.0F;
		overlay.y = 0.0F;
		overlay.z = 0.0F;
		overlay.xRot = 0.0F;
		overlay.yRot = 0.0F;
		overlay.zRot = 0.0F;
		overlay.xScale = 1.0F;
		overlay.yScale = 1.0F;
		overlay.zScale = 1.0F;
	}
}
