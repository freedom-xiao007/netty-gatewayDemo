package com.gateway.filter;

/**
 * @author lw
 */
public class Filter {
    static final FilterSingleton filterSingleton = FilterSingleton.getInstance();

    static public void registerRequestFrontFilter(RequestFrontFilter requestFrontFilter) {
        filterSingleton.registerRequestFrontFilter(requestFrontFilter);
    }

    static public void registerResponseBackendFilter(ResponseBackendFilter responseBackendFilter) {
        filterSingleton.responseBackendFilters.add(responseBackendFilter);
    }
}
