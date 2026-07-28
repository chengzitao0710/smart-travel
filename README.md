# 智游天下 - 智慧旅游综合服务平台

> 基于 Spring Boot 3.2.5 + Java 17 的智慧旅游综合服务平台（仅后端）

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-orange)](https://www.oracle.com/java/)
[![License](https://img.shields.io/badge/License-MIT-blue)](LICENSE)

## 项目简介

智游天下是一个集景点搜索、门票秒杀、路线规划、游记社区、社交互动、轨迹打卡于一体的智慧旅游平台。采用 Maven 多模块架构，**本项目仅实现后端服务**，提供 50+ RESTful API 接口，推荐使用 Knife4j 接口文档（`/doc.html`）或 Postman 进行接口调试。

## 技术架构

| 类别 | 技术选型                                 |
|------|--------------------------------------|
| 核心框架 | Spring Boot 3.2.5、MyBatis-Plus 3.5.7 |
| 数据库 | MySQL 8.0                            |
| 缓存 | Redis（缓存穿透保护、逻辑过期、GEO、BitMap、ZSet）   |
| 搜索引擎 | Elasticsearch 8.12.0 + IK 分词器        |
| 消息队列 | RabbitMQ（异步下单、销量同步）                  |
| 分布式锁 | Redisson                             |
| 认证鉴权 | JWT + 拦截器                            |
| 对象存储 | 阿里云 OSS                              |
| 地图服务 | 高德地图 API（地理编码、POI、导航）                |
| 接口文档 | Knife4j（Swagger 增强）                  |
| 开发语言 | Java 17                              |

## 模块结构

```
smart-travel
├── docker-compose.yml           # Docker 中间件一键部署
├── .github/                     # Issue/PR 模板
├── smart-travel-common      # 公共模块：JWT、Redis工具、缓存、拦截器、OSS
├── smart-travel-gateway     # 主启动入口（端口 8080）
├── smart-travel-user        # 用户模块：注册/登录、签到、JWT认证
├── smart-travel-scenic      # 景点模块：CRUD、ES搜索、Redis GEO、高德POI
├── smart-travel-ticket      # 门票模块：秒杀(Lua+Redisson)、订单、支付
├── smart-travel-route       # 路线模块：路线规划、高德导航
├── smart-travel-travel      # 游记模块：发布/评论/点赞、ES搜索、热门榜单
├── smart-travel-social      # 社交模块：关注/取关、收藏(景点/游记/路线)
└── smart-travel-trajectory  # 轨迹模块：景点打卡、轨迹记录
```

## 核心功能

### 高并发门票秒杀
- Redis + Lua 脚本原子化库存扣减，防止超卖
- Redisson 分布式锁控制用户下单频率
- RabbitMQ 异步削峰，支付回调同步 ES 索引

### 多维度景点搜索
- Redis GEO 实现附近景点搜索（5km 半径）
- Elasticsearch + IK 分词器全文检索，故障自动降级 MySQL
- 支持按类型、区域、热度、评分、距离组合筛选

### 路线规划
- 高德地图 API 路线导航计算
- 行程批量保存、拖拽排序、封面图 OSS 上传

### 游记社区
- 游记发布/草稿/审核/点赞/搜索
- 二级评论（楼中楼）、点赞、举报
- Redis ZSet 热门游记榜单
- 评论数同步更新关联景点 ES 索引

### 社交互动
- 关注/取关、共同关注（Redis Set 交集）
- 统一收藏接口（景点/游记/路线）

### 用户系统
- 手机验证码 + 密码登录双模式
- JWT 无状态认证 + 拦截器
- Redis BitMap 签到统计

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.8+
- Docker & Docker Compose（推荐）

### 方式一：Docker Compose 一键部署中间件（推荐）

```bash
# 1. 创建必要目录
mkdir -p /root/mysql/conf /root/mysql/data /root/mysql/init
mkdir -p /root/redis/conf /root/redis/data

# 2. 创建 Redis 配置文件
echo "bind 0.0.0.0" > /root/redis/conf/redis.conf
echo "protected-mode no" >> /root/redis/conf/redis.conf

# 3. 启动所有中间件（MySQL、Redis、RabbitMQ、Elasticsearch）
docker-compose up -d

# 4. 初始化数据库（将 SQL 文件放入 init 目录自动执行）
cp smart-travel-gateway/src/main/resources/db/smart_travel.sql /root/mysql/init/

# 5. 安装 Elasticsearch IK 分词器插件
docker exec -it es /bin/bash
./bin/elasticsearch-plugin install https://get.infini.cloud/elasticsearch/analysis-ik/8.12.0
exit
docker restart es
```

### 方式二：手动部署

### 1. 克隆项目

```bash
git clone https://github.com/你的用户名/smart-travel.git
cd smart-travel
```

### 2. 初始化数据库

执行 `smart-travel-gateway/src/main/resources/db/smart-travel.sql` 建表脚本。

### 3. 配置环境变量

在 `smart-travel-gateway/src/main/resources/application.yml` 中配置：

```yaml
# 数据库
spring:
  datasource:
    url: jdbc:mysql://你的IP:3306/smart-travel
    username: root
    password: 你的密码

  # Redis
  data:
    redis:
      host: 你的IP
      port: 6379
      password: 你的密码

  # RabbitMQ
  rabbitmq:
    host: 你的IP
    port: 5672
    username: guest
    password: guest

# Elasticsearch
elasticsearch:
  host: 你的IP
  port: 9200

# 阿里云 OSS
oss:
  access-key-id: ${OSS_ACCESS_KEY_ID}
  access-key-secret: ${OSS_ACCESS_KEY_SECRET}

# 高德地图
amap:
  key: ${AMAP_KEY}
```

### 4. 启动项目

```bash
mvn clean install -DskipTests
cd smart-travel-gateway
mvn spring-boot:run
```

### 5. 访问接口文档

启动后访问：http://localhost:8080/doc.html

## API 概览

| 模块 | 接口前缀 | 示例 |
|------|---------|------|
| 用户 | `/api/user/**` | 注册、登录、签到 |
| 景点 | `/api/scenic/**` | 搜索、附近、详情 |
| 门票 | `/api/ticket/**` | 秒杀、下单、支付 |
| 路线 | `/api/route/**` | 规划、导航 |
| 游记 | `/api/travel-note/**` | 发布、搜索、评论 |
| 社交 | `/api/social/**` | 关注、收藏 |
| 轨迹 | `/api/trajectory/**` | 打卡、轨迹 |

## 项目亮点

- **高并发秒杀**：Lua + Redisson + RabbitMQ 三级防护，QPS 可达万级
- **缓存策略**：缓存穿透保护 + 逻辑过期 + Cache-Aside 模式
- **搜索降级**：ES 故障自动降级 MySQL，保证服务可用性
- **数据一致性**：MySQL 更新 → ES 同步 → Redis 缓存同步，多级数据一致性
- **多模块架构**：9 个 Maven 模块，高内聚低耦合

## 参与贡献

欢迎提交 Issue 和 Pull Request！

1. Fork 本仓库
2. 创建分支：`git checkout -b feat/你的功能`
3. 提交代码：`git commit -m "feat: 功能描述"`
4. 推送分支：`git push origin feat/你的功能`
5. 提交 Pull Request

### 提交规范

| 前缀 | 用途 |
|------|------|
| `feat:` | 新功能 |
| `fix:` | 修复 Bug |
| `docs:` | 文档更新 |
| `refactor:` | 代码重构 |
| `perf:` | 性能优化 |

## License

MIT License