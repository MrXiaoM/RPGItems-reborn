package think.rpgitems.utils;

import com.google.common.io.ByteArrayDataOutput;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;

public class IOUtils {

    public static ByteArrayDataOutput newDataOutput() {
        return new ByteArrayDataOutputStream(new ByteArrayOutputStream());
    }

    public static class ByteArrayDataOutputStream implements ByteArrayDataOutput {
        final DataOutput output;
        final ByteArrayOutputStream byteArrayOutputStream;

        ByteArrayDataOutputStream(ByteArrayOutputStream byteArrayOutputStream) {
            this.byteArrayOutputStream = byteArrayOutputStream;
            this.output = new DataOutputStream(byteArrayOutputStream);
        }

        public void write(int b) {
            try {
                this.output.write(b);
            } catch (IOException var3) {
                throw new AssertionError(var3);
            }
        }

        public void write(byte @NotNull [] b) {
            try {
                this.output.write(b);
            } catch (IOException var3) {
                throw new AssertionError(var3);
            }
        }

        public void write(byte @NotNull [] b, int off, int len) {
            try {
                this.output.write(b, off, len);
            } catch (IOException var5) {
                throw new AssertionError(var5);
            }
        }

        public void writeBoolean(boolean v) {
            try {
                this.output.writeBoolean(v);
            } catch (IOException var3) {
                throw new AssertionError(var3);
            }
        }

        public void writeByte(int v) {
            try {
                this.output.writeByte(v);
            } catch (IOException var3) {
                throw new AssertionError(var3);
            }
        }

        public void writeBytes(@NotNull String s) {
            try {
                this.output.writeBytes(s);
            } catch (IOException var3) {
                throw new AssertionError(var3);
            }
        }

        public void writeChar(int v) {
            try {
                this.output.writeChar(v);
            } catch (IOException var3) {
                throw new AssertionError(var3);
            }
        }

        public void writeChars(@NotNull String s) {
            try {
                this.output.writeChars(s);
            } catch (IOException var3) {
                throw new AssertionError(var3);
            }
        }

        public void writeDouble(double v) {
            try {
                this.output.writeDouble(v);
            } catch (IOException var4) {
                throw new AssertionError(var4);
            }
        }

        public void writeFloat(float v) {
            try {
                this.output.writeFloat(v);
            } catch (IOException var3) {
                throw new AssertionError(var3);
            }
        }

        public void writeInt(int v) {
            try {
                this.output.writeInt(v);
            } catch (IOException var3) {
                throw new AssertionError(var3);
            }
        }

        public void writeLong(long v) {
            try {
                this.output.writeLong(v);
            } catch (IOException var4) {
                throw new AssertionError(var4);
            }
        }

        public void writeShort(int v) {
            try {
                this.output.writeShort(v);
            } catch (IOException var3) {
                throw new AssertionError(var3);
            }
        }

        public void writeUTF(@NotNull String s) {
            try {
                this.output.writeUTF(s);
            } catch (IOException var3) {
                throw new AssertionError(var3);
            }
        }

        public byte @NotNull [] toByteArray() {
            return this.byteArrayOutputStream.toByteArray();
        }
    }
}
