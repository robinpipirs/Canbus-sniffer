package dao;

import java.util.List;

public interface Filter {
    public CanbusMessage execute(CanbusMessage request);
}