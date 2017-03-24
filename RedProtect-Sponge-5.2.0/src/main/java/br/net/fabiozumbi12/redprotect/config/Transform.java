/*
 * * Copyright (C) 2015 Matt Baxter http://kitteh.org
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package br.net.fabiozumbi12.redprotect.config;

import de.icongmbh.oss.maven.plugin.javassist.ClassTransformer;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.StringMemberValue;

import java.util.Properties;

import br.net.fabiozumbi12.redprotect.RedProtect;

/**
 * Used purely to set the version value on the Plugin. Don't bother touching.
 */
public class Transform extends ClassTransformer {
    private String version;

    @Override
    protected boolean shouldTransform(final CtClass clazz) throws NotFoundException {
        CtClass spongeIRC = ClassPool.getDefault().get(RedProtect.class.getName());
        return !clazz.equals(spongeIRC) && clazz.subtypeOf(spongeIRC);
    }

    @Override
    protected void applyTransformations(CtClass clazz) throws Exception {
        AnnotationsAttribute attribute = (AnnotationsAttribute) clazz.getClassFile().getAttribute(AnnotationsAttribute.visibleTag);
        Annotation annotation = attribute.getAnnotation("org.spongepowered.api.plugin.Plugin");
        StringMemberValue version = (StringMemberValue) annotation.getMemberValue("version");
        version.setValue(this.version);
        attribute.setAnnotation(annotation);
    }

    @Override
    public void configure(final Properties properties) {
        if (properties == null || (this.version = properties.getProperty("version")) == null) {
            throw new AssertionError("Version not set!");
        }
    }
}