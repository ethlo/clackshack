package com.ethlo.clackshack.model;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DataTypes
{
    public static final DataType<Short> UINT_8 = new DataType<>("UInt8", Short.class, Short::valueOf);
    public static final DataType<Byte> INT_8 = new DataType<>("Int8", Byte.class, Byte::valueOf);

    public static final DataType<Integer> UINT_16 = new DataType<>("UInt16", Integer.class, Integer::valueOf);
    public static final DataType<Integer> INT_16 = new DataType<>("Int16", Integer.class, Integer::valueOf);

    public static final DataType<Long> UINT_32 = new DataType<>("UInt32", Long.class, Long::valueOf);
    public static final DataType<Integer> INT_32 = new DataType<>("Int32", Integer.class, Integer::valueOf);

    public static final DataType<BigInteger> UINT_64 = new DataType<>("UInt64", BigInteger.class, BigInteger::new);
    public static final DataType<Long> INT_64 = new DataType<>("Int64", Long.class, Long::valueOf);

    public static final DataType<Inet4Address> IP_V4 = new DataType<>("IPv4", Inet4Address.class, value ->
    {
        try
        {
            return (Inet4Address) InetAddress.getByName(value);
        }
        catch (UnknownHostException e)
        {
            throw new IllegalArgumentException(e);
        }
    });

    public static final DataType<Inet6Address> IP_V6 = new DataType<>("IPv6", Inet6Address.class, value ->
    {
        try
        {
            return (Inet6Address) InetAddress.getByName(value);
        }
        catch (UnknownHostException e)
        {
            throw new IllegalArgumentException(e);
        }
    });

    public static final DataType<LocalDateTime> DATE_TIME_64 =
            new DataType<>("DateTime64", LocalDateTime.class, value ->
                    LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))
            );

    public static final DataType<LocalDateTime> DATE_TIME =
            new DataType<>("DateTime", LocalDateTime.class, value ->
                    LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            );

    public static final DataType<LocalDate> DATE = new DataType<>("Date", LocalDate.class, LocalDate::parse);

    public static final DataType<String> STRING = new DataType<>("String", String.class, value -> value);
    public static final DataType<String> FIXED_STRING = new DataType<>("FixedString", String.class, value -> value);

    public static final DataType<java.util.UUID> UUID = new DataType<>("UUID", java.util.UUID.class, java.util.UUID::fromString);

    public static final DataType<Float> FLOAT_32 = new DataType<>("Float32", Float.class, Float::valueOf);
    public static final DataType<Double> FLOAT_64 = new DataType<>("Float64", Double.class, Double::valueOf);

    public static final DataType<BigDecimal> DECIMAL = new DataType<>("Decimal", BigDecimal.class, BigDecimal::new);
    public static final DataType<BigDecimal> DECIMAL_32 = new DataType<>("Decimal32", BigDecimal.class, BigDecimal::new);
    public static final DataType<BigDecimal> DECIMAL_64 = new DataType<>("Decimal64", BigDecimal.class, BigDecimal::new);

    private static final Map<String, DataType<?>> TYPES;

    static
    {
        TYPES = Arrays.stream(DataTypes.class.getDeclaredFields())
                .filter(f -> f.getType().equals(DataType.class))
                .map(f ->
                        {
                            try
                            {
                                return (DataType<?>) f.get(null);
                            }
                            catch (IllegalAccessException e)
                            {
                                throw new IllegalArgumentException(e);
                            }
                        }
                ).collect(Collectors.toMap(DataType::getName, d -> d));
    }

    public static Optional<DataType<?>> match(final String baseType)
    {
        return Optional.ofNullable(TYPES.get(baseType));
    }

    /**
     * If you want to override a parser or add one that is not supported
     *
     * @param dataType the data type to add
     */
    public static void addDataType(final DataType<?> dataType)
    {
        TYPES.put(dataType.getName(), dataType);
    }

    public static class DataType<R>
    {
        private final String name;
        private final Class<R> type;
        private final Function<String, R> parser;

        public DataType(final String name, final Class<R> type, final Function<String, R> parser)
        {
            this.name = name;
            this.type = type;
            this.parser = parser;
        }

        public String getName()
        {
            return name;
        }

        public Function<String, R> getParser()
        {
            return parser;
        }

        public Class<R> getType()
        {
            return type;
        }
    }
}