package pasheadskins;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.object.armorstand.ArmorStandArmorModel;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import org.joml.Quaternionf;

public class HeadStandCapeModel extends EntityModel<ArmorStandRenderState> {
	private static final float DEG = (float) (Math.PI / 180.0);

	private final ModelPart body;
	private final ModelPart cape;
	private ModelPart standBody;

	public HeadStandCapeModel(ModelPart root) {
		super(root);
		this.body = root.getChild("body");
		this.cape = this.body.getChild("cape");
		hide(root, "head");
		hide(root, "hat");
		hide(root, "right_arm");
		hide(root, "left_arm");
		hide(root, "right_leg");
		hide(root, "left_leg");
	}

	public void follow(ArmorStandArmorModel stand) {
		this.standBody = stand.body;
	}

	@Override
	public void setupAnim(ArmorStandRenderState state) {
		if (this.standBody != null) {
			copy(this.body, this.standBody);
		}
		this.cape.resetPose();
		this.cape.rotateBy(new Quaternionf()
			.rotateY(-(float) Math.PI)
			.rotateX(6.0F * DEG)
			.rotateY((float) Math.PI));
	}

	private static void hide(ModelPart root, String name) {
		if (root.hasChild(name)) {
			root.getChild(name).visible = false;
		}
	}

	private static void copy(ModelPart to, ModelPart from) {
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
}
