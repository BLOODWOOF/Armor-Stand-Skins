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
}
