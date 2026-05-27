package com.greendam.birdhelp.service.admin.impl;

import cn.hutool.crypto.symmetric.AES;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.greendam.birdhelp.exception.BusinessException;
import com.greendam.birdhelp.exception.ErrorCode;
import com.greendam.birdhelp.mapper.ApiKeyMapper;
import com.greendam.birdhelp.model.dto.admin.ApiKeyCreateDTO;
import com.greendam.birdhelp.model.dto.admin.ApiKeyUpdateDTO;
import com.greendam.birdhelp.model.entity.ApiKey;
import com.greendam.birdhelp.model.vo.admin.ApiKeyVO;
import com.greendam.birdhelp.service.admin.ApiKeyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * API密钥管理服务实现类。
 * <p>
 * 使用AES加密算法（CBC模式，PKCS7Padding填充）对API密钥进行加密存储，
 * 提供密钥的完整生命周期管理。支持密钥的创建、查询、更新、删除以及启用/禁用
 * 状态切换等操作。所有返回给前端的密钥信息均经过脱敏处理。
 * </p>
 *
 * @author ForeverGreenDam
 */
@Slf4j
@Service
public class ApiKeyServiceImpl extends ServiceImpl<ApiKeyMapper, ApiKey> implements ApiKeyService {

    /**
     * AES加密密钥，从配置文件中注入。
     */
    @Value("${birdhelp.api-key.encryption-secret}")
    private String encryptionSecret;

    @Override
    public Page<ApiKeyVO> listApiKeys(int page, int size) {
        Page<ApiKey> entityPage = page(Page.of(page, size),
                new LambdaQueryWrapper<ApiKey>().orderByDesc(ApiKey::getCreateTime));
        Page<ApiKeyVO> voPage = new Page<>(page, size, entityPage.getTotal());
        voPage.setRecords(entityPage.getRecords().stream().map(e -> ApiKeyVO.builder()
                .id(e.getId())
                .providerName(e.getProviderName())
                .apiKeyMasked(maskKey(""))
                .baseUrl(e.getBaseUrl())
                .modelName(e.getModelName())
                .enabled(e.getEnabled())
                .description(e.getDescription())
                .createTime(e.getCreateTime())
                .updateTime(e.getUpdateTime())
                .build()).toList());
        return voPage;
    }

    @Override
    public ApiKeyVO getApiKeyDetail(Long id) {
        ApiKey entity = getById(id);
        if (entity == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "API密钥不存在");
        }
        String decrypted = decryptKey(entity.getApiKey());
        return ApiKeyVO.builder()
                .id(entity.getId())
                .providerName(entity.getProviderName())
                .apiKeyMasked(maskKey(decrypted))
                .baseUrl(entity.getBaseUrl())
                .modelName(entity.getModelName())
                .enabled(entity.getEnabled())
                .description(entity.getDescription())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }

    @Override
    public void createApiKey(ApiKeyCreateDTO dto) {
        ApiKey entity = new ApiKey();
        entity.setProviderName(dto.getProviderName());
        entity.setApiKey(encryptKey(dto.getApiKey()));
        entity.setBaseUrl(dto.getBaseUrl());
        entity.setModelName(dto.getModelName());
        entity.setDescription(dto.getDescription());
        entity.setEnabled(1);
        save(entity);
        log.info("创建API密钥: provider={}, baseUrl={}, model={}", dto.getProviderName(), dto.getBaseUrl(), dto.getModelName());
    }

    @Override
    public void updateApiKey(ApiKeyUpdateDTO dto) {
        ApiKey entity = getById(dto.getId());
        if (entity == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "API密钥不存在");
        }
        if (dto.getProviderName() != null) entity.setProviderName(dto.getProviderName());
        if (dto.getApiKey() != null) entity.setApiKey(encryptKey(dto.getApiKey()));
        if (dto.getBaseUrl() != null) entity.setBaseUrl(dto.getBaseUrl());
        if (dto.getModelName() != null) entity.setModelName(dto.getModelName());
        if (dto.getDescription() != null) entity.setDescription(dto.getDescription());
        updateById(entity);
        log.info("更新API密钥: id={}", dto.getId());
    }

    @Override
    public void deleteApiKey(Long id) {
        removeById(id);
        log.info("删除API密钥: id={}", id);
    }

    @Override
    public void toggleEnabled(Long id, Boolean enabled) {
        ApiKey entity = getById(id);
        if (entity == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "API密钥不存在");
        }
        entity.setEnabled(enabled ? 1 : 0);
        updateById(entity);
        log.info("切换API密钥状态: id={}, enabled={}", id, enabled);
    }

    @Override
    public List<Map<String, Object>> fetchDecryptedKeys(String providerName) {
        LambdaQueryWrapper<ApiKey> wrapper = new LambdaQueryWrapper<ApiKey>()
                .eq(ApiKey::getEnabled, 1);
        if (providerName != null && !providerName.isEmpty()) {
            wrapper.eq(ApiKey::getProviderName, providerName);
        }
        List<ApiKey> keys = list(wrapper);
        List<Map<String, Object>> result = new ArrayList<>();
        for (ApiKey k : keys) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("providerName", k.getProviderName());
            item.put("apiKey", decryptKey(k.getApiKey()));
            item.put("baseUrl", k.getBaseUrl());
            item.put("modelName", k.getModelName());
            result.add(item);
        }
        return result;
    }

    @Override
    public Map<String, String> resolveCredentials(String modelName) {
        List<ApiKey> keys = list(new LambdaQueryWrapper<ApiKey>().eq(ApiKey::getEnabled, 1));
        if (keys.isEmpty()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "没有可用的LLM API密钥，请联系管理员配置");
        }
        ApiKey target;
        if (modelName != null && !modelName.isEmpty()) {
            target = keys.stream()
                    .filter(k -> modelName.equals(k.getModelName()))
                    .findFirst()
                    .orElse(keys.get(0));
        } else {
            target = keys.get(0);
        }
        Map<String, String> result = new LinkedHashMap<>();
        result.put("apiKey", decryptKey(target.getApiKey()));
        result.put("baseUrl", target.getBaseUrl());
        result.put("modelName", target.getModelName());
        log.info("解析LLM凭证: requested={}, resolved={}", modelName, target.getModelName());
        return result;
    }

    @Override
    public List<Map<String, String>> listAvailableModels() {
        List<ApiKey> keys = list(new LambdaQueryWrapper<ApiKey>()
                .eq(ApiKey::getEnabled, 1)
                .orderByAsc(ApiKey::getProviderName));
        List<Map<String, String>> result = new ArrayList<>();
        for (ApiKey k : keys) {
            Map<String, String> item = new LinkedHashMap<>();
            item.put("modelName", k.getModelName());
            item.put("providerName", k.getProviderName());
            item.put("description", k.getDescription() != null ? k.getDescription() : "");
            result.add(item);
        }
        return result;
    }

    /**
     * 使用AES算法加密API密钥明文。
     * <p>
     * 采用CBC模式、PKCS7Padding填充方式，使用配置的加密密钥进行加密，
     * 最终输出Base64编码的密文字符串。
     * </p>
     *
     * @param rawKey API密钥明文
     * @return Base64编码的加密密文
     */
    private String encryptKey(String rawKey) {
        AES aes = new AES("CBC", "PKCS7Padding",
                encryptionSecret.getBytes(StandardCharsets.UTF_8),
                encryptionSecret.substring(0, 16).getBytes(StandardCharsets.UTF_8));
        return aes.encryptBase64(rawKey);
    }

    /**
     * 解密AES加密的API密钥密文。
     * <p>
     * 使用与加密时相同的密钥和参数进行解密，还原出API密钥明文。
     * </p>
     *
     * @param encryptedKey Base64编码的加密密文
     * @return 解密后的API密钥明文
     */
    private String decryptKey(String encryptedKey) {
        AES aes = new AES("CBC", "PKCS7Padding",
                encryptionSecret.getBytes(StandardCharsets.UTF_8),
                encryptionSecret.substring(0, 16).getBytes(StandardCharsets.UTF_8));
        return aes.decryptStr(encryptedKey);
    }

    /**
     * 对API密钥进行脱敏处理。
     * <p>
     * 仅保留密钥的前4位和后4位，中间部分替换为"****"。
     * 如果密钥长度小于等于8位，则直接返回"****"。
     * </p>
     *
     * @param key API密钥明文
     * @return 脱敏后的密钥字符串，格式为 "前4位****后4位"
     */
    private String maskKey(String key) {
        if (key == null || key.length() <= 8) return "****";
        return key.substring(0, 4) + "****" + key.substring(key.length() - 4);
    }
}
