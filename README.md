# 健身房管理系统

基于 **Spring Boot 3 + MyBatis + Vue 3 + Element Plus + MySQL** 的前后端分离健身房管理系统毕业设计。

系统面向 **管理员 / 教练 / 会员** 三种角色，覆盖场馆运营的完整业务闭环：
教练发布课程 → 管理员审核上架 → 会员购买课程 → 会员预约课程 → 教练确认/拒绝 → 到店考勤 → 会员健身打卡与身体数据看板。

---

## 技术栈

| 端 | 技术 |
| --- | --- |
| 后端 | Spring Boot 3.2.5、MyBatis、MySQL 8、Lombok、Hutool（验证码） |
| 前端 | Vue 3、Vue Router、Element Plus、Vite 6 |
| 运行环境 | JDK 17+、Node 18+ |

## 目录结构

```
Gym-Management-System
├── db/init_mysql.sql        # 数据库初始化脚本（建库建表 + 演示数据）
├── src/main/java/...        # 后端源码（controller/entity/mapper/service/util）
├── src/main/resources/      # 后端配置 application.yml
└── frontend/                # 前端源码（views/router/utils）
```

## 快速开始

### 1. 初始化数据库

```bash
mysql -uroot -p < db/init_mysql.sql
```

> 脚本会**删除并重建** `gym_management_system` 库（仅用于开发环境），
> 表结构、注释、外键与演示数据一次到位。

如需调整数据库连接，修改 `src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/gym_management_system?useUnicode=true&characterEncoding=utf8&serverTimezone=GMT%2B8
    username: root
    password: 123456
```

### 2. 启动后端（端口 8091）

- 方式一：IntelliJ IDEA 打开项目，直接运行 `AdminApplication`
- 方式二：命令行

```bash
mvn spring-boot:run
# 或打包后运行
mvn -DskipTests package
java -jar target/javademo-0.0.1-SNAPSHOT.jar
```

### 3. 启动前端（端口 5173）

```bash
cd frontend
npm install
npm run dev
```

浏览器访问 <http://localhost:5173>（已配置 `/api` 代理到后端 8091）。

生产构建：`npm run build`，产物在 `frontend/dist`。

## 默认账号（密码均为 `123456`）

| 角色 | 账号 | 说明 |
| --- | --- | --- |
| 管理员 | `admin` | 会员/教练/课程/订单管理、课程审核、数据看板 |
| 教练 | `coach001` | 张伟（减脂塑形），coach002/coach003 同上 |
| 会员 | `member001` | 陈晨，member002/003 同上 |

登录页按角色切换页签，带图形验证码，登录态使用 Session + Cookie。

## 功能模块

### 管理员端 `/admin`
- 运营数据看板：会员/课程/预约/今日到店统计、本月预约状态占比
- 会员管理：增删改查、启用/禁用、按状态筛选
- 教练管理：增删改查、启用/禁用
- 课程管理：全部课程/待审核双视图，审核通过（自动上架）或拒绝（下架）、手动上架/下架、删除
- 订单管理：全量预约记录，按状态（待教练查看/已预约/预约成功/已到店/未到店/预约失败）筛选

### 教练端 `/coach`
- 首页：今日/明日已确认课表、下一节课倒计时、未读预约消息提醒
- 课程管理：发布课程（待审核）、编辑（重新送审）、主动下架、删除
- 预约查看：处理"待教练查看"预约（通过→预约成功 / 拒绝→已拒绝），过期预约自动置为已失效
- 课程考勤：预约时间到达后可标记 已到店 / 未到店

### 会员端 `/member`
- 首页：最近体重/体脂/BMI、本月训练次数与卡路里、今日/明日课表
- 首页：**AI 健身顾问**——结合本人 BMI/体重/本月训练数据，回答减脂、增肌、饮食、计划、拉伸、伤痛等问题
- 课程购买：浏览上架课程（含教练信息）、购买生成课时
- 课程预约：基于已购课时选择上课时间（同一会员/同一教练时间冲突自动拦截），提交后待教练查看
- 我的预约：查看全部预约与状态（已预约课程自动处理过期失效）
- 健身打卡：每日打卡 + 录入体重/体脂/BMI/卡路里（支持手动/设备来源），历史记录分页
- 个人信息：维护资料、修改密码

## 业务状态约定

- 课程 `sale_status`：`待审核` →（审核通过）`上架` /（审核拒绝）`下架`
- 预约 `order_status`：`待教练查看` →（同意）`预约成功` /（拒绝）`已拒绝`；到点后教练标记 `已到店` / `未到店`；超时未处理自动 `已失效`
- 教练/会员账号状态：`启用` / `禁用`（`正常` / `禁用`），被禁用的账号无法登录

## 数据库表

`admin` 管理员、`coach` 教练、`member` 会员、`course` 课程、
`course_buy` 课程购买（课时）、`course_order` 课程预约、
`clock_in` 健身打卡（体重/体脂/BMI/卡路里）、`coach_order_notice_read` 教练预约消息已读记录。

外键约束保证数据一致性：课程→教练、购买/预约→课程/教练/会员、打卡→会员。

## AI 健身顾问

会员首页内置「AI 健身助手」，提问后会结合该会员的**体重、BMI、本月训练次数、本月消耗卡路里**给出个性化建议。

采用**双引擎设计**，保证任何环境都能使用：

| 引擎 | 配置 | 特点 |
| --- | --- | --- |
| 内置知识库（默认） | `ai.provider: local` | 规则引擎 + 健身知识库，**无需联网、无需 Key**，答辩演示稳定可用 |
| 大模型 | `ai.provider: llm` + base-url/api-key/model | 接入任意 OpenAI 兼容协议模型（DeepSeek / 通义 / OpenAI 等），回答更灵活 |

大模型调用失败（网络异常、Key 失效）会**自动回退**到本地知识库，功能不受影响。

切换为大模型的配置示例（`src/main/resources/application.yml`）：

```yaml
ai:
  provider: llm
  base-url: https://api.deepseek.com/v1/chat/completions
  api-key: sk-xxxxxxxxxxxxxxxx
  model: deepseek-chat
  timeout-ms: 20000
```

后端实现：`AiAdviceService` / `AiAdviceServiceImpl`（`com.jincheng.service`），
接口 `GET /api/member/ai/info`（当前引擎）、`POST /api/member/ai/advice`（提问）；
前端组件：`frontend/src/components/AiAdvisor.vue`。

## 常用接口速览

- 登录：`POST /api/login/{member|coach|admin}`、`GET /api/verifyCode`
- 管理端：`/api/admin/member`、`/api/admin/coach`、`/api/admin/course`、`/api/admin/order`
- 教练端：`/api/coach/course`、`/api/coach/order`（pending/schedule/attendance/notice）
- 会员端：`/api/member/course`、`/api/member/order`、`/api/member/clockin`、`/api/member/dashboard`、`/api/member/ai`

未登录访问受保护接口返回 401，前端自动跳转登录页。
