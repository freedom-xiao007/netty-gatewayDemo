package com.gateway.filter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lw
 */
public class FilterSingleton {
    private enum EnumSingleton {
        /**
         * 懒汉枚举单例
         */
        INSTANCE;
        private FilterSingleton instance = null;

        private EnumSingleton(){
            instance = new FilterSingleton();
        }
        public FilterSingleton getSingleton(){
            return instance;
        }
    }

    public static FilterSingleton getInstance(){
        return EnumSingleton.INSTANCE.getSingleton();
    }

    /**
     * Request过滤操作链
     */
    List<RequestFilter> requestFrontFilterList = new ArrayList<>();
    /**
     * Response过滤操作链
     */
    List<ResponseFilter> responseBackendFilters = new ArrayList<>();

    public void registerRequestFrontFilter(RequestFilter requestFrontFilter) {
        this.requestFrontFilterList.add(requestFrontFilter);
    }

    public void registerResponseBackendFilter(ResponseFilter responseBackendFilter) {
        this.responseBackendFilters.add(responseBackendFilter);
    }

    public List<RequestFilter> getRequestFrontFilterList() {
        return requestFrontFilterList;
    }

    public List<ResponseFilter> getResponseBackendFilters() {
        return responseBackendFilters;
    }
}
