package frc.lib.miniNT4;

public enum NT4Types {
    BOOL(0, NT4TypeStr.BOOL),   
    FLOAT_64(1, NT4TypeStr.FLOAT_64),
    INT(2, NT4TypeStr.INT),
    FLOAT_32(3, NT4TypeStr.FLOAT_32),
    STR(4, NT4TypeStr.STR),
    JSON(4, NT4TypeStr.JSON),
    RAW(5, NT4TypeStr.RAW),
    RPC(5, NT4TypeStr.RPC),
    MSGPACK(5, NT4TypeStr.MSGPACK),
    PROTOBUF(5, NT4TypeStr.PROTOBUF),
    ARRAY_BOOL(16, NT4TypeStr.ARRAY_BOOL),
    ARRAY_FLOAT_64(17, NT4TypeStr.ARRAY_FLOAT_64),
    ARRAY_INT(18, NT4TypeStr.ARRAY_INT),
    ARRAY_FLOAT_32(19, NT4TypeStr.ARRAY_FLOAT_32),
    ARRAY_STR(20, NT4TypeStr.ARRAY_STR);

    public final int type_idx;
    public final String dtstr;
    
    private NT4Types(final int type_idx, final String dtstr) {
        this.type_idx = type_idx;
        this.dtstr = dtstr;
    }
}

