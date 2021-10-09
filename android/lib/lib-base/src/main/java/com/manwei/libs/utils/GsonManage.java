package com.manwei.libs.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * @author : wuyq
 * Time : 2020/12/5 20:34
 * Description :
 */
public class GsonManage {

    private static final Gson mGson = new GsonBuilder().create();


    public static String toJson(Object object) throws JsonSyntaxException {
        return mGson.toJson(object);
    }


    /**
     * Json 转对象 T
     */
    public static <T> T fromJson(String jsonElement, Type clazz) throws Exception {
        return mGson.fromJson(jsonElement, clazz);
    }

    public static <T> T fromJson(JsonElement jsonElement, Class<T> tClass) throws Exception {
        return mGson.fromJson(jsonElement, tClass);
    }

    public static <T> T fromJson(String jsonElement, Class<T> tClass) throws Exception {
        return mGson.fromJson(jsonElement, tClass);
    }

    /**
     * Json  转 List<T>
     */
    public static <T> List<T> fromJsonList(String  jsonElement, Class<T> tClass) throws Exception {
        Type objectType = new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return new Type[]{tClass};
            }

            @Override
            public Type getRawType() {
                return List.class;
            }

            @Override
            public Type getOwnerType() {
                return null;
            }
        };
        return mGson.fromJson(jsonElement, objectType);
    }

    public static <T> List<T> fromJsonList(JsonElement jsonElement, Class<T> tClass) throws Exception {
        Type objectType = new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return new Type[]{tClass};
            }

            @Override
            public Type getRawType() {
                return List.class;
            }

            @Override
            public Type getOwnerType() {
                return null;
            }
        };
        return mGson.fromJson(jsonElement, objectType);
    }


    /**
     * List<T> 转 List<Object>
     */
    public static <T> List<T> fromJsonList(List<T> listT, Class<T> tClass) throws Exception {
        Type objectType = new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return new Type[]{tClass};
            }

            @Override
            public Type getRawType() {
                return List.class;
            }

            @Override
            public Type getOwnerType() {
                return null;
            }
        };
        return mGson.fromJson(mGson.toJson(listT), objectType);
    }

    /**
     * json 转对象 K，K 对象中有 List<T>
     *
     * @param kClass      父对象
     * @param tClass      子对象
     * @param jsonElement json 串
     */
    public static <K, T> K getJsonResult(final Class<K> kClass, final Class<T> tClass, JsonElement jsonElement) throws Exception {
        Type objectType = new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return new Type[]{tClass};
            }

            @Override
            public Type getRawType() {
                return kClass;
            }

            @Override
            public Type getOwnerType() {
                return null;
            }
        };
        return mGson.fromJson(jsonElement, objectType);
    }
} 