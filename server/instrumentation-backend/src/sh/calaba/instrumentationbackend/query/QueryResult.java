package sh.calaba.instrumentationbackend.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class QueryResult {
    private final List<?> result;

    public QueryResult(Collection<?> input) {
        result = new ArrayList<Object>(input);
    }

    public List<?> asMappedList() {
        return ViewMapper.mapList(result);
    }

    public List<?> asList() {
        return result;
    }
}
