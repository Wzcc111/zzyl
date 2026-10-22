package com.zzyl.nursing.service;

import com.zzyl.nursing.domain.NursingPlan;
import com.zzyl.nursing.domain.NursingProjectPlan;
import com.zzyl.nursing.dto.NursingPlanDto;
import com.zzyl.nursing.mapper.NursingPlanMapper;
import com.zzyl.nursing.mapper.NursingProjectPlanMapper;
import com.zzyl.nursing.service.impl.NursingPlanServiceImpl;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NursingPlanServiceImplTest {
    @Mock NursingPlanMapper planMapper;
    @Mock NursingProjectPlanMapper projectPlanMapper;
    @InjectMocks NursingPlanServiceImpl service;

    @Test
    void missingPlanDoesNotQueryProjects() {
        when(planMapper.selectById(99L)).thenReturn(null);
        assertNull(service.selectNursingPlanById(99L));
        verify(projectPlanMapper, never()).selectByPlanId(any());
    }

    @Test
    void insertUsesGeneratedPlanIdForProjects() {
        NursingPlanDto dto = new NursingPlanDto();
        NursingProjectPlan project = new NursingProjectPlan();
        project.setProjectId(8L);
        project.setExecuteTime("08:00:00");
        project.setExecuteCycle(0);
        project.setExecuteFrequency(1);
        dto.setProjectPlans(Collections.singletonList(project));
        doAnswer(invocation -> {
            invocation.<NursingPlan>getArgument(0).setId(12L);
            return 1;
        }).when(planMapper).insert(any(NursingPlan.class));
        when(projectPlanMapper.insertForPlan(eq(12L), any())).thenReturn(1);

        assertEquals(1, service.insertNursingPlan(dto));
        verify(projectPlanMapper).insertForPlan(12L, dto.getProjectPlans());
    }

    @Test
    void emptyProjectsOnUpdateClearsExistingLinks() {
        NursingPlanDto dto = new NursingPlanDto();
        dto.setId(12L);
        dto.setProjectPlans(Collections.emptyList());
        when(planMapper.updateById(any(NursingPlan.class))).thenReturn(1);

        assertEquals(1, service.updateNursingPlan(dto));
        verify(projectPlanMapper).deleteByPlanId(12L);
        verify(projectPlanMapper, never()).insertForPlan(any(), any());
    }

    @Test
    void invalidProjectRejectsInsertBeforeWritingLinks() {
        NursingPlanDto dto = new NursingPlanDto();
        dto.setProjectPlans(Collections.singletonList(new NursingProjectPlan()));
        doAnswer(invocation -> {
            invocation.<NursingPlan>getArgument(0).setId(12L);
            return 1;
        }).when(planMapper).insert(any(NursingPlan.class));

        assertThrows(IllegalArgumentException.class, () -> service.insertNursingPlan(dto));
        verify(projectPlanMapper, never()).insertForPlan(any(), any());
    }

    @Test
    void batchDeleteRemovesLinksForEveryPlan() {
        when(planMapper.deleteById(12L)).thenReturn(1);
        when(planMapper.deleteById(13L)).thenReturn(1);

        assertEquals(2, service.deleteNursingPlanByIds(new Long[] {12L, 13L}));
        verify(projectPlanMapper).deleteByPlanId(12L);
        verify(projectPlanMapper).deleteByPlanId(13L);
    }
}
