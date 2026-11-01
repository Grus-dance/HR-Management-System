# X 公司人事管理系统

《网页设计与网站建设》课程项目：一个基于 **Java + Spring Boot + MyBatis + MySQL** 的分层解耦人事管理系统。
后端提供 REST 接口，前端为原生 HTML/CSS/JavaScript 页面，覆盖题目基础版、指定设计与进阶版的全部功能。

---

## 1. 功能一览

| 题目要求 | 实现情况 |
| --- | --- |
| 三类角色：管理者 / 一般用户 / 个人用户 | ✔ 登录即分流，接口与页面双重权限控制 |
| 管理者：查看信息、薪酬等级调整、禁止修改个人信息 | ✔ 员工档案、等级调整、只读档案 |
| 一般用户：新建用户、重置密码、薪酬调整、业绩确认 | ✔ 建号、重置、津贴/奖金比例调整、审核 |
| 个人用户：只维护自己的信息 | ✔ 可编辑字段白名单 + 锁定字段不可改 |
| 薪酬页面「展开」按钮折叠不常用数据 | ✔ 管理者薪酬页按需加载、展开/收起、互斥展开 |
| 员工一般信息 + 深度信息（曾就职、薪酬等） | ✔ 详情页展示曾就职、薪酬构成、业绩、调整流水 |
| 管理者和一般用户也在员工表中，以员工编号登录 | ✔ M0001 / H0001 / H0002 均为员工并可维护本人信息 |
| 部分信息不可修改，业绩通过新增记录添加 | ✔ 锁定字段 + 月度业绩记录 |
| 离职标记、禁止登录、仅管理者可查 | ✔ `status=RESIGNED`，登录统一提示错误，列表按角色过滤 |
| 进阶：简历 PDF 下载 | ✔ PDFBox + 中文字体渲染，仅本人可下载 |
| 进阶：私发红包 | ✔ 仅管理者发放，附言仅收件人可见，金额计入当月薪资 |
| 进阶：业绩需确认才有效 | ✔ `PENDING → APPROVED / REJECTED`，驳回可重提 |
| 进阶：仅本人可见薪资 | ✔ 业务层归属校验 |
| 进阶：薪资比例 / 薪资曲线 / 业绩曲线 | ✔ ECharts 饼图 + 双折线，支持多选员工对比 |
| 前端布局 | ✔ B 站风格顶部工作台导航；右上角个人信息弹窗内含「修改密码 / 退出登录」 |

`docs/需求分析与设计文档.md` 是完整的需求、架构、数据库、接口与页面设计说明。

---

## 2. 技术栈

| 层 | 技术 |
| --- | --- |
| 运行环境 | JDK 25（题目要求 JDK 24，经确认使用本机 JDK 25 编译运行） |
| 框架 | Spring Boot 4.0.9（Spring MVC、Validation、Jackson 3） |
| 持久层 | MyBatis 4.0.1 + MySQL 8.0.19 + HikariCP |
| 数据库脚本 | `schema.sql` / `data.sql`，启动自动初始化，可重复执行 |
| PDF | Apache PDFBox 3.0.5（系统中文字体） |
| 前端 | HTML5 + CSS3 + ES6，B 站风格（粉 `#FB7299` / 蓝 `#00AEEC`），DIN 数字排版、轻边框与大留白；原生页面 + SVG 品牌图标；ECharts 5.5.1 本地文件 |
| 密码 | PBKDF2-HMAC-SHA256（12 万次迭代 + 随机盐） |
| 构建 | Maven（IntelliJ 内置即可） |

---

## 3. 项目结构

```
src/main/java/org/example
├── common/      统一响应、异常、会话拦截、密码哈希、月份工具
├── config/      Web 拦截器配置
├── entity/      数据库实体（9 张表）
├── dto/         请求参数对象（带校验）
├── vo/          响应视图对象
├── mapper/      MyBatis Mapper 接口
├── service/     业务接口（impl 为实现）
└── web/         REST 控制器

src/main/resources
├── application.properties
├── db/          schema.sql / data.sql
├── mapper/      MyBatis XML
└── static/      16 个页面 + 样式 + 脚本 + ECharts + favicon
```

依赖方向严格单向：`web → service → mapper`，Controller 不接触 Mapper，Service 不依赖 Servlet API。

---

## 4. 快速开始

### 4.1 环境

- JDK 25（`java -version` 可验证）
- MySQL 8（本机 3306，服务已启动）
- Maven（IDEA 自带或独立安装）

### 4.2 数据库

默认配置在 `src/main/resources/application.properties`：

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/hrm?createDatabaseIfNotExist=true&useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&useSSL=false
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:root}
```

默认使用 `root/root`；也可以通过环境变量 `DB_USERNAME`、`DB_PASSWORD` 覆盖，避免把本机数据库密码提交到仓库。

启动时会自动创建 `hrm` 库、执行 `db/schema.sql` 与 `db/data.sql`；脚本使用 `IF NOT EXISTS` / `INSERT IGNORE`，重复启动不会产生重复数据。
如需重置演示数据，删除 `hrm` 库后重启即可。

### 4.3 启动

```bash
# 方式一：Maven 直接运行
mvn -DskipTests spring-boot:run

# 方式二：打包运行
mvn -DskipTests package
java -jar target/HR-Management-System-0.0.1-SNAPSHOT.jar
```

访问 <http://localhost:8080/login.html>。

### 4.4 演示账号

| 角色 | 工号 | 姓名 | 说明 |
| --- | --- | --- | --- |
| 管理者 | `M0001` | 张伟 | 档案、等级调整、红包、分析 |
| 一般用户 | `H0001` | 李娜 | 建号、重置密码、薪酬调整、审核 |
| 一般用户 | `H0002` | 王强 | 同上 |
| 个人用户 | `X1001` | 陈晨 | 首次登录需修改密码 |
| 个人用户 | `X1002` | 刘洋 | 直接进入 |
| 离职员工 | `X1012` | 邓超 | 登录被拒绝，仅管理者可查 |

初始密码统一为 `123456`。`X1001` 以及新建用户的 `must_change_password=1`，未改密前只能访问改密接口。

---

## 5. 关键业务规则

- 应发薪资 = 基本工资（薪酬等级决定） + 岗位津贴 + 绩效奖金 + 红包
- 绩效奖金 = 基本工资 × 奖金比例 × 有效业绩分 / 100
- 业绩按月唯一，个人提交后为待审核；一般用户通过后才计入有效业绩；驳回可修改后重新提交
- 红包由管理者发放，金额 (0, 50000]，附言仅收件人可见，计入发放当月薪资
- 离职员工：登录统一返回「工号或密码错误」；一般用户列表不可见；管理者可按状态筛选查看
- 锁定字段（姓名、性别、出生日期、身份证、部门、职位、入职日期、等级、学历、院校、专业）在更新 SQL 中不存在，无法被前端篡改

---

## 6. 测试与验证

### 6.1 接口冒烟测试

```bash
node tools/smoke-test.mjs
```

覆盖 28 项：三角色登录、首次改密拦截、建号、业绩提交与审核、等级/津贴调整、红包、薪资核算、越权访问、离职登录、PDF 下载、静态页面与拦截跳转。
结果写入 `tools/smoke-result.txt`（当前 28/28 通过）。

> 脚本会创建测试员工 `X1099`，如反复执行，请先清理该工号的数据。

### 6.2 页面检查

```bash
node tools/browser/check.mjs
```

使用系统 Edge 无头浏览器打开 16 个页面并截图到 `docs/screenshots/`，同时检查控制台错误、资源 404/500 与横向溢出。 `tools/browser` 内已放置 playwright-core（已被 .gitignore 忽略），删除后需自行执行 `npm install playwright-core` 再运行。

### 6.3 单元测试

```bash
mvn test
```

---

## 7. 常见问题

**Q：启动报「Access denied for user 'root'」？**
修改 `application.properties` 中的 `spring.datasource.password` 为本机 MySQL 密码。

**Q：PDF 简历生成失败？**
程序按 `C:/Windows/Fonts/simhei.ttf → Deng.ttf → simkai.ttf → simfang.ttf` 顺序查找中文字体；也可在 `src/main/resources/fonts/resume.ttf` 放置任意中文字体作为兜底。

**Q：页面图表不显示？**
ECharts 已放在 `static/assets/vendor/echarts.min.js`，无需联网；确认浏览器允许执行本地脚本。

**Q：为什么所有日期都是 2026 年？**
演示数据以生成当天（2026-10）为基准，生成脚本为 `tools/gen_seed_sql.py`。

---

## 8. 目录内的辅助脚本

| 文件 | 用途 |
| --- | --- |
| `tools/gen_seed_sql.py` | 生成 `db/data.sql` 演示数据（含密码哈希、薪资快照） |
| `tools/smoke-test.mjs` | 端到端接口测试 |
| `tools/browser/check.mjs` | 无头浏览器页面检查与截图 |
| `docs/需求分析与设计文档.md` | 需求、架构、数据库、接口、页面设计 |
| `docs/screenshots/` | 关键页面截图（21 张） |
