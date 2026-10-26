package com.zzyl.nursing.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.zzyl.nursing.domain.NursingLevel;
import com.zzyl.nursing.mapper.NursingLevelMapper;
import com.zzyl.nursing.service.impl.NursingLevelServiceImpl;
import com.zzyl.nursing.vo.NursingLevelVo;
import java.util.Collections;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static com.zzyl.common.constant.CacheConstants.NURSING_LEVEL_ALL_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NursingLevelServiceImplTest {
    @Mock NursingLevelMapper levelMapper;
    @Mock RedisTemplate<Object, Object> redisTemplate;
    @Mock ValueOperations<Object, Object> valueOperations;
    @InjectMocks NursingLevelServiceImpl service;

    @BeforeAll
    static void initializeMybatisPlusMetadata() {
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), "NursingLevelServiceImplTest"),
                NursingLevel.class
        );
    }

    @Test
    void listReturnsPlanNameFromJoinedRows() {
        NursingLevel filter = new NursingLevel();
        filter.setStatus(1);
        NursingLevelVo row = new NursingLevelVo();
        row.setPlanName("基础护理计划");
        when(levelMapper.selectNursingLevelVoList(filter)).thenReturn(Collections.singletonList(row));

        assertEquals("基础护理计划", service.selectNursingLevelVoList(filter).get(0).getPlanName());
    }

    @Test
    void listAllReturnsCachedLevelsWithoutQueryingDatabase() {
        NursingLevel enabled = new NursingLevel();
        enabled.setStatus(1);
        var cachedLevels = Collections.singletonList(enabled);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(NURSING_LEVEL_ALL_KEY)).thenReturn(cachedLevels);

        assertSame(cachedLevels, service.listAll());
        verifyNoInteractions(levelMapper);
    }

    @Test
    void listAllQueriesEnabledLevelsAndCachesThemOnCacheMiss() {
        NursingLevel enabled = new NursingLevel();
        enabled.setStatus(1);
        var enabledLevels = Collections.singletonList(enabled);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(NURSING_LEVEL_ALL_KEY)).thenReturn(null);
        when(levelMapper.selectList(any())).thenAnswer(invocation -> {
            LambdaQueryWrapper<NursingLevel> wrapper = invocation.getArgument(0);
            assertTrue(wrapper.getSqlSegment().contains("status"));
            assertTrue(wrapper.getParamNameValuePairs().containsValue(1));
            return enabledLevels;
        });

        assertEquals(enabledLevels, service.listAll());
        verify(valueOperations).set(NURSING_LEVEL_ALL_KEY, enabledLevels);
    }

    @Test
    void updateInvalidatesCachedLevels() {
        NursingLevel level = new NursingLevel();
        level.setId(7L);
        level.setStatus(0);
        when(levelMapper.updateById(level)).thenReturn(1);

        assertEquals(1, service.updateNursingLevel(level));
        verify(redisTemplate).delete(NURSING_LEVEL_ALL_KEY);
    }
}
