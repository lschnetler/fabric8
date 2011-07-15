/**
 * Copyright (C) 2010-2011, FuseSource Corp.  All rights reserved.
 *
 *     http://fusesource.com
 *
 * The software in this package is published under the terms of the
 * CDDL license a copy of which has been included with this distribution
 * in the license.txt file.
 */

package org.fusesource.fabric.apollo.amqp.codec;

import org.fusesource.fabric.apollo.amqp.codec.api.AnnotatedMessage;
import org.fusesource.fabric.apollo.amqp.codec.interfaces.AMQPType;
import org.fusesource.fabric.apollo.amqp.codec.interfaces.Frame;
import org.fusesource.fabric.apollo.amqp.codec.marshaller.FrameSupport;
import org.fusesource.fabric.apollo.amqp.codec.marshaller.MessageSupport;
import org.fusesource.fabric.apollo.amqp.codec.marshaller.TypeReader;
import org.fusesource.fabric.apollo.amqp.codec.types.AMQPFrame;
import org.fusesource.fabric.apollo.amqp.codec.types.AnnotatedMessageImpl;
import org.fusesource.fabric.apollo.amqp.codec.types.Transfer;
import org.fusesource.hawtbuf.Buffer;
import org.fusesource.hawtbuf.DataByteArrayInputStream;
import org.fusesource.hawtbuf.DataByteArrayOutputStream;

import static org.fusesource.fabric.apollo.amqp.codec.marshaller.MessageSupport.decodeAnnotatedMessage;

/**
 *
 */
public class TestSupport {

    private static final int MAX_SIZE = 512;

    public static <T extends Frame> T encodeDecode(T frame) throws Exception {
        DataByteArrayInputStream in = new DataByteArrayInputStream(FrameSupport.createFrame(frame).getBody());
        return (T)FrameSupport.getPerformative(in);
    }

    public static AnnotatedMessage encodeDecode(Transfer performative, AnnotatedMessage payload) throws Exception {
        DataByteArrayInputStream in = new DataByteArrayInputStream(FrameSupport.createFrame(performative, payload).getBody());

        Transfer throwaway = (Transfer)FrameSupport.getPerformative(in);
        return FrameSupport.getPayload(in);
    }

    public static AMQPFrame writeRead(AMQPFrame in, boolean display) throws Exception {
        long size = in.getFrameSize();
        if (size < MAX_SIZE && display) {
            System.out.printf("\n%s", in);
        }
        Buffer buf = write(in);
        if (size < MAX_SIZE && display) {
            System.out.printf("\n%s", string(buf.data));
        }
        AMQPFrame out = read(buf);
        if (size < MAX_SIZE && display) {
            System.out.printf("\n%s\n", out);
        }
        return out;
    }

    public static Buffer write(AMQPFrame in) throws Exception {
        DataByteArrayOutputStream out = new DataByteArrayOutputStream((int)in.getFrameSize());
        in.write(out);
        return out.toBuffer();
    }

    public static AMQPFrame read(Buffer buf) throws Exception {
        DataByteArrayInputStream in = new DataByteArrayInputStream(buf);
        return new AMQPFrame(in);
    }

    public static <T extends AMQPType> byte[] write(T value) throws Exception {
        DataByteArrayOutputStream out = new DataByteArrayOutputStream((int)value.size());
        value.write(out);
        return out.getData();
    }

    public static <T extends AMQPType> T read(byte[] b) throws Exception {
        DataByteArrayInputStream in = new DataByteArrayInputStream(b);
        return (T)TypeReader.read(in);
    }

    public static <T extends AMQPType> T writeRead(T value, boolean display) throws Exception {
        long size = value.size();
        if (size < MAX_SIZE && display) {
            System.out.printf("%s -> ", value);
        }
        byte b[] = write(value);
        if (size < MAX_SIZE && display) {
            System.out.printf("%s -> ", string(b));
        }
        T rc = (T)read(b);
        if (size < MAX_SIZE && display) {
            System.out.printf("%s\n", rc);
        }
        return rc;
    }

    public static <T extends AMQPType> T writeRead(T value) throws Exception {
        return writeRead(value, true);
    }

    public static String string(byte[] b) {
        String rc = "length=" + b.length + " : ";
        for (byte l : b) {
            rc += String.format("[0x%x]", l);
        }
        return rc;
    }

    static AnnotatedMessage encodeDecode(AnnotatedMessage in) throws Exception {
        return encodeDecode(in, true);
    }

    static AnnotatedMessage encodeDecode(AnnotatedMessage in, boolean display) throws Exception {
        long size = ((AnnotatedMessageImpl)in).size();

        if (size < MAX_SIZE && display) {
            System.out.printf("\n%s", in);
        }

        Buffer encoded = MessageSupport.toBuffer(in);
        if (size < MAX_SIZE && display) {
            System.out.printf("\n%s", string(encoded.data));
        }

        AnnotatedMessage out = decodeAnnotatedMessage(encoded);
        if (size < MAX_SIZE && display) {
            System.out.printf("\n%s\n", out);
        }
        return out;
    }
}
