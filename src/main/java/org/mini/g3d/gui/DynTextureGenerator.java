package org.mini.g3d.gui;

import org.mini.g3d.core.Camera;
import org.mini.g3d.core.vector.Vector3f;
import org.mini.g3d.entity.Entity;
import org.mini.glwrap.GLFrameBuffer;
import org.mini.glwrap.GLUtil;
import org.mini.gui.GImage;

import java.util.ArrayList;
import java.util.List;

import static org.mini.gl.GL.*;

/**
 * 生成动态纹理,
 * 原理:
 * 把一些图片以2D方式渲染进一张fbo图片, 并用fbo作为纹理
 * 用处:
 * 可以用来作为bitmap font
 */
public class DynTextureGenerator {

    GLFrameBuffer dynamicContentFbo;
    GuiRenderer dynamicContentRenderer;
    Camera dynamicContentCamera;
    List<GuiTexture> dynamicContentRenderList;

    //保存每一格的待绘制图片
    List<GuiTexture>[] grids;
    long[] gridTimeOut;


    float texW, texH;//图片宽高
    int rows;//图片被拆分的行列数

    /**
     * 生成一个w*h 大小的图片
     * 并提供rows*rows个栏位
     *
     * @param w
     * @param h
     * @param rows
     */
    public DynTextureGenerator(float w, float h, int rows) {
        texW = w;
        texH = h;
        this.rows = rows;

        int cnt = rows * rows;
        grids = new List[cnt];
        for (int i = 0; i < cnt; i++) {
            grids[i] = new ArrayList<>();
        }
        gridTimeOut = new long[cnt];
    }

    public void gl_init() {
        dynamicContentFbo = new GLFrameBuffer((int) texW, (int) texH);
        dynamicContentFbo.gl_init();

        dynamicContentCamera = new Camera(texW, texH, Camera.FOV, Camera.NEAR_PLANE, Camera.FAR_PLANE);
        dynamicContentCamera.setDistanceFromTarget(3);//摄像机离人远
        dynamicContentCamera.setHeightOfLand(1);//抬高像机
        dynamicContentCamera.setPitch(1);
        Entity entity = new Entity(null, new Vector3f(0f, 0f, 0f), 0f, 0f, 0f, 1f);
        dynamicContentCamera.setLookatTarget(entity);

        dynamicContentRenderer = new GuiRenderer();
        dynamicContentRenderList = new ArrayList<>();
    }


    public void renderToFbo() {
        dynamicContentFbo.begin();
        dynamicContentCamera.update();
        glDisable(GL_DEPTH_TEST);
        glClearColor(0f, 0f, 0f, 0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        //
        dynamicContentRenderList.clear();
        for (int i = 0, imax = grids.length; i < imax; i++) {
            if (!grids[i].isEmpty()) {
                dynamicContentRenderList.addAll(grids[i]);
            }
        }
        dynamicContentRenderer.render(dynamicContentRenderList);
        dynamicContentFbo.end();
//        GLUtil.checkGlError(this.getClass().getName() + " gl_paint renderDynamicContent");
    }

    public GImage getTextureImage() {
        return dynamicContentFbo.getFboimg();
    }

    /**
     * 找一个空栏位
     *
     * @return
     */
    public int findSlot() {
        long now = System.currentTimeMillis();
        for (int i = 0, imax = grids.length; i < imax; i++) {
            if (now > gridTimeOut[i]) {
                grids[i].clear();
                return i;
            }
        }
        return -1;
    }

    public List<GuiTexture> getSlot(int index) {
        if (index >= 0 && index < grids.length) {
            return grids[index];
        }
        return null;
    }

    /**
     * 设置栏位超期时间
     *
     * @param slot
     * @param timeOut
     */
    public void setSlotTimeOut(int slot, long timeOut) {
        gridTimeOut[slot] = timeOut;
    }
}
