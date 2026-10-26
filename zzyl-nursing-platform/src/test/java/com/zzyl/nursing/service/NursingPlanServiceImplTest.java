package com.zzyl.nursing.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.zzyl.nursing.domain.NursingPlan;
import com.zzyl.nursing.domain.NursingProjectPlan;
import com.zzyl.nursing.dto.NursingPlanDto;
import com.zzyl.nursing.mapper.NursingPlanMapper;
import com.zzyl.nursing.mapper.NursingProjectPlanMapper;
import com.zzyl.nursing.service.impl.NursingPlanServiceImpl;
import com.zzyl.nursing.vo.NursingPlanVo;
import com.zzyl.nursing.vo.NursingProjectPlanVo;
import java.util.Collections;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static com.zzyl.common.constant.CacheConstants.NURSING_PLAN_ALL_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NursingPlanServiceImplTest {
    @Mock NursingPlanMapper planMapper;
    @Mock NursingProjectPlanMapper projectPlanMapper;
    @Mock RedisTemplate<Object, Object> redisTemplate;
    @InjectMocks NursingPlanServiceImpl service;

    @BeforeAll
    static void initializeMybatisPlusMetadata() {
        TableInfoHelper.initTableInfo(
                new MapperBuilderAssistant(new MybatisConfiguration(), "NursingPlanServiceImplTest"),
                NursingPlan.class
        );
    }

    @BeforeEach
    void initializeServiceBaseMapper() {
        ReflectionTestUtils.setField(service, "baseMapper", planMapper);
    }

    @Test
    void detailCombinesPlanAndProjectRows() {
        NursingPlan plan = new NursingPlan();
        plan.setId(12L);
        plan.setPlanName("基础护理计划");
        NursingProjectPlanVo project = new NursingProjectPlanVo();
        project.setProjectId("8");
        var projects = Collections.singletonList(project);
        when(planMapper.selectById(12L)).thenReturn(plan);
        when(projectPlanMapper.selectByNursingPlanId(12L)).thenReturn(projects);

        NursingPlanVo result = service.selectNursingPlanById(12L);

        assertEquals(12L, result.getId());
        assertEquals("基础护理计划", result.getPlanName());
        assertSame(projects, result.getProjectPlans());
    }

    @Test
    void insertUsesGeneratedPlanIdForBatchInsert() {
        NursingPlanDto dto = planWithOneProject();
        doAnswer(invocation -> {
            invocation.<NursingPlan>getArgument(0).setId(12L);
            return 1;
        }).when(planMapper).insert(any(NursingPlan.class));
        when(projectPlanMapper.batchInsert(dto.getProjectPlans(), 12L)).thenReturn(1);

        assertEquals(1, service.insertNursingPlan(dto));
        verify(projectPlanMapper).batchInsert(dto.getProjectPlans(), 12L);
        verify(redisTemplate).delete(NURSING_PLAN_ALL_KEY);
    }

    @Test
    void updateWithProjectsReplacesExistingLinks() {
        NursingPlanDto dto = planWithOneProject();
        dto.setId(12L);
        when(projectPlanMapper.batchInsert(dto.getProjectPlans(), 12L)).thenReturn(1);
        when(planMapper.updateById(any(NursingPlan.class))).thenReturn(1);

        assertEquals(1, service.updateNursingPlan(dto));
        verify(projectPlanMapper).deleteByNursingPlanId(12L);
        verify(projectPlanMapper).batchInsert(dto.getProjectPlans(), 12L);
        verify(redisTemplate).delete(NURSING_PLAN_ALL_KEY);
    }

    @Test
    void updateWithoutProjectsLeavesExistingLinksUnchanged() {
        NursingPlanDto dto = new NursingPlanDto();
        dto.setId(12L);
        dto.setProjectPlans(Collections.emptyList());
        when(planMapper.updateById(any(NursingPlan.class))).thenReturn(1);

        assertEquals(1, service.updateNursingPlan(dto));
        verify(projectPlanMapper, never()).deleteByNursingPlanId(any());
        verify(projectPlanMapper, never()).batchInsert(any(), any());
        verify(redisTemplate).delete(NURSING_PLAN_ALL_KEY);
    }

    @Test
    void deleteRemovesProjectLinksAndInvalidatesCache() {
        when(planMapper.deleteById(12L)).thenReturn(1);

        assertEquals(1, service.deleteNursingPlanById(12L));
        verify(projectPlanMapper).deleteByNursingPlanId(12L);
        verify(redisTemplate).delete(NURSING_PLAN_ALL_KEY);
    }

    private NursingPlanDto planWithOneProject() {
        NursingPlanDto dto = new NursingPlanDto();
        dto.setPlanName("基础护理计划");
        NursingProjectPlan project = new NursingProjectPlan();
        project.setProjectId(8L);
        project.setExecuteTime("08:00:00");
        project.setExecuteCycle(0);
        project.setExecuteFrequency(1);
        dto.setProjectPlans(Collections.singletonList(project));
        return dto;
    }
}
