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
	private boolean pasheadskins$slim;

	@Unique
	private boolean pasheadskins$usingHeadSkin;

	@Override
	public void pasheadskins$setHeadSkin(Identifier texture, boolean slim) {
		this.pasheadskins$texture = texture;
		this.pasheadskins$slim = slim;
		this.pasheadskins$usingHeadSkin = texture != null;
	}

	@Override
	public void pasheadskins$clearHeadSkin() {
		this.pasheadskins$texture = null;
		this.pasheadskins$slim = false;
		this.pasheadskins$usingHeadSkin = false;
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
}
