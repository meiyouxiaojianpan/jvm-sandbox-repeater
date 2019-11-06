package com.alibaba.repeater.console.dal.mapper;

import com.alibaba.repeater.console.dal.model.Repeat;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author : gaozhiwen
 * @date : 2019/11/5
 */
@Mapper
public interface RepeatMapper {

    Repeat selectByRepeatId(String repeatId);

    Repeat selectAllColumnByRepeatId(String repeatId);

    void insert(Repeat repeat);

    void update(Repeat repeat);
}
