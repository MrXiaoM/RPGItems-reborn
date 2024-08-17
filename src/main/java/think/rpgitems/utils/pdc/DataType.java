package think.rpgitems.utils.pdc;

import org.jetbrains.annotations.NotNull;

/**
 * PersistentDataType from Bukkit
 */
public interface DataType<T, Z> {
    DataType<Byte, Byte> BYTE = new PrimitiveDataType<>(Byte.class);
    DataType<Short, Short> SHORT = new PrimitiveDataType<>(Short.class);
    DataType<Integer, Integer> INTEGER = new PrimitiveDataType<>(Integer.class);
    DataType<Long, Long> LONG = new PrimitiveDataType<>(Long.class);
    DataType<Float, Float> FLOAT = new PrimitiveDataType<>(Float.class);
    DataType<Double, Double> DOUBLE = new PrimitiveDataType<>(Double.class);
    DataType<Byte, Boolean> BOOLEAN = new DataType.BooleanDataType();
    DataType<String, String> STRING = new PrimitiveDataType<>(String.class);
    DataType<byte[], byte[]> BYTE_ARRAY = new PrimitiveDataType<>(byte[].class);
    DataType<int[], int[]> INTEGER_ARRAY = new PrimitiveDataType<>(int[].class);
    DataType<long[], long[]> LONG_ARRAY = new PrimitiveDataType<>(long[].class);
    DataType<DataContainer[], DataContainer[]> TAG_CONTAINER_ARRAY = new PrimitiveDataType<>(DataContainer[].class);
    DataType<DataContainer, DataContainer> TAG_CONTAINER = new PrimitiveDataType<>(DataContainer.class);

    @NotNull
    Class<T> getPrimitiveType();
    @NotNull
    Class<Z> getComplexType();

    T toPrimitive(Z complex, @NotNull DataAdapterContext context);

    Z fromPrimitive(T primitive, @NotNull DataAdapterContext context);

    class PrimitiveDataType<T> implements DataType<T, T> {

        private final Class<T> primitiveType;

        PrimitiveDataType(@NotNull Class<T> primitiveType) {
            this.primitiveType = primitiveType;
        }

        @NotNull
        @Override
        public Class<T> getPrimitiveType() {
            return primitiveType;
        }

        @NotNull
        @Override
        public Class<T> getComplexType() {
            return primitiveType;
        }

        @NotNull
        @Override
        public T toPrimitive(@NotNull T complex, @NotNull DataAdapterContext context) {
            return complex;
        }

        @NotNull
        @Override
        public T fromPrimitive(@NotNull T primitive, @NotNull DataAdapterContext context) {
            return primitive;
        }
    }

    class BooleanDataType implements DataType<Byte, Boolean> {

        @NotNull
        @Override
        public Class<Byte> getPrimitiveType() {
            return Byte.class;
        }

        @NotNull
        @Override
        public Class<Boolean> getComplexType() {
            return Boolean.class;
        }

        @NotNull
        @Override
        public Byte toPrimitive(@NotNull Boolean complex, @NotNull DataAdapterContext context) {
            return (byte) (complex ? 1 : 0);
        }

        @NotNull
        @Override
        public Boolean fromPrimitive(@NotNull Byte primitive, @NotNull DataAdapterContext context) {
            return primitive != 0;
        }
    }
}
