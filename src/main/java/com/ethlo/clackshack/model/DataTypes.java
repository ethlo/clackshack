package com.ethlo.clackshack.model;

/*-
 * #%L
 * clackshack
 * %%
 * Copyright (C) 2021 Morten Haraldsen (ethlo)
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.IOException;
import java.io.UncheckedIOException;
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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class DataTypes
{
    public static final SimpleDataType<Short> UINT_8 = new SimpleDataType<>("UInt8", Short.class, Short::valueOf);
    public static final SimpleDataType<Byte> INT_8 = new SimpleDataType<>("Int8", Byte.class, Byte::valueOf);

    public static final SimpleDataType<Integer> UINT_16 = new SimpleDataType<>("UInt16", Integer.class, Integer::valueOf);
    public static final SimpleDataType<Integer> INT_16 = new SimpleDataType<>("Int16", Integer.class, Integer::valueOf);

    public static final SimpleDataType<Long> UINT_32 = new SimpleDataType<>("UInt32", Long.class, Long::valueOf);
    public static final SimpleDataType<Integer> INT_32 = new SimpleDataType<>("Int32", Integer.class, Integer::valueOf);

    public static final SimpleDataType<BigInteger> UINT_64 = new SimpleDataType<>("UInt64", BigInteger.class, BigInteger::new);
    public static final SimpleDataType<Long> INT_64 = new SimpleDataType<>("Int64", Long.class, Long::valueOf);

    public static final SimpleDataType<Inet4Address> IP_V4 = new SimpleDataType<>("IPv4", Inet4Address.class, value ->
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

    public static final SimpleDataType<Inet6Address> IP_V6 = new SimpleDataType<>("IPv6", Inet6Address.class, value ->
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

    public static final SimpleDataType<LocalDateTime> DATE_TIME_64 =
            new SimpleDataType<>("DateTime64", LocalDateTime.class, value ->
                    LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"))
            );

    public static final SimpleDataType<String> ENUM = new SimpleDataType<>("Enum", String.class, value -> value);
    public static final SimpleDataType<String> ENUM_8 = new SimpleDataType<>("Enum8", String.class, value -> value);
    public static final SimpleDataType<String> ENUM_16 = new SimpleDataType<>("Enum16", String.class, value -> value);

    public static final SimpleDataType<LocalDateTime> DATE_TIME =
            new SimpleDataType<>("DateTime", LocalDateTime.class, value ->
                    LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            );

    public static final SimpleDataType<LocalDate> DATE = new SimpleDataType<>("Date", LocalDate.class, LocalDate::parse);

    public static final SimpleDataType<String> STRING = new SimpleDataType<>("String", String.class, value -> value);
    public static final SimpleDataType<String> FIXED_STRING = new SimpleDataType<>("FixedString", String.class, value -> value);

    public static final SimpleDataType<java.util.UUID> UUID = new SimpleDataType<>("UUID", java.util.UUID.class, java.util.UUID::fromString);

    public static final SimpleDataType<Float> FLOAT_32 = new SimpleDataType<>("Float32", Float.class, Float::valueOf);
    public static final SimpleDataType<Double> FLOAT_64 = new SimpleDataType<>("Float64", Double.class, Double::valueOf);

    public static final SimpleDataType<BigDecimal> DECIMAL = new SimpleDataType<>("Decimal", BigDecimal.class, BigDecimal::new);
    public static final SimpleDataType<BigDecimal> DECIMAL_32 = new SimpleDataType<>("Decimal32", BigDecimal.class, BigDecimal::new);
    public static final SimpleDataType<BigDecimal> DECIMAL_64 = new SimpleDataType<>("Decimal64", BigDecimal.class, BigDecimal::new);
    public static final DataType<ArrayNode> ARRAY = new DataType<>("Array", ArrayNode.class, n -> Optional.ofNullable(n)
            .filter(node -> n.isArray())
            .map(ArrayNode.class::cast)
            .orElseThrow(() -> new UncheckedIOException(new IOException("Unable to handle 'Array' type that is not an array node"))));

    public static final DataType<ObjectNode> MAP = new DataType<>("Map", ObjectNode.class, n -> Optional.ofNullable(n)
            .filter(node -> n.isObject())
            .map(ObjectNode.class::cast)
            .orElseThrow(() -> new UncheckedIOException(new IOException("Unable to handle 'Map' type that is not an object node"))));

    private static final Map<String, DataType<?>> TYPES;

    static
    {
        TYPES = Arrays.stream(DataTypes.class.getDeclaredFields())
                .filter(f -> DataType.class.isAssignableFrom(f.getType()))
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
    public static void addDataType(final SimpleDataType<?> dataType)
    {
        TYPES.put(dataType.getName(), dataType);
    }

    public static class DataType<R>
    {
        protected final String name;
        protected final Class<R> type;
        protected final Function<JsonNode, R> parser;

        public DataType(String name, Class<R> type, Function<JsonNode, R> parser)
        {
            this.parser = parser;
            this.name = name;
            this.type = type;
        }

        public String getName()
        {
            return name;
        }

        public Function<JsonNode, R> getParser()
        {
            return parser;
        }

        public Class<R> getType()
        {
            return type;
        }
    }

    public static class SimpleDataType<R> extends DataType<R>
    {
        public SimpleDataType(final String name, final Class<R> type, final Function<String, R> parser)
        {
            super(name, type, n -> parser.apply(n.asText()));
        }
    }
}
