package top.bogey.touch_tool.bean.pin.pin_objects;

public enum PinType {
    NONE,

    // 特殊类型
    EXECUTE,
    ADD,

    // 值类型
    OBJECT,
    ANY,

    LIST,
    MAP,

    STRING,
    NUMBER,
    BOOLEAN,

    VALUE_AREA,

    POINT,
    AREA,

    TOUCH,
    NODE,

    IMAGE,
    COLOR,

    APP,
    APPS,
    ;

    public int getGroup() {
        return switch (this) {
            case NONE, EXECUTE, ADD -> -1;
            case LIST, APPS -> 1;
            case MAP -> 2;
            default -> 0;
        };
    }
}
