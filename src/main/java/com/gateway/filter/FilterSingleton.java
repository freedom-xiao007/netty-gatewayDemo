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

    List<RequestFrontFilter> requestFrontFilterList = new ArrayList<>();
    List<ResponseBackendFilter> responseBackendFilters = new ArrayList<>();

    public void registerRequestFrontFilter(RequestFrontFilter requestFrontFilter) {
        this.requestFrontFilterList.add(requestFrontFilter);
    }

    public void registerResponseBackendFilter(ResponseBackendFilter responseBackendFilter) {
        this.responseBackendFilters.add(responseBackendFilter);
    }
}
