package com.zzyl.nursing.service;

import com.zzyl.common.exception.ServiceException;
import com.zzyl.nursing.domain.NursingLevel;
import com.zzyl.nursing.domain.NursingPlan;
import com.zzyl.nursing.mapper.NursingLevelMapper;
import com.zzyl.nursing.mapper.NursingPlanMapper;
import com.zzyl.nursing.service.impl.NursingLevelServiceImpl;
import com.zzyl.nursing.vo.NursingLevelVo;
import java.math.BigDecimal;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NursingLevelServiceImplTest {
    @Mock NursingLevelMapper levelMapper;
    @Mock NursingPlanMapper planMapper;
    @InjectMocks NursingLevelServiceImpl service;

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
    void createRejectsMissingPlanBeforeInsert() {
        NursingLevel level = validLevel();
        when(planMapper.selectById(3L)).thenReturn(null);

        assertThrows(ServiceException.class, () -> service.insertNursingLevel(level));
        verify(levelMapper, never()).insert(any());
    }

    @Test
    void statusOnlyUpdateDoesNotRequireOtherFields() {
        NursingLevel level = new NursingLevel();
        level.setId(7L);
        level.setStatus(0);
        when(levelMapper.updateById(level)).thenReturn(1);

        assertEquals(1, service.updateNursingLevel(level));
        verify(planMapper, never()).selectById(any());
    }

    @Test
    void createRejectsNegativeFee() {
        NursingLevel level = validLevel();
        level.setFee(new BigDecimal("-0.01"));
        NursingPlan plan = new NursingPlan();
        plan.setStatus(1);
        when(planMapper.selectById(3L)).thenReturn(plan);

        assertThrows(ServiceException.class, () -> service.insertNursingLevel(level));
        verify(levelMapper, never()).insert(any());
    }

    @Test
    void createRejectsDisabledPlan() {
        NursingLevel level = validLevel();
        NursingPlan plan = new NursingPlan();
        plan.setStatus(0);
        when(planMapper.selectById(3L)).thenReturn(plan);

        assertThrows(ServiceException.class, () -> service.insertNursingLevel(level));
        verify(levelMapper, never()).insert(any());
    }

    @Test
    void createRejectsEmojiInName() {
        NursingLevel level = validLevel();
        level.setName("一级护理😀");

        assertThrows(ServiceException.class, () -> service.insertNursingLevel(level));
        verify(levelMapper, never()).insert(any());
    }

    private NursingLevel validLevel() {
        NursingLevel level = new NursingLevel();
        level.setName("一级护理");
        level.setPlanId(3L);
        level.setFee(new BigDecimal("100.00"));
        level.setStatus(1);
        level.setDescription("日常照护");
        return level;
    }
}
