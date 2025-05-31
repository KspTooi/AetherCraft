package com.ksptool.ql.commons.dialect;

import org.hibernate.dialect.H2Dialect;
import org.springframework.context.annotation.Configuration;

import java.sql.Types;

/**
 * H2数据库自定义方言
 * 将所有Date类型映射到TIMESTAMP WITH TIME ZONE
 */
@Configuration
public class H2DatabaseDialect extends H2Dialect {

    @Override
    protected String columnType(int sqlTypeCode) {
        // 将DATE和TIMESTAMP类型映射到TIMESTAMP WITH TIME ZONE
        if (sqlTypeCode == Types.DATE || sqlTypeCode == Types.TIMESTAMP) {
            return "TIMESTAMP WITH TIME ZONE";
        }
        return super.columnType(sqlTypeCode);
    }

}
