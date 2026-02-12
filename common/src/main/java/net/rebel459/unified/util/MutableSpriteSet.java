package net.rebel459.unified.util;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.particle.ParticleResources;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.RandomSource;

import java.util.List;

public class MutableSpriteSet implements SpriteSet {
        private List<TextureAtlasSprite> sprites;

        public MutableSpriteSet() {}

        public TextureAtlasSprite get(int i, int j) {
            return this.sprites.get(i * (this.sprites.size() - 1) / j);
        }

        public TextureAtlasSprite get(RandomSource randomSource) {
            return this.sprites.get(randomSource.nextInt(this.sprites.size()));
        }

        public TextureAtlasSprite first() {
            return this.sprites.getFirst();
        }

        public void rebind(List<TextureAtlasSprite> list) {
            this.sprites = ImmutableList.copyOf(list);
        }
}