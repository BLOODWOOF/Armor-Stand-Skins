package pasheadskins.mixin;

import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import pasheadskins.HeadStandRender;

@Mixin(ArmorStandRenderState.class)
public class ArmorStandRenderStateMixin implements HeadStandRender {
	@Unique
	private Identifier pasheadskins$texture;

	@Unique
	private Identifier pasheadskins$capeTexture;

	@Unique
	private Identifier pasheadskins$elytraTexture;

	@Unique
	private boolean pasheadskins$slim;

	@Unique
	private boolean pasheadskins$usingHeadSkin;

	@Unique
	private boolean pasheadskins$capeBodySet;

	@Unique
	private float pasheadskins$capeX;

	@Unique
	private float pasheadskins$capeY;

	@Unique
	private float pasheadskins$capeZ;

	@Unique
	private float pasheadskins$capeXRot;

	@Unique
	private float pasheadskins$capeYRot;

	@Unique
	private float pasheadskins$capeZRot;

	@Unique
	private float pasheadskins$capeXScale = 1.0F;

	@Unique
	private float pasheadskins$capeYScale = 1.0F;

	@Unique
	private float pasheadskins$capeZScale = 1.0F;

	@Unique
	private boolean pasheadskins$limbsSet;

	@Unique
	private float[] pasheadskins$limbs;

	@Override
	public void pasheadskins$setHeadSkin(Identifier texture, boolean slim) {
		this.pasheadskins$texture = texture;
		this.pasheadskins$slim = slim;
		this.pasheadskins$usingHeadSkin = texture != null;
	}

	@Override
	public void pasheadskins$setCapeTextures(Identifier cape, Identifier elytra) {
		this.pasheadskins$capeTexture = cape;
		this.pasheadskins$elytraTexture = elytra;
	}

	@Override
	public void pasheadskins$clearHeadSkin() {
		this.pasheadskins$texture = null;
		this.pasheadskins$capeTexture = null;
		this.pasheadskins$elytraTexture = null;
		this.pasheadskins$slim = false;
		this.pasheadskins$usingHeadSkin = false;
		this.pasheadskins$capeBodySet = false;
		this.pasheadskins$limbsSet = false;
	}

	@Override
	public boolean pasheadskins$usingHeadSkin() {
		return this.pasheadskins$usingHeadSkin;
	}

	@Override
	public boolean pasheadskins$slim() {
		return this.pasheadskins$slim;
	}

	@Override
	public Identifier pasheadskins$texture() {
		return this.pasheadskins$texture;
	}

	@Override
	public Identifier pasheadskins$capeTexture() {
		return this.pasheadskins$capeTexture;
	}

	@Override
	public Identifier pasheadskins$elytraTexture() {
		return this.pasheadskins$elytraTexture;
	}

	@Override
	public void pasheadskins$captureCapeBody(net.minecraft.client.model.geom.ModelPart body) {
		this.pasheadskins$capeX = body.x;
		this.pasheadskins$capeY = body.y;
		this.pasheadskins$capeZ = body.z;
		this.pasheadskins$capeXRot = body.xRot;
		this.pasheadskins$capeYRot = body.yRot;
		this.pasheadskins$capeZRot = body.zRot;
		this.pasheadskins$capeXScale = body.xScale;
		this.pasheadskins$capeYScale = body.yScale;
		this.pasheadskins$capeZScale = body.zScale;
		this.pasheadskins$capeBodySet = true;
	}

	@Override
	public void pasheadskins$applyCapeBody(net.minecraft.client.model.geom.ModelPart body) {
		if (!this.pasheadskins$capeBodySet) {
			return;
		}
		body.x = this.pasheadskins$capeX;
		body.y = this.pasheadskins$capeY;
		body.z = this.pasheadskins$capeZ;
		body.xRot = this.pasheadskins$capeXRot;
		body.yRot = this.pasheadskins$capeYRot;
		body.zRot = this.pasheadskins$capeZRot;
		body.xScale = this.pasheadskins$capeXScale;
		body.yScale = this.pasheadskins$capeYScale;
		body.zScale = this.pasheadskins$capeZScale;
	}

	@Override
	public void pasheadskins$captureLimbs(net.minecraft.client.model.HumanoidModel<?> model) {
		if (model == null) {
			this.pasheadskins$limbsSet = false;
			return;
		}
		if (this.pasheadskins$limbs == null) {
			this.pasheadskins$limbs = new float[63];
		}
		int i = 0;
		i = pack(model.head, this.pasheadskins$limbs, i);
		i = pack(model.hat, this.pasheadskins$limbs, i);
		i = pack(model.body, this.pasheadskins$limbs, i);
		i = pack(model.leftArm, this.pasheadskins$limbs, i);
		i = pack(model.rightArm, this.pasheadskins$limbs, i);
		i = pack(model.leftLeg, this.pasheadskins$limbs, i);
		pack(model.rightLeg, this.pasheadskins$limbs, i);
		this.pasheadskins$limbsSet = true;
	}

	@Override
	public void pasheadskins$applyLimbs(net.minecraft.client.model.HumanoidModel<?> model) {
		if (!this.pasheadskins$limbsSet || this.pasheadskins$limbs == null || model == null) {
			return;
		}
		int i = 0;
		i = unpack(model.head, this.pasheadskins$limbs, i);
		i = unpack(model.hat, this.pasheadskins$limbs, i);
		i = unpack(model.body, this.pasheadskins$limbs, i);
		i = unpack(model.leftArm, this.pasheadskins$limbs, i);
		i = unpack(model.rightArm, this.pasheadskins$limbs, i);
		i = unpack(model.leftLeg, this.pasheadskins$limbs, i);
		unpack(model.rightLeg, this.pasheadskins$limbs, i);
	}

	@Unique
	private static int pack(net.minecraft.client.model.geom.ModelPart part, float[] out, int i) {
		if (part == null) {
			return i + 9;
		}
		out[i++] = part.x;
		out[i++] = part.y;
		out[i++] = part.z;
		out[i++] = part.xRot;
		out[i++] = part.yRot;
		out[i++] = part.zRot;
		out[i++] = part.xScale;
		out[i++] = part.yScale;
		out[i++] = part.zScale;
		return i;
	}

	@Unique
	private static int unpack(net.minecraft.client.model.geom.ModelPart part, float[] in, int i) {
		if (part == null) {
			return i + 9;
		}
		part.x = in[i++];
		part.y = in[i++];
		part.z = in[i++];
		part.xRot = in[i++];
		part.yRot = in[i++];
		part.zRot = in[i++];
		part.xScale = in[i++];
		part.yScale = in[i++];
		part.zScale = in[i++];
		return i;
	}
}
