package com.swingfrog.summer2.dao.starter;

import com.swingfrog.summer2.core.configuration.annotation.Configuration;
import com.swingfrog.summer2.core.configuration.annotation.Value;

/**
 * @author: toke
 */
@Configuration
public class SummerDaoConfiguration {

    @Value("summer.dao.starter.priority")
    private Integer daoStarterPriority;

    @Value("summer.dao.async.corePoolSize")
    private int daoAsyncCorePoolSize;

    public Integer getDaoStarterPriority() {
        return daoStarterPriority;
    }

    public int getDaoAsyncCorePoolSize() {
        return daoAsyncCorePoolSize;
    }

}
