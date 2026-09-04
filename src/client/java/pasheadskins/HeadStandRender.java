package pasheadskins;

import net.minecraft.resources.Identifier;

public interface HeadStandRender {
	void pasheadskins$setHeadSkin(Identifier texture, boolean slim);

	void pasheadskins$clearHeadSkin();

	boolean pasheadskins$usingHeadSkin();

	boolean pasheadskins$slim();

	Identifier pasheadskins$texture();
}
