package br.net.fabiozumbi12.pixelvip;

import org.junit.Assert;
import org.junit.Test;
import org.spongepowered.api.plugin.Plugin;

import br.net.fabiozumbi12.pixelvip.PixelVip;

/**
 * Confirms the version has been set.
 */
public class AnnotatedVersionTest {
    @Test
    public void test() {
        Plugin annotation = PixelVip.class.getAnnotation(Plugin.class);
        Assert.assertNotNull("Failed to set version! Annotation eaten!", annotation);
        Assert.assertNotEquals("Failed to set version! Annotation not set!", "SET_BY_MAGIC", annotation.version());
        Assert.assertEquals("Failed to set version! Something horrible has happened!", "SET_BY_MAGIC", PixelVip.MAGIC_VERSION);
    }
}