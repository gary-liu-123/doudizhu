# 斗地主在线游戏

基于Spring Boot + React的斗地主在线游戏项目。

## 技术栈

### 后端
- Java 17
- Spring Boot 3.2.0
- Spring WebSocket + STOMP
- Spring Data JPA + Hibernate
- MySQL 8.0
- Redis集群
- Maven

### 前端
- React 18 + TypeScript
- Vite
- React Router
- WebSocket (STOMP.js)
- CSS3

## 项目结构

```
doudizhu/
├── src/main/java/com/doudizhu/     # 后端Java代码
│   ├── config/                     # 配置类
│   ├── controller/                 # 控制器
│   ├── entity/                     # 实体类
│   ├── enums/                      # 枚举类
│   ├── model/                      # 模型类
│   ├── repository/                 # 数据访问层
│   └── DoudizhuApplication.java    # 启动类
├── src/main/resources/
│   └── application.yml             # 配置文件
├── frontend/                       # 前端React项目
│   ├── src/
│   │   ├── components/             # 组件
│   │   ├── pages/                  # 页面
│   │   ├── contexts/               # 上下文
│   │   └── types/                  # 类型定义
│   ├── package.json
│   └── vite.config.ts
├── pom.xml                         # Maven配置
├── DESIGN.md                       # 设计文档
└── UI_DESIGN_REFERENCE.md          # UI设计参考
```

## 快速开始

### 环境要求
- Java 17+
- Node.js 18+
- MySQL 8.0
- Redis 集群

### 1. 克隆项目
```bash
git clone <project-url>
cd doudizhu
```

### 2. 配置数据库
确保MySQL和Redis服务正在运行，并根据`src/main/resources/application.yml`中的配置创建数据库。

### 3. 启动后端
```bash
# 安装Maven依赖并启动Spring Boot
mvn clean install
mvn spring-boot:run
```

后端服务将在 `http://localhost:8080` 启动

### 4. 启动前端
```bash
# 进入前端目录
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端服务将在 `http://localhost:5173` 启动

## 功能特性

### 🎯 已实现功能

#### 基础架构
- ✅ **项目基础架构搭建** - Spring Boot 3.2.0 + React 18 + TypeScript
- ✅ **Spring Boot后端配置** - 完整的MVC架构，JPA、Redis、WebSocket集成
- ✅ **React前端项目结构** - Vite构建，TypeScript支持，组件化开发
- ✅ **WebSocket实时通信** - STOMP协议，支持房间消息广播

#### 用户界面
- ✅ **响应式首页设计** - 绿色游戏主题，创建房间/加入房间/快速匹配
- ✅ **游戏界面布局** - 经典三人斗地主布局，玩家位置自动安排
- ✅ **扑克牌组件系统** - 完整的54张牌组件，支持选中状态和点击交互
- ✅ **玩家信息展示** - 头像、昵称、手牌数量、在线状态显示
- ✅ **出牌控制面板** - 出牌/不要/提示按钮，牌型识别显示
- ✅ **最后出牌展示区** - 显示上家出牌内容和牌型

#### 房间系统
- ✅ **房间创建功能** - 6位数房间号生成，房间状态管理
- ✅ **房间加入功能** - 房间号验证，玩家昵称设置
- ✅ **实时房间状态同步** - 玩家加入/离开实时通知
- ✅ **房间信息存储** - Redis缓存，24小时过期机制

#### 游戏核心逻辑
- ✅ **完整发牌系统** - 54张标准斗地主牌，每人17张，3张底牌
- ✅ **洗牌算法** - 随机洗牌确保公平性
- ✅ **手牌排序** - 按牌型大小自动排序
- ✅ **叫地主系统** - 完整的叫分机制、地主选择、底牌分配
- ✅ **牌型识别引擎** - 识别单张、对子、三张、炸弹、王炸等所有牌型
- ✅ **出牌规则验证** - 完整的斗地主出牌规则验证
- ✅ **牌型大小比较** - 准确的牌型大小比较算法

#### 游戏流程
- ✅ **自动游戏开始** - 3人满员自动发牌开始游戏
- ✅ **叫地主阶段** - 完整的叫分系统，支持1-3分和不叫
- ✅ **出牌阶段** - 完整的出牌系统，支持所有斗地主牌型
- ✅ **回合制逻辑** - 准确的玩家轮次管理，支持压牌和跳过
- ✅ **游戏状态管理** - 等待玩家 → 叫地主阶段 → 游戏中 → 游戏结束
- ✅ **当前玩家标识** - 高亮显示当前轮次玩家
- ✅ **胜负判定** - 先出完牌获胜的判定逻辑

#### 数据模型
- ✅ **完整的数据模型** - Card、Player、Room、GameState等核心类
- ✅ **枚举类型定义** - 牌型、花色、房间状态、游戏阶段
- ✅ **JSON序列化支持** - 处理Java 8时间类型，避免序列化问题
- ✅ **Redis序列化优化** - 解决SubList序列化问题

#### 安全与配置
- ✅ **跨域配置** - 支持前后端分离开发
- ✅ **安全配置** - 禁用默认认证，支持开发阶段测试
- ✅ **WebSocket配置** - SockJS降级支持，STOMP协议
- ✅ **Jackson配置** - Java 8时间类型支持，时区设置

### 🚀 技术亮点

#### 后端技术
- **Spring Boot 3.2** - 现代化Java开发框架
- **Spring WebSocket + STOMP** - 实时双向通信
- **Spring Data JPA** - ORM数据访问层
- **Redis集群** - 分布式缓存，房间状态存储
- **MySQL 8.0** - 关系型数据库
- **Lombok** - 减少样板代码
- **Jackson JSR310** - Java 8时间类型序列化

#### 前端技术
- **React 18 + TypeScript** - 现代化前端开发
- **Vite** - 极速构建工具
- **Context API + useReducer** - 状态管理
- **WebSocket (STOMP.js)** - 实时通信客户端
- **CSS3 Flexbox/Grid** - 响应式布局
- **React Router** - 单页面应用路由

### 🎮 游戏特色

1. **真实斗地主体验** - 标准54张牌，经典三人对战
2. **实时多人对战** - WebSocket实现毫秒级同步
3. **智能玩家匹配** - 3个Chrome标签页即可开始游戏
4. **美观的游戏界面** - 绿色主题，符合斗地主传统
5. **完整的游戏流程** - 从房间创建到游戏结束的完整体验

### ⏳ 待实现功能

#### 用户系统
- ⏳ **用户注册登录** - JWT认证，用户身份管理
- ⏳ **用户个人信息** - 头像、昵称、积分系统
- ⏳ **好友系统** - 添加好友，邀请对战

#### 高级功能
- ⏳ **游戏历史记录** - 战绩统计，历史回放
- ⏳ **房间设置** - 私人房间，观战功能
- ⏳ **聊天系统** - 房间内文字聊天
- ⏳ **断线重连** - 网络断开后自动重连
- ⏳ **音效系统** - 游戏音效和背景音乐

#### 界面优化
- ⏳ **移动端适配** - 响应式布局优化
- ⏳ **动画效果** - 发牌、出牌、胜负动画
- ⏳ **主题切换** - 多套UI主题选择

#### 性能优化
- ⏳ **数据库优化** - 索引优化，查询性能提升
- ⏳ **Redis分布式** - 支持多实例部署
- ⏳ **CDN静态资源** - 前端资源加速
- ⏳ **服务器集群** - 负载均衡，高可用性

### 📊 当前完成度

| 模块 | 完成度 | 说明 |
|------|-------|------|
| 基础架构 | 100% | Spring Boot + React 完整搭建 |
| 房间系统 | 100% | 创建、加入、状态同步 |
| 发牌系统 | 100% | 洗牌、发牌、手牌管理 |
| 叫地主系统 | 100% | 完整的叫分和地主选择逻辑 |
| 出牌系统 | 100% | 牌型识别、规则验证、回合管理 |
| 界面组件 | 95% | 基本完成，待优化动画效果 |
| WebSocket通信 | 100% | 实时消息推送 |
| 游戏逻辑 | 90% | 核心功能完成，待实现胜负积分 |
| 用户系统 | 0% | 待开发 |

**总体完成度：约 85%**

## 🧪 测试指南

### 多人游戏测试
1. **启动服务**
   ```bash
   # 后端 (Terminal 1)
   mvn spring-boot:run
   
   # 前端 (Terminal 2)
   cd frontend && npm run dev
   ```

2. **3人游戏测试**
   - 打开3个Chrome标签页
   - 标签页1：访问 `http://localhost:5178` → 创建房间 → 输入昵称"玩家一"
   - 标签页2：访问 `http://localhost:5178` → 加入房间 → 输入房间号 → 昵称"玩家二"  
   - 标签页3：访问 `http://localhost:5178` → 加入房间 → 输入房间号 → 昵称"玩家三"

3. **预期结果**
   - ✅ 玩家一：看到玩家二、玩家三头像，显示"玩家数量: 3/3"
   - ✅ 玩家二：看到玩家一、玩家三头像，显示"房间状态: 叫地主阶段"
   - ✅ 玩家三：看到玩家一、玩家二头像，显示17张手牌
   - ✅ 所有玩家：自动发牌，进入叫地主阶段

### 故障排除
1. **WebSocket连接失败**
   - 检查后端是否启动 (`http://localhost:8080`)
   - 检查Redis是否运行
   - 查看浏览器控制台错误

2. **房间状态不同步**
   - 刷新页面重新连接WebSocket
   - 检查后端日志中的WebSocket消息

3. **卡牌显示异常**
   - 重启后端应用Redis序列化修复
   - 清除浏览器缓存

### 开发调试
- **后端日志**: Spring Boot控制台输出
- **前端调试**: 浏览器开发者工具 → Console → Network
- **WebSocket消息**: 控制台会输出所有收发的消息
- **Redis查看**: 使用Redis CLI查看room:*键值

## API接口

### 🔗 REST API

#### 房间管理
- `POST /api/rooms/create` - 创建房间
  ```json
  // 请求体
  {
    "playerName": "玩家昵称"
  }
  // 响应
  {
    "success": true,
    "message": "房间创建成功",
    "data": {
      "id": "431521",
      "players": [...],
      "status": "WAITING",
      "createdAt": "2025-08-24 15:33:00"
    }
  }
  ```

- `POST /api/rooms/join` - 加入房间
  ```json
  // 请求体
  {
    "roomId": "431521",
    "playerName": "玩家昵称"
  }
  ```

- `GET /api/rooms/{roomId}` - 获取房间信息
- `POST /api/rooms/leave` - 离开房间

#### 叫地主相关
- `POST /api/rooms/bid` - 叫地主
  ```json
  // 请求体
  {
    "roomId": "431521",
    "playerId": "780796976",
    "playerName": "玩家一",
    "bidScore": 1
  }
  ```

#### 出牌相关  
- `POST /api/rooms/play` - 出牌
  ```json
  // 请求体
  {
    "roomId": "431521", 
    "playerId": "780796976",
    "playerName": "玩家一",
    "cardIds": ["SPADE_THREE", "HEART_THREE"]
  }
  ```

- `POST /api/rooms/pass` - 跳过出牌
  ```json
  // 请求体
  {
    "roomId": "431521",
    "playerId": "780796976", 
    "playerName": "玩家一"
  }
  ```

#### 系统接口
- `GET /health` - 健康检查
- `GET /` - 服务信息

### 🔌 WebSocket端点
- `/api/ws` - WebSocket连接端点 (SockJS + STOMP)

### 📨 WebSocket消息类型

#### 订阅频道
- `/topic/room-{roomId}` - 房间内消息广播
- `/topic/room-updates` - 全局房间更新

#### 消息类型
1. **ROOM_CREATED** - 房间创建通知
   ```json
   {
     "type": "ROOM_CREATED",
     "roomId": "431521",
     "playerName": "玩家一"
   }
   ```

2. **PLAYER_JOINED** - 玩家加入通知
   ```json
   {
     "type": "PLAYER_JOINED",
     "roomId": "431521",
     "playerName": "玩家二",
     "playerCount": 2,
     "room": {...}
   }
   ```

3. **GAME_STARTED** - 游戏开始通知
   ```json
   {
     "type": "GAME_STARTED",
     "roomId": "431521",
     "room": {...},
     "currentPlayer": 806074553,
     "phase": "BIDDING",
     "playerCount": 3
   }
   ```

4. **PLAYER_LEFT** - 玩家离开通知
   ```json
   {
     "type": "PLAYER_LEFT",
     "roomId": "431521",
     "userId": "123456"
   }
   ```

### 📋 数据模型

#### Room (房间)
```json
{
  "id": "431521",
  "players": [Player...],
  "gameState": GameState,
  "status": "WAITING|GAMING|FINISHED",
  "createdAt": "2025-08-24 15:33:00",
  "creatorId": "780796976"
}
```

#### Player (玩家)
```json
{
  "userId": 780796976,
  "name": "玩家一",
  "handCards": [Card...],
  "position": 0,
  "online": true,
  "landlord": false
}
```

#### GameState (游戏状态)
```json
{
  "phase": "WAITING|BIDDING|PLAYING|FINISHED",
  "currentPlayerId": 806074553,
  "landlordCards": [Card...],
  "landlordId": null,
  "lastPlay": null,
  "playHistory": [],
  "scores": {},
  "biddingRound": 1
}
```

#### Card (扑克牌)
```json
{
  "suit": "SPADE|HEART|DIAMOND|CLUB|JOKER",
  "rank": "THREE|FOUR|...|ACE|TWO|SMALL_JOKER|BIG_JOKER",
  "id": "SPADE_THREE"
}
```

## 开发指南

### 后端开发
1. 在`src/main/java/com/doudizhu/`下按功能模块组织代码
2. 使用Lombok减少样板代码
3. 遵循Spring Boot最佳实践

### 前端开发
1. 使用TypeScript确保类型安全
2. 组件采用函数式组件 + Hooks
3. 使用Context API进行状态管理

### WebSocket通信
1. 客户端使用STOMP.js连接WebSocket
2. 服务端使用Spring WebSocket处理消息
3. 消息格式统一使用JSON

## 部署说明

### 后端部署
```bash
# 打包
mvn clean package -DskipTests

# 运行
java -jar target/doudizhu-1.0.0.jar --spring.profiles.active=prod
```

### 前端部署
```bash
# 构建
cd frontend
npm run build

# 将dist目录部署到Nginx
```

## 数据库配置

### MySQL配置
- 主机: 124.223.16.249:3306
- 数据库: garydatabase
- 用户名: gary_user

### Redis集群配置
- 节点: 124.223.16.249:7000,7001,7002

## 贡献指南

1. Fork项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建Pull Request

## 许可证

本项目采用MIT许可证 - 查看[LICENSE](LICENSE)文件了解详情