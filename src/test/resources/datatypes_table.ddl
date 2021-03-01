CREATE TABLE data_types
(
    t_uint8 UInt8,
    t_uint16 UInt16,
    t_uint32 UInt32,
    t_uint64 UInt64,
    t_uuid UUID,
    t_int8 Int8,
    t_int16 Int16,
    t_int32 Int32,
    t_int64 Int64,
    t_string String,
    t_fixedstring FixedString(20),
    t_date Date,
    t_ipv4 IPv4,
    t_ipv6 IPv6,
    t_datetime DateTime,
    t_datetime64 DateTime64,
    t_float32 Float32,
    t_float64 Float64,
    t_decimal Decimal(2, 2),
    t_decimal32 Decimal32(2),
    t_decimal64 Decimal64(2)
)
ENGINE = GenerateRandom(1, 5, 3)
