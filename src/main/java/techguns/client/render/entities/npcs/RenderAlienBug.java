package techguns.client.render.entities.npcs;

import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import techguns.Techguns;
import techguns.client.models.npcs.ModelAlienBug;
import techguns.entities.npcs.AlienBug;

public class RenderAlienBug<T extends AlienBug> extends MobRenderer<T> {

	private static final ResourceLocation TEXTURE = new ResourceLocation(Techguns.MODID,"textures/entity/alienbug.png");

	public RenderAlienBug(EntityRendererManager rendermanagerIn) {
		super(rendermanagerIn, new ModelAlienBug(), 0.8f);
	}

	@Override
	protected ResourceLocation getEntityTexture(T entity) {
		return TEXTURE;
	}

}
