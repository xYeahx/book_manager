# 📚 图书管理系统 (Book Manager System)

基于 Spring Boot + Vue 2 的图书管理系统，支持多角色权限控制、图书借阅管理、消息通知等功能。

## 功能特性

- **多角色权限控制**：读者 / 管理员 / 超级管理员三级权限，动态路由与按钮级权限控制
- **图书管理**：图书信息与图书类型 CRUD、封面图片上传、按类型统计
- **借阅管理**：借书、还书、续借、借阅记录查询、逾期罚款、周统计
- **余额与押金**：账户余额（押金）管理、借阅扣费/退押金、交易流水明细
- **消息中心**：站内消息发送与查询、催还通知
- **系统管理**：操作日志、系统配置（如逾期罚款金额）管理
- **统一异常处理**：全局异常拦截、统一返回格式，保证接口稳定性

## 技术栈

| 层次 | 技术 | 版本 |
|------|------|------|
| 后端框架 | Spring Boot | 2.5.6 |
| JDK | Java | 8+ |
| ORM | MyBatis | 2.0.0 |
| 数据库 | MySQL | 5.7+ |
| 缓存 | Redis | 6.x |
| 前端框架 | Vue | 2.x |
| UI 组件 | Element UI | 2.13.2 |
| 状态管理 | Vuex | 3.x |
| HTTP 客户端 | Axios | 0.24+ |

## 前置条件

在启动项目前，请确保已安装以下环境：

| 工具 | 版本要求 | 用途 |
|------|---------|------|
| JDK | 8+ | 运行后端 |
| Maven | 3.6+ | 构建后端（也可用项目自带的 `mvnw`） |
| MySQL | 5.7+ | 数据存储 |
| Redis | 6.x | 会话管理（Token 存储） |
| Node.js | 8.9+ | 运行前端 |
| npm | 3.0+ | 安装前端依赖 |

## Docker 一键启动（推荐）

项目根目录提供了 `docker-compose.yml`，可一键启动 MySQL 5.7 与 Redis 6：

```bash
cd book_manager
docker compose up -d
```

- MySQL：`localhost:3306`，账号 `root` / 密码 `root`，数据库 `book_manager`（首次启动自动导入 `BookManager2-master/book_manager.sql`）
- Redis：`localhost:6379`（无密码）

> 采用 Docker 方式时，可跳过下方“初始化数据库”与“配置数据库连接”两步（默认配置已匹配），直接启动后端与前端。

如需清空数据并重置数据库，可在项目根目录执行：

```powershell
.\reset-db.ps1
```

脚本会依次执行 `docker compose down -v`、`docker compose up -d`，并等待 MySQL 就绪后提示完成。

## 快速启动

### 1. 初始化数据库

```sql
-- 登录 MySQL 并创建数据库
mysql -u root -p
CREATE DATABASE IF NOT EXISTS book_manager DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
exit;

-- 导入表结构和初始数据
mysql -u root -p book_manager < BookManager2-master/book_manager.sql
```

### 1.1 数据库升级（可选）

如果项目从旧版本升级而来，需要补充余额/押金、交易流水等新结构，可执行迁移脚本：

```bash
mysql -u root -p book_manager < migration.sql
```

该脚本会为 `user` 表添加 `balance` 字段、创建 `transaction` 流水表，并写入逾期罚款金额等系统配置（重复执行会自动跳过已存在的内容）。

### 2. 配置数据库连接

编辑 `BookManager2-master/src/main/resources/application.properties`，修改为你的数据库和 Redis 配置：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/book_manager
spring.datasource.username=root
spring.datasource.password=your_password

spring.redis.host=localhost
spring.redis.port=6379
spring.redis.password=        # 如果没有密码则留空
```

### 3. 启动后端

```bash
cd BookManager2-master
mvn spring-boot:run
```

或使用项目自带的 Maven 包装器：

```bash
cd BookManager2-master
./mvnw spring-boot:run         # macOS / Linux
.\mvnw.cmd spring-boot:run     # Windows
```

后端默认启动在 `http://localhost:8092/BookManager`

### 4. 启动前端

```bash
cd BookManagerVue-permission-control
npm install
npm run dev
```

前端默认启动在 `http://localhost:9528`

> **开发模式说明**：前端默认会加载 Mock 数据，无需后端即可预览 UI。如需对接真实后端，请确保后端已启动，API 已在 `src/utils/request.js` 中配置为 `http://localhost:8092/BookManager/`。

### 5. 访问系统

打开浏览器访问：`http://localhost:9528`

## 测试账号

| 用户名 | 密码 | 角色 | 说明 |
|--------|------|------|------|
| `周一` | `123456` | 读者 | 可借阅图书、查看个人记录、接收消息 |
| `周日` | `123456` | 管理员 | 可管理图书、读者用户、查看日志 |
| `周继业` | `123456` | 超级管理员 | 拥有全部权限，可管理管理员和系统设置 |

> 注册管理员账号需要邀请码，默认邀请码为 `xiaoye123`（可在系统设置中修改）。

## 角色说明

| 角色 | 标识 | 权限 |
|------|------|------|
| **读者** (reader) | `isAdmin = 0` | 浏览图书、借阅/归还图书、查看个人借阅记录、接收消息通知、修改个人信息 |
| **管理员** (admin) | `isAdmin = 1` | 读者所有权限 + 管理图书信息、管理图书类型、管理读者用户、查看操作日志 |
| **超级管理员** (super_admin) | `isAdmin = 2` | 管理员所有权限 + 管理管理员用户、管理系统设置、配置系统参数 |

## 项目结构

```
book_manager/
├── BookManager2-master/                  # 后端项目（Spring Boot）
│   ├── src/main/java/com/example/bms/
│   │   ├── web/                   # 控制器层
│   │   ├── service/               # 服务层接口
│   │   ├── service/impl/          # 服务层实现
│   │   ├── mapper/                # MyBatis 数据访问层
│   │   ├── model/                 # 数据模型（实体类）
│   │   ├── config/                # 配置类（CORS、拦截器）
│   │   ├── exception/             # 自定义异常
│   │   ├── interceptor/           # 登录/权限拦截器
│   │   └── utils/                 # 工具类
│   ├── src/main/resources/
│   │   ├── application.properties # 应用配置
│   │   └── static/                # 前端构建产物（生产部署）
│   ├── book_manager.sql           # 数据库初始化脚本
│   └── pom.xml                    # Maven 构建配置
│
├── BookManagerVue-permission-control/    # 前端项目（Vue 2）
│   ├── src/
│   │   ├── views/                 # 页面组件
│   │   ├── components/            # 公共组件
│   │   ├── api/                   # API 接口封装
│   │   ├── store/                 # Vuex 状态管理
│   │   ├── router/                # 路由配置（含动态权限）
│   │   ├── layout/                # 布局组件
│   │   ├── utils/                 # 工具函数
│   │   └── directive/             # 自定义指令（权限、水波纹）
│   └── vue.config.js              # Vue CLI 配置
│
├── 数据字典.md                     # 数据库设计、API 接口文档
└── README.md                       # 本文件
```

## API 概览

完整的 API 接口文档请见 [`数据字典.md`](./数据字典.md)，主要模块包括：

- **用户管理**：登录、注册、信息管理、密码修改
- **图书管理**：图书信息 CRUD、按类型统计、读者端查询
- **借阅管理**：借书、还书、续借、借阅记录查询、逾期罚款、周统计
- **账户余额**：押金充值/扣费、交易流水查询
- **图书类型**：类型 CRUD
- **消息中心**：发送消息、查询消息、催还通知
- **系统管理**：操作日志、系统配置管理
- **图片上传**：图书封面上传

## 构建部署

### 后端构建

```bash
cd BookManager2-master
mvn clean package -DskipTests
java -jar target/BookManagerApi-1.0.5.jar
```

### 前端构建

```bash
cd BookManagerVue-permission-control
npm run build:prod
```

构建产物在 `dist/` 目录，可直接部署到后端 `src/main/resources/static/` 或任意 Web 服务器。

## 常见问题

**Q: 前端页面无法登录，提示 420 错误？**
A: 请确认后端已启动。如果使用 Mock 数据开发，无需启动后端；如需对接真实后端，请确保后端服务运行在 8092 端口。

**Q: Redis 连接失败？**
A: 请确认 Redis 服务已启动。如无需 Redis，可在 `application.properties` 中注释掉 Redis 相关配置，并修改 `UserServiceImpl` 中 Token 存储方式。

**Q: 数据库导入乱码？**
A: 请使用 `utf8mb4` 字符集创建数据库，SQL 文件已采用 UTF-8 编码。

## License

MIT
