package com.cc.util;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.shiro.codec.Base64;

/** 
 *  序列化就是将一个对象转换为二进制的数据流。这样就可以进行传输，或者保存到文件中。如果一个类的对象要想实现序列化，就必须实现serializable接口。在此接口中没有任何的方法，此接口只是作为一个标识，表示本类的对象具备了序列化的能力而已。
 *  反序列化:将二进制数据流转换成相应的对象。
 *  如果想要完成对象的序列化，则还要依靠ObjectOutputStream和ObjectInputStream,前者属于序列化操作，而后者属于反序列化操作。
 *
 * @author chencheng0816@gmail.com 
 * @date 2018年4月23日 下午6:15:16
 */
public class SerializeUtils extends SerializationUtils {
	public static String serializeToString(Serializable obj) {  
        try {  
            byte[] value = serialize(obj);  
            return Base64.encodeToString(value);  
        } catch (Exception e) {  
            throw new RuntimeException("serialize session error", e);  
        }  
    }  
  
    public static <T> T deserializeFromString(String base64) {  
        try {  
            byte[] objectData = Base64.decode(base64);  
            return deserialize(objectData);  
        } catch (Exception e) {  
            throw new RuntimeException("deserialize session error", e);  
        }  
    }  
  
    public static <T> Collection<T> deserializeFromStringController(Collection<String> base64s) {  
        try {  
            List<T> list = new LinkedList<T>();  
            for (String base64 : base64s) {  
                byte[] objectData = Base64.decode(base64);  
                T t = deserialize(objectData);  
                list.add(t);  
            }  
            return list;  
        } catch (Exception e) {  
            throw new RuntimeException("deserialize session error", e);  
        }  
    }  
}
