/*
 * Copyright (c) 2020. Morgan Lewis <https://github.com/MESLewis>
 * This file is part of the SimpleGLTF2 project which is released under the MIT License.
 * See <https://github.com/MESLewis/SimpleGLTF2/blob/master/LICENSE> for more information
 */

package org.mini.g3d.core.gltf2.loader.data;

import java.io.DataInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;

/**
 * The root object fr a glTF asset
 */
public class GLTF extends GLTFProperty {


    public GLTF() {

    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    String version;
    /**
     * Holds references of a field to set, an index to get, and a field to get from
     */
    List<Runnable> indexResolvers = new ArrayList<>();

    public void setExtensionsUsed(LinkedHashSet<String> extensionsUsed) {
        this.extensionsUsed = extensionsUsed;
    }

    public void setExtensionsRequired(LinkedHashSet<String> extensionsRequired) {
        this.extensionsRequired = extensionsRequired;
    }

    public void setAccessors(List<GLTFAccessor> accessors) {
        this.accessors = accessors;
    }

    public void setAnimations(List<GLTFAnimation> animations) {
        this.animations = animations;
    }

    public void setAsset(GLTFAsset asset) {
        this.asset = asset;
    }

    public void setBuffers(List<GLTFBuffer> buffers) {
        this.buffers = buffers;
    }

    public void setBufferViews(List<GLTFBufferView> bufferViews) {
        this.bufferViews = bufferViews;
    }

    public void setCameras(List<GLTFCamera> cameras) {
        this.cameras = cameras;
    }

    public void setImages(List<GLTFImage> images) {
        this.images = images;
    }

    public void setMaterials(List<GLTFMaterial> materials) {
        this.materials = materials;
    }

    public void setMeshes(List<GLTFMesh> meshes) {
        this.meshes = meshes;
    }

    public void setNodes(List<GLTFNode> nodes) {
        this.nodes = nodes;
    }

    public void setSamplers(List<GLTFSampler> samplers) {
        this.samplers = samplers;
    }

    public void setDefaultScene(GLTFScene defaultScene) {
        this.defaultScene = defaultScene;
    }

    public void setScenes(List<GLTFScene> scenes) {
        this.scenes = scenes;
    }

    public void setSkins(List<GLTFSkin> skins) {
        this.skins = skins;
    }

    public void setTextures(List<GLTFTexture> textures) {
        this.textures = textures;
    }

    public void setSource(String source, ResourceFrom from) {
        this.source = source.replace('\\', '/');
        fileFrom = from;
    }

    public AnimationClip getAnimationClip() {
        return animationClip;
    }

    public void setAnimationClip(AnimationClip animationClip) {
        this.animationClip = animationClip;
    }

    private AnimationClip animationClip;

    /**
     * Names of glTF extensions used somewhere in this asset.
     */
    private LinkedHashSet<String> extensionsUsed;
    /**
     * Names of glTF extensions required to properly load this asset.
     */
    private LinkedHashSet<String> extensionsRequired;
    /**
     * An array of accessors. An accessor is a typed view into a bufferView
     */
    private List<GLTFAccessor> accessors;
    /**
     * An array of keyframe animations.
     */
    private List<GLTFAnimation> animations;
    /**
     * Metadata about the glTF asset.
     */
    private GLTFAsset asset;
    /**
     * An array of buffers. A buffer points to binary geometry, animation, or kins.
     */
    private List<GLTFBuffer> buffers;
    /**
     * An array of bufferViews.  A bufferView is a view into a buffer generally representing a subset
     * of the buffer.
     */
    private List<GLTFBufferView> bufferViews;
    /**
     * An array of cameras.  A camera defines a projection matrix.
     */
    private List<GLTFCamera> cameras;
    /**
     * An array of images.  An image defines data used to create a texture.
     */
    private List<GLTFImage> images;
    /**
     * An array of materials. A material defines the appearance of a primitive.
     */
    private List<GLTFMaterial> materials;
    /**
     * An array of meshes. A mes is a set of primitives to be rendered.
     */
    private List<GLTFMesh> meshes;
    /**
     * An array of nodes.
     */
    private List<GLTFNode> nodes;
    /**
     * An array of samplers.  A sampler contains properties for texture filtering and wrapping modes.
     */
    private List<GLTFSampler> samplers;
    /**
     * The index of the default scene.
     */
    private GLTFScene defaultScene;

    /**
     * An array of scenes.
     */
    private List<GLTFScene> scenes;
    /**
     * An array of skins. A skin is defined by joints and matrices.
     */
    private List<GLTFSkin> skins;
    /**
     * An array of textures. minItems 1
     */
    private List<GLTFTexture> textures;
    /**
     * The URI of the root file for this GLTF asset Used to resolve non-absolute file paths
     */
    private String source;

    public ByteBuffer getBin() {
        return bin;
    }

    public void setBin(ByteBuffer bin) {
        this.bin = bin;
    }

    /**
     * The implementation of how to convert from a URI to a Stream Used to load all files
     */
    private ByteBuffer bin;

    public enum ResourceFrom {
        JAR,
        FILE;
    }

    public ResourceFrom getFileFrom() {
        return fileFrom;
    }

    public void setFileFrom(ResourceFrom fileFrom) {
        this.fileFrom = fileFrom;
    }

    private ResourceFrom fileFrom = ResourceFrom.JAR;

    /**
     * Resolve relativePath against the base URI for this file
     *
     * @return
     */
    public String getRelativePath() {
        String s = source;
        if (s.indexOf('/') >= 0) {
            return s.substring(0, s.lastIndexOf('/')) + "/";
        }
        return "";
    }

    public String getSource() {
        return source;
    }

    /**
     * @return the default Scene, or null if undefined
     */
    public GLTFScene getDefaultScene() {
        return defaultScene;
    }

    public void setScene(int index) {
        gltf.indexResolvers.add(() -> defaultScene = gltf.getScene(index));
    }

    public List<GLTFScene> getScenes() {
        return Collections.unmodifiableList(scenes);
    }

    GLTFBufferView getBufferView(int indexBufferView) {
        return this.bufferViews.get(indexBufferView);
    }

    GLTFBuffer getBuffer(int indexBuffer) {
        return this.buffers.get(indexBuffer);
    }

    GLTFMaterial getMaterial(int indexMaterial) {
        return this.materials.get(indexMaterial);
    }

    GLTFAccessor getAccessor(int indexAccessor) {
        return this.accessors.get(indexAccessor);
    }

    GLTFNode getNode(int indexNode) {
        return this.nodes.get(indexNode);
    }

    GLTFCamera getCamera(int indexCamera) {
        return this.cameras.get(indexCamera);
    }

    GLTFSkin getSkin(int indexSkin) {
        return this.skins.get(indexSkin);
    }

    GLTFMesh getMesh(int indexMesh) {
        return this.meshes.get(indexMesh);
    }

    GLTFTexture getTexture(int indexTexture) {
        return this.textures.get(indexTexture);
    }

    GLTFImage getImage(int indexImage) {
        return this.images.get(indexImage);
    }

    GLTFSampler getSampler(int indexSampler) {
        return samplers.get(indexSampler);
    }

    private GLTFScene getScene(int index) {
        return scenes.get(index);
    }

    public LinkedHashSet<String> getExtensionsUsed() {
        return extensionsUsed;
    }

    public Set<String> getExtensionsRequired() {
        return extensionsRequired;
    }

    public List<GLTFAnimation> getAnimations() {
        return animations;
    }

    public void applyLookupMap() {
        indexResolvers.forEach(Runnable::run);
    }


    /**
     * Route bufferIO function through this to ensure little endian.
     *
     * @return
     */
    static public ByteBuffer getDirectByteBuffer(String path) {//gust
        try {
//            FileInputStream is = new FileInputStream(path);
            if (path.charAt(0) != '/') path = "/" + path;
            InputStream is = GLTF.class.getResourceAsStream(path);
            DataInputStream dis = new DataInputStream(is);
            byte[] fb = new byte[dis.available()];
            dis.readFully(fb);
            dis.close();
            //ByteBuffer bb=ByteBuffer.wrap(fb);
            ByteBuffer bb = ByteBuffer.allocateDirect(fb.length).put(fb);
            bb.order(ByteOrder.LITTLE_ENDIAN);
            return (ByteBuffer) bb.rewind();
        } catch (Exception e) { //Thrown by .map on a JarFileSystem entry
            e.printStackTrace();
        }
        return null;
    }
}
