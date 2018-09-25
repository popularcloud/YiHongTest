package cn.dlc.yihongtest.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;
import java.util.Map;

/**
 * @author YoungeTao
 * @Email youngetao@gmail.com
 * @date 2018/5/17 10:49
 * @describe
 */
public class GsonUtil {

    private static Gson mGson = null;

    static {
        if(mGson == null){
            mGson = new Gson();
        }
    }

    /**
     * object 转成json
     * @param object
     * @return
     */
    public static String GsonString(Object object) {
        String gsonString = null;
        if (mGson != null) {
            gsonString = mGson.toJson(object);
        }
        return gsonString;
    }

    /**
     * 转成bean
     * @param gsonString
     * @param cls
     * @param <T>
     * @return
     */
    public static <T> T GsonToBean(String gsonString, Class<T> cls) {
        T t = null;
        if (mGson != null) {
            t = mGson.fromJson(gsonString, cls);
        }
        return t;
    }

    /**
     * 转成list
     * @param gsonString
     * @param cls
     * @param <T>
     * @return
     */
    public static <T> List<T> GsonToList(String gsonString, Class<T> cls) {
        List<T> list = null;
        if (mGson != null) {
            list = mGson.fromJson(gsonString, new TypeToken<List<T>>() {
            }.getType());
        }
        return list;
    }

    /**
     * 转成list中含有map的
     * @param gsonString
     * @param <T>
     * @return
     */
    public static <T> List<Map<String, T>> GsonToListMaps(String gsonString) {
        List<Map<String, T>> list = null;
        if (mGson != null) {
            list = mGson.fromJson(gsonString,
                    new TypeToken<List<Map<String, T>>>() {
                    }.getType());
        }
        return list;
    }

    /**
     * 转成map
     * @param gsonString
     * @param <T>
     * @return
     */
    public static <T> Map<String, T> GsonToMaps(String gsonString) {
        Map<String, T> map = null;
        if (mGson != null) {
            map = mGson.fromJson(gsonString, new TypeToken<Map<String, T>>() {
            }.getType());
        }
        return map;
    }
}
