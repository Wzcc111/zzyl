package com.zzyl.nursing.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.zzyl.nursing.domain.Bed;
import com.zzyl.nursing.domain.CheckIn;
import com.zzyl.nursing.domain.CheckInConfig;
import com.zzyl.nursing.domain.Contract;
import com.zzyl.nursing.domain.Elder;
import com.zzyl.nursing.dto.CheckInApplyDto;
import com.zzyl.nursing.dto.CheckInConfigDto;
import com.zzyl.nursing.dto.CheckInContractDto;
import com.zzyl.nursing.dto.CheckInElderDto;
import com.zzyl.nursing.dto.ElderFamilyDto;
import com.zzyl.nursing.mapper.BedMapper;
import com.zzyl.nursing.mapper.CheckInConfigMapper;
import com.zzyl.nursing.mapper.CheckInMapper;
import com.zzyl.nursing.mapper.ContractMapper;
import com.zzyl.nursing.mapper.ElderMapper;
import com.zzyl.nursing.vo.CheckInDetailVo;
import cn.hutool.core.util.IdcardUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdmissionServiceContractTest {

    @Mock private CheckInMapper checkInMapper;
    @Mock private ElderMapper elderMapper;
    @Mock private BedMapper bedMapper;
    @Mock private CheckInConfigMapper checkInConfigMapper;
    @Mock private ContractMapper contractMapper;

    private CheckInServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CheckInServiceImpl();
        ReflectionTestUtils.setField(service, "checkInMapper", checkInMapper);
        ReflectionTestUtils.setField(service, "elderMapper", elderMapper);
        ReflectionTestUtils.setField(service, "bedMapper", bedMapper);
        ReflectionTestUtils.setField(service, "checkInConfigMapper", checkInConfigMapper);
        ReflectionTestUtils.setField(service, "contractMapper", contractMapper);
    }

    @Test
    void applyUsesBedIdAndWritesAllAdmissionRecords() {
        CheckInApplyDto request = admissionRequest();
        Bed bed = new Bed();
        bed.setId(14L);
        bed.setBedNumber("203-1");
        bed.setBedStatus(0);

        when(elderMapper.selectOne(any(Wrapper.class))).thenReturn(null);
        when(bedMapper.selectById(14L)).thenReturn(bed);
        when(elderMapper.insert(any(Elder.class))).thenAnswer(invocation -> {
            Elder elder = invocation.getArgument(0);
            elder.setId(501L);
            return 1;
        });
        when(checkInMapper.insert(any(CheckIn.class))).thenAnswer(invocation -> {
            CheckIn checkIn = invocation.getArgument(0);
            checkIn.setId(601L);
            return 1;
        });

        service.apply(request);

        ArgumentCaptor<Bed> bedCaptor = ArgumentCaptor.forClass(Bed.class);
        verify(bedMapper).updateById(bedCaptor.capture());
        assertEquals(1, bedCaptor.getValue().getBedStatus());

        ArgumentCaptor<Elder> elderCaptor = ArgumentCaptor.forClass(Elder.class);
        verify(elderMapper).insert(elderCaptor.capture());
        assertEquals("203-1", elderCaptor.getValue().getBedNumber());
        assertEquals(14L, elderCaptor.getValue().getBedId());

        ArgumentCaptor<CheckIn> checkInCaptor = ArgumentCaptor.forClass(CheckIn.class);
        verify(checkInMapper).insert(checkInCaptor.capture());
        assertEquals("203-1", checkInCaptor.getValue().getBedNumber());
        assertEquals(0, checkInCaptor.getValue().getStatus());

        ArgumentCaptor<CheckInConfig> configCaptor = ArgumentCaptor.forClass(CheckInConfig.class);
        verify(checkInConfigMapper).insert(configCaptor.capture());
        assertEquals(601L, configCaptor.getValue().getCheckInId());

        ArgumentCaptor<Contract> contractCaptor = ArgumentCaptor.forClass(Contract.class);
        verify(contractMapper).insert(contractCaptor.capture());
        assertEquals(501L, contractCaptor.getValue().getElderId());
        assertEquals("https://example.test/contract.png", contractCaptor.getValue().getAgreementPath());
        assertNotNull(contractCaptor.getValue().getContractNumber());
    }

    @Test
    void applyRejectsMissingAgreementBeforeWritingAnything() {
        CheckInApplyDto request = admissionRequest();
        request.getCheckInContractDto().setAgreementPath(null);

        assertThrows(com.zzyl.common.exception.ServiceException.class, () -> service.apply(request));
        verifyNoInteractions(bedMapper, elderMapper, checkInMapper, contractMapper, checkInConfigMapper);
    }

    @Test
    void listDoesNotForceOnlyActiveAdmissions() {
        CheckIn filter = new CheckIn();
        service.selectCheckInList(filter);
        assertEquals(null, filter.getStatus());
        verify(checkInMapper).selectCheckInList(filter);
    }

    @Test
    void applyRejectsElderAlreadyInAdmissionStatusFour() {
        CheckInApplyDto request = admissionRequest();
        Elder existing = new Elder();
        existing.setStatus(4);
        when(elderMapper.selectOne(any(Wrapper.class))).thenReturn(existing);
        assertThrows(com.zzyl.common.exception.ServiceException.class, () -> service.apply(request));
        verifyNoInteractions(bedMapper, checkInMapper, contractMapper, checkInConfigMapper);
    }

    @Test
    void detailAssemblesElderFamilyConfigAndContract() {
        CheckIn checkIn = new CheckIn();
        checkIn.setId(6L);
        checkIn.setElderId(326L);
        checkIn.setElderName("张三");
        checkIn.setBedNumber("106-1");
        checkIn.setStartDate(LocalDateTime.of(2024, 8, 26, 0, 0));
        checkIn.setEndDate(LocalDateTime.of(2024, 9, 30, 0, 0));
        checkIn.setRemark("[{\"name\":\"李彤\",\"phone\":\"13211223311\",\"kinship\":\"0\"}]");

        Elder elder = new Elder();
        elder.setId(326L);
        elder.setName("李天龙");
        elder.setBirthday("1956-12-13");
        elder.setIdCardNo("132123196712131239");

        CheckInConfig config = new CheckInConfig();
        config.setId(2L);
        config.setCheckInId(6L);

        Contract contract = new Contract();
        contract.setId(2L);
        contract.setElderId(326L);

        when(checkInMapper.selectCheckInById(6L)).thenReturn(checkIn);
        when(elderMapper.selectById(326L)).thenReturn(elder);
        when(checkInConfigMapper.selectOne(any(Wrapper.class))).thenReturn(config);
        when(contractMapper.selectOne(any(Wrapper.class))).thenReturn(contract);

        CheckInDetailVo result = service.detail(6L);

        assertEquals("张三", result.getCheckInElderVo().getName());
        assertEquals(IdcardUtil.getAgeByIdCard(elder.getIdCardNo()), result.getCheckInElderVo().getAge());
        assertEquals(1, result.getElderFamilyVoList().size());
        assertEquals("李彤", result.getElderFamilyVoList().get(0).getName());
        assertEquals("106-1", result.getCheckInConfigVo().getBedNumber());
        assertEquals(checkIn.getStartDate(), result.getCheckInConfigVo().getStartDate());
        assertEquals(contract, result.getContract());
    }

    private CheckInApplyDto admissionRequest() {
        CheckInElderDto elder = new CheckInElderDto();
        elder.setName("测试老人");
        elder.setIdCardNo("132123194612191234");
        elder.setBirthday("1946-12-19");
        elder.setPhone("15543234565");

        ElderFamilyDto family = new ElderFamilyDto();
        family.setName("测试家属");
        family.setPhone("15543234568");
        family.setKinship("1");

        CheckInConfigDto config = new CheckInConfigDto();
        config.setBedId(14L);
        config.setNursingLevelId(77L);
        config.setNursingLevelName("一级护理等级");
        config.setStartDate(LocalDateTime.of(2026, 10, 23, 0, 0));
        config.setEndDate(LocalDateTime.of(2027, 10, 23, 0, 0));
        config.setFeeStartDate(config.getStartDate());
        config.setFeeEndDate(config.getEndDate());
        config.setDeposit(new BigDecimal("3000"));
        config.setNursingFee(new BigDecimal("2000"));
        config.setBedFee(new BigDecimal("4000"));

        CheckInContractDto contract = new CheckInContractDto();
        contract.setContractName("测试入住合同");
        contract.setAgreementPath("https://example.test/contract.png");
        contract.setThirdPartyName("测试家属");
        contract.setThirdPartyPhone("15543234568");
        contract.setSignDate(LocalDateTime.of(2026, 10, 23, 9, 0));

        CheckInApplyDto request = new CheckInApplyDto();
        request.setCheckInElderDto(elder);
        request.setElderFamilyDtoList(Collections.singletonList(family));
        request.setCheckInConfigDto(config);
        request.setCheckInContractDto(contract);
        return request;
    }
}
