package cf.wynntils.core.framework.rendering;

import cf.wynntils.core.framework.rendering.colors.CustomColor;
import cf.wynntils.core.framework.rendering.textures.Texture;
import cf.wynntils.core.utils.ReflectionFields;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.ItemStack;

import java.awt.*;
import java.util.Arrays;

import static org.lwjgl.opengl.GL11.GL_QUADS;

/** ScreenRenderer      -SHCM
 * Extend this class whenever you want to render things on the screen
 * without context as to what they are.
 * The things rendered by this class would not be configurable without
 * them extending overlays!
 */
public class ScreenRenderer {
    protected static SmartFontRenderer fontRenderer = null;
    protected static Minecraft mc;
    protected static ScaledResolution screen = null;
    private static boolean rendering = false;
    private static float scale = 1.0f;
    private static float rotation = 0;
    private static boolean mask = false;
    private static Point drawingOrigin = new Point(0,0);
    private static Point transformationOrigin = new Point(0,0);
    protected static void transformationOrigin(int x, int y) {transformationOrigin.x = x; transformationOrigin.y = y;}protected static Point transformationOrigin() {return transformationOrigin;}
    private static RenderItem itemRenderer = null;

    public static boolean isRendering() { return rendering; }
    public static float getScale() { return scale; }
    public static float getRotation() { return rotation; }
    public static boolean isMasking() { return mask; }

    /** void refresh
     * Triggered by a slower loop(client tick), refresh
     * updates the screen resolution to match the window
     * size and sets the font renderer in until its ok.
     * Do not call this method from anywhere in the mod
     * except cf.wynntils.core.events.ClientEvents@onTick!
     */
    public static void refresh() {
        mc = Minecraft.getMinecraft();
        screen = new ScaledResolution(mc);
        if(fontRenderer == null)
            try {
                fontRenderer = new SmartFontRenderer();
            }
            catch (Exception e){}
            finally {
                fontRenderer.onResourceManagerReload(mc.getResourceManager());
            }
        if(itemRenderer == null)
            itemRenderer = (RenderItem)ReflectionFields.ItemRenderer_itemRenderer.getValue(Minecraft.getMinecraft().getItemRenderer());
    }

    /** void beginGL
     * Sets everything needed to start rendering,
     * sets the drawing origin to {x} and {y} and
     * allows the ability to render on the screen
     * (on the 2D plane).
     * Do not call this method unless you truly
     * understand what you're doing, This method
     * is being called before each overlay render
     * is called.
     *
     * @param x drawing origin's X
     * @param y drawing origin's Y
     */
    public static void beginGL(int x, int y) {
        if(rendering) return;
        rendering = true;
        GlStateManager.pushMatrix();
        drawingOrigin = new Point(x,y);
        transformationOrigin = new Point(0,0);
        resetScale();
        resetRotation();
        GlStateManager.enableAlpha();
        GlStateManager.color(1,1,1);
    }

    /** void endGL
     * Resets everything related to the ScreenRenderer
     * and stops the ability to render on screen(the
     * 2D plane).
     */
    public static void endGL() {
        if(!rendering) return;
        resetScale();
        resetRotation();
        GlStateManager.disableBlend();
        if(mask) clearMask();
        GlStateManager.translate(-drawingOrigin.x,-drawingOrigin.y,0);
        drawingOrigin = new Point(0,0);
        transformationOrigin = new Point(0,0);
        GlStateManager.popMatrix();
        GlStateManager.color(1,1,1);
        rendering = false;
    }

    /** void rotate
     * Appends rotation(in degrees) to the rotation
     * field and rotates the following renders around
     * (drawingOrigin+transformationOrigin).
     *
     * @param degrees amount of degrees to rotate
     */
    public static void rotate(float degrees) {
        if(!rendering) return;
        GlStateManager.translate(drawingOrigin.x+transformationOrigin.x,drawingOrigin.y+transformationOrigin.y,0);
        GlStateManager.rotate(degrees,0,0,1);
        GlStateManager.translate(-drawingOrigin.x-transformationOrigin.x,-drawingOrigin.y-transformationOrigin.y,0);
        rotation += degrees;
    }

    /** void resetRotation
     * Resets the rotation field and makes the
     * following renders render as usual(pre-scaling).
     */
    public static void resetRotation() {
        if(!rendering) return;
        if(rotation != 0.0f) {
            GlStateManager.translate(drawingOrigin.x+transformationOrigin.x,drawingOrigin.y+transformationOrigin.y,0);
            GlStateManager.rotate(rotation,0,0,-1);
            GlStateManager.translate(-drawingOrigin.x-transformationOrigin.x,-drawingOrigin.y-transformationOrigin.y,0);
            rotation = 0;
        }
    }

    /** void scale
     * Multiplies the scale field(in multiplier amount) by
     * {multiplier} and makes the following renders scale
     * by {multiplier} around (drawingOrigin+transformationOrigin).
     *
     * @param multiplier amount to multiply the current scale by
     */
    public static void scale(float multiplier) {
        if(!rendering) return;
        GlStateManager.translate(drawingOrigin.x+transformationOrigin.x,drawingOrigin.y+transformationOrigin.y,0);
        GlStateManager.scale(multiplier,multiplier,multiplier);
        GlStateManager.translate(-drawingOrigin.x-transformationOrigin.x,-drawingOrigin.y-transformationOrigin.y,0);
        scale *= multiplier;
    }

    /** void resetScale
     * Resets the scale field and makes the
     * following renders render as usual(pre-scaling).
     */
    public static void resetScale() {
        if(!rendering) return;
        if(scale != 1.0f) {
            float m = 1.0f/scale;
            GlStateManager.translate(drawingOrigin.x+transformationOrigin.x,drawingOrigin.y+transformationOrigin.y,0);
            GlStateManager.scale(m,m,m);
            GlStateManager.translate(-drawingOrigin.x-transformationOrigin.x,-drawingOrigin.y-transformationOrigin.y,0);
            scale = 1.0f;
        }
    }

    public static void createMask(Texture texture, int x1, int y1, int x2, int y2) {
        if(!rendering || mask) return;
        try {
            clearMask();
            GlStateManager.colorMask(false, false, false, false);
            texture.bind();
            GlStateManager.glBegin(GL_QUADS);
            GlStateManager.glTexCoord2f(0,0);
            GlStateManager.glVertex3f(x1+drawingOrigin.x,y1+drawingOrigin.y, 1000.0F);
            GlStateManager.glTexCoord2f(0,1);
            GlStateManager.glVertex3f(x1+drawingOrigin.x,y2+drawingOrigin.y, 1000.0F);
            GlStateManager.glTexCoord2f(1,1);
            GlStateManager.glVertex3f(x2+drawingOrigin.x,y2+drawingOrigin.y, 1000.0F);
            GlStateManager.glTexCoord2f(1,0);
            GlStateManager.glVertex3f(x2+drawingOrigin.x,y1+drawingOrigin.y, 1000.0F);
            GlStateManager.glEnd();
            GlStateManager.colorMask(true, true, true, true);
            GlStateManager.depthMask(false);
            GlStateManager.depthFunc(516);
            mask = true;
        } catch (Exception e) {
            e.printStackTrace();
            clearMask();
        }
    }

    public static void clearMask() {
        if(!mask || !rendering) return;
        try {
            GlStateManager.depthMask(true);
            GlStateManager.clear(256);
            GlStateManager.enableDepth();
            GlStateManager.depthFunc(515);
            GlStateManager.enableAlpha();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.clearColor(1.0F, 1.0F, 1.0F, 1.0F);
            mask = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /** float drawString
     * Draws a string using the current fontRenderer
     *
     * @param text the text to render
     * @param x x(from drawingOrigin) to render at
     * @param y y(from drawingOrigin) to render at
     * @param color the starting color to render(without codes, its basically the actual text's color)
     * @param alignment the alignment around {x} and {y} to render the text about
     * @param shadow should the text have a shadow behind it
     * @return the length of the rendered text in pixels(not taking scale into account)
     */
    public float drawString(String text, float x, float y, CustomColor color, SmartFontRenderer.TextAlignment alignment, SmartFontRenderer.TextShadow shadow) {
        if(!rendering) return -1f;
        return fontRenderer.drawString(text,drawingOrigin.x + (scale == 1f ? x : x/scale),drawingOrigin.y + (scale == 1f ? y : y/scale) ,color,alignment,shadow);
    }

    /**
     * Shorter overload for {{drawString}}
     */
    public float drawString(String text, float x, float y, CustomColor color) {
        return drawString(text, x, y, color, SmartFontRenderer.TextAlignment.LEFT_RIGHT, SmartFontRenderer.TextShadow.NORMAL);
    }

    /** float getStringWidth
     * Gets the length of the string in pixels without
     * drawing it (not taking scale into account).
     *
     * @param text the text to measure
     * @return the length of the text in pixels(not taking scale into account)
     */
    public float getStringWidth(String text) {
        if(!rendering) return -1f;
        if(text.isEmpty()) return -SmartFontRenderer.CHAR_SPACING;
        if(text.startsWith("ยง")) {
            if(text.charAt(1) == '[') {
                return getStringWidth(Arrays.toString(Arrays.copyOfRange(text.split("]"),1,text.length())));
            }
            else {
                return getStringWidth(text.substring(2));
            }
        }

        return fontRenderer.getCharWidth(text.charAt(0)) + SmartFontRenderer.CHAR_SPACING + getStringWidth(text.substring(1));
    }

    /** void drawRect
     * Draws a rectangle with a filled color.
     *
     * @param color color of the rectangle
     * @param x1 bottom-left x
     * @param y1 bottom-left y
     * @param x2 top-right x
     * @param y2 top-right y
     */
    public void drawRect(CustomColor color, int x1, int y1, int x2, int y2) {
        if(!rendering) return;
        GlStateManager.disableTexture2D();
        GlStateManager.enableAlpha();
        color.applyColor();
        int xMin = Math.min(x1, x2) + drawingOrigin.x,
            xMax = Math.max(x1, x2) + drawingOrigin.x,
            yMin = Math.min(y1, y2) + drawingOrigin.y,
            yMax = Math.max(y1, y2) + drawingOrigin.y;
        GlStateManager.glBegin(GL_QUADS);
        GlStateManager.glVertex3f(xMin, yMin, 0);
        GlStateManager.glVertex3f(xMin, yMax, 0);
        GlStateManager.glVertex3f(xMax, yMax, 0);
        GlStateManager.glVertex3f(xMax, yMin, 0);
        GlStateManager.glEnd();
        GlStateManager.enableTexture2D();
    }

    /** void drawRect
     * Draws a rectangle with a texture filling with texture
     * being defined by uv 0.0 -> 1.0 values.
     *
     * @param texture the texture to draw
     * @param x1 bottom-left x(on screen)
     * @param y1 bottom-left y(on screen)
     * @param x2 top-right x(on screen)
     * @param y2 top-right y(on screen)
     * @param tx1 bottom-left x of uv on texture(0.0 -> 1.0)
     * @param ty1 bottom-left y of uv on texture(0.0 -> 1.0)
     * @param tx2 top-right x of uv on texture(0.0 -> 1.0)
     * @param ty2 top-right y of uv on texture(0.0 -> 1.0)
     */
    public void drawRect(Texture texture, int x1, int y1, int x2, int y2, float tx1, float ty1, float tx2, float ty2) {
        if(!rendering || !texture.loaded) return;
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        texture.bind();

        int xMin = x1 + drawingOrigin.x,
            xMax = x2 + drawingOrigin.x,
            yMin = y1 + drawingOrigin.y,
            yMax = y2 + drawingOrigin.y;
        GlStateManager.glBegin(GL_QUADS);
        GlStateManager.glTexCoord2f(tx1, ty1);
        GlStateManager.glVertex3f(xMin, yMin, 0);
        GlStateManager.glTexCoord2f(tx1, ty2);
        GlStateManager.glVertex3f(xMin, yMax, 0);
        GlStateManager.glTexCoord2f(tx2, ty2);
        GlStateManager.glVertex3f(xMax, yMax, 0);
        GlStateManager.glTexCoord2f(tx2, ty1);
        GlStateManager.glVertex3f(xMax, yMin, 0);
        GlStateManager.glEnd();
    }

    /** void drawRect
     * Draws a rectangle with a texture filling with texture
     * being defined by uv in pixels.
     *
     * @param texture the texture to draw
     * @param x1 bottom-left x(on screen)
     * @param y1 bottom-left y(on screen)
     * @param x2 top-right x(on screen)
     * @param y2 top-right y(on screen)
     * @param tx1 bottom-left x of uv on texture(0 -> texture width)
     * @param ty1 bottom-left y of uv on texture(0 -> texture height)
     * @param tx2 top-right x of uv on texture(0 -> texture width)
     * @param ty2 top-right y of uv on texture(0 -> texture height)
     */
    public void drawRect(Texture texture, int x1, int y1, int x2, int y2, int tx1, int ty1, int tx2, int ty2) {
        drawRect(texture,x1,y1,x2,y2,(float)tx1/texture.width,(float)ty1/texture.height,(float)tx2/texture.width,(float)ty2/texture.height);
    }

    /** void drawRect
     * Overload to {{drawRect}} that matches the rectangle's size
     * to its texture mapping's size(pixels).
     *
     * @param width width of both the texture part and the rectangle
     * @param height height of both the texture part and the rectangle
     */
    public void drawRect(Texture texture, int x, int y, int tx, int ty, int width, int height) {
        drawRect(texture,x,y,x+width,y+height,tx,ty,tx+width,ty+height);
    }

    /** void drawRectF
     * Overload to {{drawRect}} that renders using floats,
     * note that the uv are in pixels and both the uv and the
     * position on screen are floats.
     *
     */
    public void drawRectF(Texture texture, float x1, float y1, float x2, float y2, float tx1, float ty1, float tx2, float ty2) {
        if(!rendering || !texture.loaded) return;
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        texture.bind();

        float xMin  = x1  + drawingOrigin.x,
              xMax  = x2  + drawingOrigin.x,
              yMin  = y1  + drawingOrigin.y,
              yMax  = y2  + drawingOrigin.y,
              txMin = tx1 / texture.width,
              txMax = tx2 / texture.width,
              tyMin = ty1 / texture.height,
              tyMax = ty2 / texture.height;
        GlStateManager.glBegin(GL_QUADS);
        GlStateManager.glTexCoord2f(txMin,tyMin);
        GlStateManager.glVertex3f(xMin, yMin, 0);
        GlStateManager.glTexCoord2f(txMin,tyMax);
        GlStateManager.glVertex3f(xMin, yMax, 0);
        GlStateManager.glTexCoord2f(txMax,tyMax);
        GlStateManager.glVertex3f(xMax, yMax, 0);
        GlStateManager.glTexCoord2f(txMax,tyMin);
        GlStateManager.glVertex3f(xMax, yMin, 0);
        GlStateManager.glEnd();
    }


    public void drawProgressBar(Texture texture, int x1, int y1, int x2, int y2, int ty1, int ty2, float progress, boolean background) {
        if (!rendering || !texture.loaded || (!background && progress == 0.0f)) return;
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        texture.bind();
        if(background) {
            float xMin = Math.min(x1, x2) + drawingOrigin.x,
                  xMax = Math.max(x1, x2) + drawingOrigin.x,
                  yMin = Math.min(y1, y2) + drawingOrigin.y,
                  yMax = Math.max(y1, y2) + drawingOrigin.y,
                  txMin = 0.0f,
                  txMax = 1.0f,
                  tyMin = (float) Math.min(ty1, ty2) / texture.height,
                  tyMax = (float) Math.max(ty1, ty2) / texture.height;

            GlStateManager.glBegin(GL_QUADS);
            GlStateManager.glTexCoord2f(txMin, tyMin);
            GlStateManager.glVertex3f(xMin, yMin, 0);
            GlStateManager.glTexCoord2f(txMin, tyMax);
            GlStateManager.glVertex3f(xMin, yMax, 0);
            GlStateManager.glTexCoord2f(txMax, tyMax);
            GlStateManager.glVertex3f(xMax, yMax, 0);
            GlStateManager.glTexCoord2f(txMax, tyMin);
            GlStateManager.glVertex3f(xMax, yMin, 0);
            GlStateManager.glEnd();
        } else {
            float xMin =  Math.min(x1, x2) + drawingOrigin.x,
                  xMax =  Math.max(x1, x2) + drawingOrigin.x,
                  yMin =  Math.min(y1, y2) + drawingOrigin.y,
                  yMax =  Math.max(y1, y2) + drawingOrigin.y - 1.5f,
                  txMin = 0.0f,
                  txMax = 1.0f,
                  tyMin = (float) Math.min(ty1, ty2) / texture.height,
                  tyMax = (float) Math.max(ty1, ty2) / texture.height;

            if(progress < 0.0f) {
                progress *= -1;
                xMin += (1.0f - progress) * (xMax - xMin); //TODO, fix this shit
                txMin += (1.0f - progress);
            } else {
                xMax -= (1.0f - progress) * (xMax - xMin);
                txMax -= (1.0f - progress);
            }

            GlStateManager.glBegin(GL_QUADS);
            GlStateManager.glTexCoord2f(txMin, tyMin);
            GlStateManager.glVertex3f(xMin, yMin, 0);
            GlStateManager.glTexCoord2f(txMin, tyMax);
            GlStateManager.glVertex3f(xMin, yMax, 0);
            GlStateManager.glTexCoord2f(txMax, tyMax);
            GlStateManager.glVertex3f(xMax, yMax, 0);
            GlStateManager.glTexCoord2f(txMax, tyMin);
            GlStateManager.glVertex3f(xMax, yMin, 0);
            GlStateManager.glEnd();
        }
    }


    public void drawProgressBar(Texture texture, int x1, int y1, int x2, int y2, int ty1, int ty2, float progress) {
        int half = (ty1 + ty2) / 2;
        drawProgressBar(texture, x1, y1, x2, y2, ty1, ty2-half+1, progress,true);
        drawProgressBar(texture, x1, y1, x2, y2, ty1+half+1, ty2, progress,false);
    }


    public void drawItemStack(ItemStack stack, int x, int y) {
        if(!rendering) return;
        RenderHelper.enableGUIStandardItemLighting();
        itemRenderer.zLevel = 200.0F;
        net.minecraft.client.gui.FontRenderer font = stack.getItem().getFontRenderer(stack);
        if (font == null) font = fontRenderer;
        itemRenderer.renderItemAndEffectIntoGUI(stack, x+drawingOrigin.x, y+drawingOrigin.y);
        itemRenderer.renderItemOverlayIntoGUI(font, stack, x+drawingOrigin.x, y+drawingOrigin.y, "");
        itemRenderer.zLevel = 0.0F;
        RenderHelper.disableStandardItemLighting();
    }
}