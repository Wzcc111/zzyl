package com.zzyl.web.controller.common;

import com.zzyl.common.core.domain.AjaxResult;
import com.zzyl.oss.AliyunOSSOperator;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CommonControllerUploadTest
{
    @Test
    void singleUploadReturnsOssResponseContract() throws Exception
    {
        String ossUrl = "https://zznursing.oss-cn-beijing.aliyuncs.com/2026/10/demo.png";
        AliyunOSSOperator operator = mock(AliyunOSSOperator.class);
        when(operator.upload(any(byte[].class), eq("demo.png"))).thenReturn(ossUrl);

        CommonController controller = new CommonController();
        ReflectionTestUtils.setField(controller, "aliyunOSSOperator", operator);
        MockMultipartFile file = new MockMultipartFile(
                "file", "demo.png", "image/png", "image-content".getBytes()
        );

        AjaxResult result = controller.uploadFile(file);

        assertThat(result.get("code")).isEqualTo(200);
        assertThat(result.get("url")).isEqualTo(ossUrl);
        assertThat(result.get("fileName")).isEqualTo(ossUrl);
        assertThat(result.get("newFileName")).isEqualTo("demo.png");
        assertThat(result.get("originalFilename")).isEqualTo("demo.png");
        verify(operator).upload(file.getBytes(), "demo.png");
    }
}
