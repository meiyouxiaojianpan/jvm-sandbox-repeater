package com.alibaba.repeater.console.service.impl;

import com.alibaba.jvm.sandbox.repeater.plugin.core.serialize.SerializeException;
import com.alibaba.jvm.sandbox.repeater.plugin.core.wrapper.RecordWrapper;
import com.alibaba.jvm.sandbox.repeater.plugin.core.wrapper.SerializerWrapper;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.RepeatModel;
import com.alibaba.jvm.sandbox.repeater.plugin.domain.RepeaterResult;
import com.alibaba.repeater.console.dal.mapper.RecordMapper;
import com.alibaba.repeater.console.dal.mapper.RepeatMapper;
import com.alibaba.repeater.console.dal.model.Record;
import com.alibaba.repeater.console.dal.model.Repeat;
import com.alibaba.repeater.console.service.RecordService;
import com.alibaba.repeater.console.service.util.ConvertUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link RecordServiceMysqlImpl} 使用mysql实现存储
 * <p>
 *
 * @author zhaoyb1990
 */
@Service("recordServiceMysql")
public class RecordServiceMysqlImpl extends AbstractRecordService implements RecordService {

    @Resource
    private RecordMapper recordMapper;
    @Resource
    private RepeatMapper repeatMapper;

    /**
     * key: repeatId
     * value: recordId
     */
    private volatile Map<String, Long> repeatCache = new ConcurrentHashMap<>(4096);

    @Override
    public RepeaterResult<String> saveRecord(String body) {
        try {
            RecordWrapper wrapper = SerializerWrapper.hessianDeserialize(body, RecordWrapper.class);
            if (wrapper == null || StringUtils.isEmpty(wrapper.getAppName())) {
                return RepeaterResult.builder().success(false).message("invalid request").build();
            }
            Record record = ConvertUtil.convertWrapper(wrapper, body);
            recordMapper.insert(record);
            return RepeaterResult.builder().success(true).message("operate success").data("-/-").build();
        } catch (Throwable throwable) {
            return RepeaterResult.builder().success(false).message(throwable.getMessage()).build();
        }
    }

    @Override
    public RepeaterResult<String> saveRepeat(String body) {
        try {
            RepeatModel rm = SerializerWrapper.hessianDeserialize(body, RepeatModel.class);
            Long recordId = repeatCache.remove(rm.getRepeatId());
            if (recordId == null) {
                return RepeaterResult.builder().success(false).message("invalid repeatId:" + rm.getRepeatId()).build();
            }
            //保存回放结果
            Repeat repeat = new Repeat();
            repeat.setTraceId(rm.getTraceId());
            repeat.setOriginResponseId(recordId);
            repeat.setFinish(rm.isFinish());
            repeat.setRepeatId(rm.getRepeatId());
            repeat.setCost(rm.getCost());
            repeat.setMockInvocations(SerializerWrapper.hessianSerialize(rm.getMockInvocations()));
            repeat.setResponse(SerializerWrapper.hessianSerialize(rm.getResponse()));
            repeatMapper.insert(repeat);
        } catch (Throwable throwable) {
            return RepeaterResult.builder().success(false).message(throwable.getMessage()).build();
        }
        return RepeaterResult.builder().success(true).message("operate success").data("-/-").build();
    }

    @Override
    public RepeaterResult<String> get(String appName, String traceId) {
        //加索引
        Record record = recordMapper.selectByAppNameAndTraceId(appName, traceId);
        if (record == null) {
            return RepeaterResult.builder().success(false).message("data not exits").build();
        }
        return RepeaterResult.builder().success(true).message("operate success").data(record.getWrapperRecord()).build();
    }

    @Override
    public RepeaterResult<String> repeat(String appName, String traceId, String repeatId) {
        final Record record = recordMapper.selectByAppNameAndTraceId(appName, traceId);
        if (record == null) {
            return RepeaterResult.builder().success(false).message("data does not exist").build();
        }
        RepeaterResult<String> rp = repeat(record, repeatId);
        if (rp.isSuccess()) {
            repeatCache.put(rp.getData(), record.getId());
        }
        return rp;
    }

    @Override
    public RepeaterResult<RepeatModel> callback(String repeatId) {
        if (repeatCache.containsKey(repeatId)) {
            return RepeaterResult.builder().success(true).message("operate is going on").build();
        }
        Repeat repeat = repeatMapper.selectAllColumnByRepeatId(repeatId);
        RepeatModel rm = new RepeatModel();
        rm.setCost(repeat.getCost());
        rm.setFinish(repeat.isFinish());
        rm.setRepeatId(repeat.getRepeatId());
        rm.setTraceId(repeat.getTraceId());
        rm.setOriginResponse(recordMapper.selectById(repeat.getOriginResponseId()));
        try {
            rm.setMockInvocations(SerializerWrapper.hessianDeserialize(repeat.getMockInvocations(), List.class));
            rm.setResponse(SerializerWrapper.hessianDeserialize(repeat.getResponse()));
        } catch (SerializeException e) {
            return RepeaterResult.builder().success(false).message(e.getMessage()).build();
        }
        //进行diff
        return RepeaterResult.builder().success(true).message("operate success").data(rm).build();
    }
}
