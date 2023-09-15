/*
 * Copyright (c) 2020. Morgan Lewis <https://github.com/MESLewis>
 * This file is part of the SimpleGLTF2 project which is released under the MIT License.
 * See <https://github.com/MESLewis/SimpleGLTF2/blob/master/LICENSE> for more information
 */

package org.mini.g3d.animation.gltf2.loader;


import org.mini.g3d.core.vector.Matrix4f;
import org.mini.g3d.core.vector.Quaternionf;
import org.mini.g3d.core.vector.Vector3f;
import org.mini.g3d.core.vector.Vector4f;
import org.mini.json.JsonParser;

import java.util.List;

public class GLTFJsonModule extends JsonParser.SimpleModule {

    private static final JsonParser.SimpleModule instance;

    static {
        instance = new GLTFJsonModule();
        instance.addDeserializer(Matrix4f.class, new JsonParser.StdDeserializer<Matrix4f>(null) {
            @Override
            public Matrix4f deserialize(JsonParser.JsonCell p, String types) {
                if (p.getType() != JsonParser.JsonCell.TYPE_LIST) {
                    throw new RuntimeException("parse error:" + this);
                }
                List<JsonParser.JsonNumber> list = (List) p;
                float[] arr = new float[]{
                        list.get(0).asFloat()
                        , list.get(1).asFloat()
                        , list.get(2).asFloat()
                        , list.get(3).asFloat()
                        , list.get(4).asFloat()
                        , list.get(5).asFloat()
                        , list.get(6).asFloat()
                        , list.get(7).asFloat()
                        , list.get(8).asFloat()
                        , list.get(9).asFloat()
                        , list.get(10).asFloat()
                        , list.get(11).asFloat()
                        , list.get(12).asFloat()
                        , list.get(13).asFloat()
                        , list.get(14).asFloat()
                        , list.get(15).asFloat()
                };
                return (Matrix4f) new Matrix4f().load(arr);
            }
        });
        instance.addDeserializer(Quaternionf.class, new JsonParser.StdDeserializer<Quaternionf>(null) {

            @Override
            public Quaternionf deserialize(JsonParser.JsonCell p, String types) {
                if (p.getType() != JsonParser.JsonCell.TYPE_LIST) {
                    throw new RuntimeException("parse error:" + this);
                }
                List<JsonParser.JsonNumber> list = (List) p;
                return new Quaternionf(
                        list.get(0).asFloat()
                        , list.get(1).asFloat()
                        , list.get(2).asFloat()
                        , list.get(3).asFloat()
                );
            }
        });
        instance
                .addDeserializer(Vector3f.class, new JsonParser.StdDeserializer<Vector3f>(null) {

                    @Override
                    public Vector3f deserialize(JsonParser.JsonCell p, String types) {
                        if (p.getType() != JsonParser.JsonCell.TYPE_LIST) {
                            throw new RuntimeException("parse error:" + this);
                        }
                        List<JsonParser.JsonNumber> list = (List) p;
                        return new Vector3f(
                                list.get(0).asFloat()
                                , list.get(1).asFloat()
                                , list.get(2).asFloat());
                    }
                });
        instance.addDeserializer(Vector4f.class, new JsonParser.StdDeserializer<Vector4f>(null) {
            @Override
            public Vector4f deserialize(JsonParser.JsonCell p, String types) {
                if (p.getType() != JsonParser.JsonCell.TYPE_LIST) {
                    throw new RuntimeException("parse error:" + this);
                }
                List<JsonParser.JsonNumber> list = (List) p;
                return new Vector4f(
                        list.get(0).asFloat()
                        , list.get(1).asFloat()
                        , list.get(2).asFloat()
                        , list.get(3).asFloat());
            }
        })
        ;

    }

    public static JsonParser.SimpleModule getModule() {
        return instance;
    }

}
