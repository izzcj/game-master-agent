package io.github.izzcj.gamemaster.controller;

import io.github.izzcj.gamemaster.ai.AgentModelProperties;
import io.github.izzcj.gamemaster.ai.ModelRouter;
import io.github.izzcj.gamemaster.ai.ModelScene;
import io.github.izzcj.gamemaster.model.response.ModelBindingResponse;
import io.github.izzcj.gamemaster.model.response.ModelConfigResponse;
import io.github.izzcj.gamemaster.model.response.SearchSourceResponse;
import io.github.izzcj.gamemaster.search.SearchProperties;
import io.github.izzcj.gamemaster.support.ApiResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台管理接口控制器。
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final ModelRouter modelRouter;
    private final SearchProperties searchProperties;

    public AdminController(ModelRouter modelRouter, SearchProperties searchProperties) {
        this.modelRouter = modelRouter;
        this.searchProperties = searchProperties;
    }

    /**
     * 查看当前模型绑定配置。
     *
     * @return 模型配置响应
     */
    @GetMapping("/models")
    public ApiResponse<ModelConfigResponse> getModels() {
        List<ModelBindingResponse> sceneBindings = modelRouter.describeChatScenes().entrySet().stream()
            .map(entry -> toResponse(entry.getKey(), entry.getValue()))
            .toList();
        ModelBindingResponse embedding = toResponse(null, modelRouter.selectEmbeddingModel());
        return ApiResponse.ok(new ModelConfigResponse(sceneBindings, embedding));
    }

    /**
     * 重新加载模型配置。
     *
     * @return 重载后的模型配置
     */
    @PostMapping("/models/reload")
    public ApiResponse<ModelConfigResponse> reloadModels() {
        modelRouter.reload();
        return getModels();
    }

    /**
     * 查询搜索源配置。
     *
     * @return 搜索源列表
     */
    @GetMapping("/search-sources")
    public ApiResponse<List<SearchSourceResponse>> getSearchSources() {
        List<SearchSourceResponse> sources = searchProperties.getProviders().stream()
            .map(provider -> new SearchSourceResponse(provider.getName(), provider.getType(), provider.isEnabled()))
            .toList();
        return ApiResponse.ok(sources);
    }

    /**
     * 将模型绑定转换为响应对象。
     *
     * @param scene 模型场景
     * @param binding 模型绑定
     * @return 模型绑定响应
     */
    private ModelBindingResponse toResponse(ModelScene scene, AgentModelProperties.ModelBinding binding) {
        return new ModelBindingResponse(
            scene == null ? "EMBEDDING" : scene.name(),
            scene == null ? "embedding" : scene.getKey(),
            binding.getBeanName(),
            binding.getProvider(),
            binding.getDisplayName()
        );
    }
}
