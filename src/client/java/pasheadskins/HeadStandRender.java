package pasheadskins;

import net.minecraft.resources.Identifier;

public interface HeadStandRender {
	void pasheadskins$setHeadSkin(Identifier texture, boolean slim);

	void pasheadskins$setCapeTextures(Identifier cape, Identifier elytra);

	void pasheadskins$clearHeadSkin();

	boolean pasheadskins$usingHeadSkin();

	boolean pasheadskins$slim();

	Identifier pasheadskins$texture();

	Identifier pasheadskins$capeTexture();

	Identifier pasheadskins$elytraTexture();

	void pasheadskins$captureCapeBody(net.minecraft.client.model.geom.ModelPart body);

	void pasheadskins$applyCapeBody(net.minecraft.client.model.geom.ModelPart body);

	void pasheadskins$captureLimbs(net.minecraft.client.model.HumanoidModel<?> model);

	void pasheadskins$applyLimbs(net.minecraft.client.model.HumanoidModel<?> model);
}
