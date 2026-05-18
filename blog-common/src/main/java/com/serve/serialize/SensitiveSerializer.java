package com.serve.serialize;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.serve.anno.Sensitive;
import com.serve.enums.SensitiveType;

import java.io.IOException;

//创建星号序列化器
public class SensitiveSerializer extends JsonSerializer<String> implements ContextualSerializer {

    private SensitiveType sensitiveType;

    @Override
    public void serialize(String str, JsonGenerator jsonGenerator, SerializerProvider provider)
            throws IOException {
        if (str == null) {
            jsonGenerator.writeNull();
            return;
        }
        //根据类型脱敏
        String maskedValue = switch (sensitiveType) {
            case PASSWORD -> "******";
            case MOBILE_PHONE -> mobilePhone(str);
            case EMAIL -> email(str);
            case ALL_MASK -> "*".repeat(Math.min(str.length(), 8));     //将字符全部替换为最长为8的*号字符串
        };
        jsonGenerator.writeString(maskedValue);
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider serializerProvider, BeanProperty beanProperty)
            throws JsonMappingException {
        if (beanProperty == null) {return serializerProvider.findValueSerializer(String.class);}

        // 获取字段上的 @Sensitive 注解
        Sensitive annotation = beanProperty.getAnnotation(Sensitive.class);
        if (annotation == null) {return serializerProvider.findValueSerializer(String.class);}
        // 创建带脱敏类型的新实例
        SensitiveSerializer serializer = new SensitiveSerializer();
        serializer.sensitiveType = annotation.type();
        return serializer;
    }

    private String mobilePhone(String phone) {
        if (phone.length() != 11) {return phone;}
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    private String email(String email) {
        int index = email.indexOf("@");
        if (index <= 1) {return email;}
        return email.charAt(0) + "****" + email.substring(index);
    }
}
