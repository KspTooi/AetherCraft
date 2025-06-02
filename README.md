# AetherCraft
AetherCraft是一个基于SpringBoot 3.4.1开发的AI模型聊天和管理平台，提供了多模型聊天、用户权限控制等功能。

## 项目概述

AetherCraft是一个AI大语言模型的聊天平台，目前支持Gemini、Grok等模型，并提供用户管理和权限控制功能。该项目采用H2数据库作为存储，使用Undertow作为Web服务器，具有轻量级、高性能的特点。

AetherCraft的特色功能是AI角色扮演(RP, Role-Play)，允许用户创建AI角色并与其对话。系统支持用户角色和AI角色的交互，用户可以选择角色身份与AI角色互动。角色可以设置头像、角色设定和对话内容，系统也支持对话存档功能。

本项目95%的开发与设计均由AI辅助完成，可能存在一些未尽之处或潜在的缺陷。如果您在使用过程中遇到任何问题或有改进建议，欢迎随时在GitHub上提出ISSUE，您的反馈对我们至关重要。

## 主要功能

### AI模型聊天
- 支持多种AI模型（如Gemini、Grok等）
- 支持创建和管理聊天线程
- 支持流式响应和批量响应
- 支持聊天历史记录管理
- 支持自定义系统提示词
- 支持调整基本模型参数

### AI角色扮演(RP)功能
- 支持创建和管理AI角色，包括角色名称、描述等
- 支持为角色设置情景和首次对话内容
- 支持角色对话存档功能
- 支持用户角色和AI角色交互
- 支持对话流式生成
- 支持中断AI响应

### AI模型角色管理
- 支持创建和管理AI角色
- 支持为角色设置头像
- 支持为角色配置系统提示词
- 支持设置基本参数

### 用户角色管理
- 支持创建和管理用户角色
- 支持为用户角色设置头像
- 支持设置默认用户角色

### API密钥管理
- 支持创建和管理API密钥
- 支持设置API密钥的有效期
- 支持基本权限管理

### 用户管理
- 用户登录和权限控制
- 用户角色分配


## 技术栈

### 后端技术
- **框架**：SpringBoot 3.4.1
- **数据库**：H2 Database
- **Web服务器**：Undertow
- **ORM框架**：Spring Data JPA
- **HTTP客户端**：OkHttp
- **系统监控**：OSHI
- **其他工具**：Lombok、Gson、Commons-lang3

### 前端技术
- **模板引擎**：Thymeleaf（服务端渲染）
- **UI框架**：Bootstrap 5
- **JavaScript库**：jQuery
- **图标库**：Font Awesome
- **CSS预处理器**：原生CSS
- **HTTP请求**：Fetch API、Axios

### 服务端渲染(SSR)
AetherCraft采用Thymeleaf作为模板引擎实现服务端渲染，主要特点包括：
- 无需构建复杂的前端SPA应用，降低开发复杂度
- 页面在服务器端渲染完成后发送到客户端，提高首屏加载速度
- 更好的SEO支持，搜索引擎可以直接抓取完整页面内容
- 结合Spring MVC，可以直接在控制器中传递模型数据到视图
- 使用ModelAndView响应模式，支持页面局部刷新和数据回显

## 系统要求

- JDK 21+
- Maven 3.6+
- Windows操作系统（部分功能依赖Windows系统）

## 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/yourusername/AetherCraft.git
cd AetherCraft
```

### 2. 编译项目

```bash
mvn clean package
```

### 3. 运行项目

```bash
java -jar target/AetherCraft-1.1-H-M6.jar
```

### 4. 访问系统

打开浏览器，访问 http://localhost:30000

## 项目结构

```
AetherCraft/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── ksptool/
│   │   │           └── ql/
│   │   │               ├── biz/
│   │   │               │   ├── controller/  # 控制器
│   │   │               │   │   └── panel/   # 管理面板控制器
│   │   │               │   ├── service/     # 服务层
│   │   │               │   │   └── panel/   # 管理面板服务
│   │   │               │   ├── mapper/      # 数据访问层
│   │   │               │   └── model/       # 数据模型
│   │   │               │       ├── dto/     # 数据传输对象
│   │   │               │       ├── vo/      # 视图对象
│   │   │               │       ├── po/      # 持久化对象
│   │   │               │       ├── gemini/  # Gemini模型相关对象
│   │   │               │       └── grok/    # Grok模型相关对象
│   │   │               └── commons/         # 公共组件
│   │   └── resources/
│   │       ├── views/              # 前端视图
│   │       │   ├── components/     # 可复用组件
│   │       │   ├── commons/        # 公共页面元素
│   │       │   ├── static/         # 静态资源
│   │       │   │   ├── css/        # 样式文件
│   │       │   │   ├── js/         # JavaScript文件
│   │       │   │   └── images/     # 图片资源
│   │       │   └── *.html          # 页面模板
│   │       └── application.yml     # 应用配置
│   └── test/                       # 测试代码
└── pom.xml                         # Maven配置
```


## 配置说明

主要配置文件位于 `src/main/resources/application.yml`，包含以下配置：

- 服务器端口：默认为30000
- 数据库配置：H2数据库，默认端口30001
- Thymeleaf模板配置
- JPA配置
- 会话超时配置

## 管理面板功能

AetherCraft提供了基础的管理面板功能：

### 模型角色管理 (PanelModelRoleController)
- 创建和管理AI模型的角色信息
- 配置角色的基本参数
- 上传和管理角色头像
- 设置角色的情景和对话内容

### 模型配置管理 (PanelModelConfigController)
- 配置AI模型的基本参数
- 启用/禁用模型

### 用户角色管理 (PanelModelUserRoleController)
- 创建和管理用户角色
- 管理用户角色的头像
- 设置默认用户角色

### API密钥管理 (PanelApiKeyController)
- 创建和管理API密钥
- 设置API密钥的有效期

### 系统维护 (PanelMaintainController)
- 基本系统配置管理

## 开发状态

本项目仍在开发中，部分功能可能尚未完全实现或优化。欢迎贡献代码或提出建议。

## 许可证

[添加您的许可证信息]

## 联系方式

[添加您的联系方式] 