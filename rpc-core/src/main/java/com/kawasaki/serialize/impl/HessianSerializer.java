package com.kawasaki.serialize.impl;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
import com.kawasaki.serialize.Serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class HessianSerializer implements Serializer {
    @Override
    public byte[] serialize(Object obj) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            HessianOutput hessianOutput = new HessianOutput(baos);
            hessianOutput.writeObject(obj);

            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
            HessianInput hessianInput = new HessianInput(bais);
            Object o = hessianInput.readObject();

            return clazz.cast(o);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
