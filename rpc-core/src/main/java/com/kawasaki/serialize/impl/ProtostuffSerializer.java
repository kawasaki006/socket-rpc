package com.kawasaki.serialize.impl;

import com.kawasaki.serialize.Serializer;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

public class ProtostuffSerializer implements Serializer {
    private static final LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);

    @Override
    public byte[] serialize(Object obj) {
        Class<?> clazz = obj.getClass();

        Schema schema = RuntimeSchema.getSchema(clazz);

        try {
            return ProtobufIOUtil.toByteArray(obj, schema, buffer);
        } finally {
            buffer.clear();
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        Schema<T> schema = RuntimeSchema.getSchema(clazz);

        T t = schema.newMessage();
        ProtobufIOUtil.mergeFrom(bytes, t, schema);

        return t;
    }
}
