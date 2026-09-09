package pasheadskins;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerCapeModel;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import org.joml.Quaternionf;

public class HeadStandCapeModel extends EntityModel<ArmorStandRenderState> {
	private static final float DEG = (float) (Math.PI / 180.0);

	private final ModelPart body;
	private final ModelPart cape;

	public HeadStandCapeModel() {
		this(PlayerCapeModel.createCapeLayer().bakeRoot());
	}

	private HeadStandCapeModel(ModelPart root) {
		super(root);
		this.body = root.getChild("body");
		this.cape = this.body.getChild("cape");
		this.body.setPos(0.0F, 0.0F, 0.0F);
		this.body.xRot = 0.0F;
		this.body.yRot = 0.0F;
		this.body.zRot = 0.0F;
		hide(root, "head");
		hide(root, "hat");
		hide(root, "right_arm");
		hide(root, "left_arm");
		hide(root, "right_leg");
		hide(root, "left_leg");
		hide(this.body, "jacket");
	}

	@Override
	public void setupAnim(ArmorStandRenderState state) {
		this.body.setPos(0.0F, 0.0F, 0.0F);
		this.body.xRot = 0.0F;
		this.body.yRot = 0.0F;
		this.body.zRot = 0.0F;
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
}
