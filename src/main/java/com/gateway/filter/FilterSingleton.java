package com.gateway.filter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lw
 */
class FilterSingleton {
    private enum EnumSingleton {
        /**
         * 懒汉枚举单例
         */
        INSTANCE;
        private FilterSingleton instance;

        EnumSingleton(){
            instance = new FilterSingleton();
        }
        public FilterSingleton getSingleton(){
            return instance;
        }
    }

    static FilterSingleton getInstance(){
        return EnumSingleton.INSTANCE.getSingleton();
    }

    /**
     * Request过滤操作链
     */
    private List<RequestFilter> requestFrontFilterList = new ArrayList<>();
    /**
     * Response过滤操作链
     */
    private List<ResponseFilter> responseBackendFilters = new ArrayList<>();

    void registerRequestFrontFilter(RequestFilter requestFrontFilter) {
        this.requestFrontFilterList.add(requestFrontFilter);
    }

    void registerResponseBackendFilter(ResponseFilter responseBackendFilter) {
        this.responseBackendFilters.add(responseBackendFilter);
    }

    List<RequestFilter> getRequestFrontFilterList() {
        return requestFrontFilterList;
    }

    List<ResponseFilter> getResponseBackendFilters() {
        return responseBackendFilters;
    }
}
