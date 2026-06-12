package com.example.agent.config;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import java.util.function.Function;

@Configuration
public class ToolsConfig {
    // 🚀 新增：注册一个基于内存的聊天记忆体
    // (在真实的生产环境中，这里通常会换成 RedisChatMemory，但为了极速冲刺，我们先用内置内存版跑通逻辑)
    @Bean
    public ChatMemory chatMemory() {
        return new InMemoryChatMemory();
    }

    // ==========================================
    // Tool 1: 费控系统报销进度查询 (HR/财务场景)
    // ==========================================
    public record ExpenseQueryRequest(String employeeName) {}
    public record ExpenseQueryResponse(String status, String expectedPaymentDate, double amount) {}

    @Bean
    @Description("企业内部费控系统工具。当员工询问自己的报销单进度、何时打款等动态信息时调用此工具。必须提供员工姓名。")
    public Function<ExpenseQueryRequest, ExpenseQueryResponse> queryExpenseStatus() {
        return request -> {
            System.out.println("【Agent触发】正在查询费控系统，员工：" + request.employeeName());

            // 模拟数据库查询逻辑
            if ("林思远".equals(request.employeeName())) {
                return new ExpenseQueryResponse("财务总监审批中", "预计下个打款日(25日)", 1250.00);
            } else {
                return new ExpenseQueryResponse("已打款", "本月10日已完成打款", 650.00);
            }
        };
    }

    // ==========================================
    // Tool 2: 客户 API 速率限制查询 (技术/售前场景)
    // ==========================================
    public record ApiTierQueryRequest(String customerName) {}
    public record ApiTierQueryResponse(String customerName, String planType, int qpsLimit, String status) {}

    @Bean
    @Description("云帆智算 SailCore AI 引擎客户管理工具。当询问某位客户或公司的 API 速率限制、版本（基础版/企业专享版）时调用。必须提供客户公司名称。")
    public Function<ApiTierQueryRequest, ApiTierQueryResponse> queryCustomerApiTier() {
        return request -> {
            System.out.println("【Agent触发】正在查询 API 网关客户库，客户：" + request.customerName());

            // 模拟数据库查询逻辑
            if (request.customerName().contains("星轨互娱")) {
                return new ApiTierQueryResponse(request.customerName(), "千万级私有化部署版", 10000, "运行正常");
            } else {
                return new ApiTierQueryResponse(request.customerName(), "基础版", 50, "面临限流风险");
            }
        };
    }

    // ==========================================
    // Tool 3: 新员工 IT 设备配置查询 (行政/IT场景)
    // ==========================================
    public record EquipmentQueryRequest(String newEmployeeName) {}
    public record EquipmentQueryResponse(String deviceModel, String provisionStatus, String handler) {}

    @Bean
    @Description("Bamboohr 入职资产申领查询工具。当查询新员工的电脑、显示器等 IT 设备是否配置完毕时调用。必须提供新员工姓名。")
    public Function<EquipmentQueryRequest, EquipmentQueryResponse> queryITEquipmentStatus() {
        return request -> {
            System.out.println("【Agent触发】正在查询 IT 资产管理系统，新员工：" + request.newEmployeeName());

            return new EquipmentQueryResponse(
                    "16英寸 MacBook Pro (M3 Max) + 27寸4K显示器",
                    "配置中，预计今日 10:00 前完成",
                    "IT专员王波"
            );
        };
    }
}