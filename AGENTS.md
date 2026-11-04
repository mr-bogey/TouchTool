# AGENTS.md

本文件记录本项目的开发约定与注意事项，供后续开发参考。

## 项目概览

- 技术栈：Android / Java、ViewBinding、Material3、RecyclerView + ItemTouchHelper、data-binding 布局。
- 构建：`gradlew.bat :app:assembleDebug`。本机需先设置 `JAVA_HOME`（如 `D:\Android\Android Studio\jbr`）。
- 提交信息风格：`feat(scope): 中文标题` + 中文正文要点；默认**不推送**。

## 约定

1. **不要改动共享基类来做单项功能**：功能相近但归属不同的代码，宁可复制过来，也不要让两者互相继承。
2. 保持项目既有写法与风格，代码方法加简短中文注释。
3. 提交保持未推送状态，除非明确要求推送。
4. 真机行为（手势、布局观感）无法用构建验证，涉及这类改动需说明待验证项。

## 注意事项

书写规则：每条只陈述**当前事实**——现在是什么样、有什么约束、有什么已知隐患，据此可判断能不能做、该怎么做。不写成改造记录：不写改动动机、提交号、变更前后对比与待验证清单；已由 `## 约定` 覆盖的事项不重复。

- **针脚卡片**：`pins` 顺序即运行期取值顺序，任何拖动排序都必须回写 `action.getPins()`，且一个方位只能有一个容器；拖动一律用长按整行（柄只是视觉提示），不可拖动的行由 `getMovementFlags()` 返回 `(0, 0)` 从机制上排除；针脚视图的 `pins` 声明顺序不能变（`reAddPins` 按类顺序匹配，变了老任务丢针脚）；把默认针脚改成可移除时必须同步检查取值逻辑——不能硬编码 `pins.add(firstPin)`，要按标志针脚界定范围，被固定针脚（如 `resultPin`）隔成两段的不适合改；删 `android:visibility="gone"` 前确认它不是该控件的唯一隐藏机制。

- **动态针脚动作的卡片**：开关、选择、顺序、随机、并行、数值相加、数值相乘、文本拼接、或、与、短路与、短路或、位置转手势这 13 项由 `DynamicPinsActionCard` 承载，四个方位各一个 RecyclerView，一次添加多条针脚的动作同组并作一行拖动；`SwitchAction` 与数值相减、相除、取模仍用 `NormalActionCard`；数值加/乘与文本拼接的动态针脚被 `resultPin` 隔成两段，其默认针脚不可移除。

- **导入任务**：导入对话框有「导入副本」开关，默认关闭。关闭时按任务 id 写入（本地同 id 任务会被覆盖）；开启时本地任务不变，导入任务以新 id 落库，标题冲突依次取 `原名_复制`、`原名_复制_2`。`TaskRecord.duplicate()` 换根任务 id 并递归改写副本内的 `PinTaskString`；变量 id 不变，也不覆盖本地已有的全局变量；`ExportTaskDialog` 隐藏该开关。该流程要求先调 `getTaskRecord()` 再 `duplicate()`（前者就地做 `cleanInvalidTag()`）。`Identity` 以 uid+id 做 hash，对象进入 Set/Map 后不能再 `setId`。

- **开关任务**：`SwitchTaskAction` 的任务针脚是 `ALL_TASK_ID`（`NotLinkAblePin`，可选到全部任务）；任务针脚按 id 用 `TaskSaver.getTask()` 直接取存档任务（与 `StopTaskAction` 同款，不做 `upFindTask` 上溯），运行中的任务只是副本、开始动作的真实数据在存档里，开关与保存必须落在存档任务上；关闭任务后要用 `FloatWindow.getView(PlayFloatView.class.getName())`（单按钮悬浮窗看 `getViews(SinglePlayView.class)`）判断悬浮窗是否显示着，显示着才调 `TaskInfoSummary.tryShowManualPlayView(true)` 刷新，否则手动执行悬浮窗上还留着已关闭的任务、或者悬浮窗被凭空弹出来；`tryShowManualPlayView(false)` 会清空列表使悬浮窗收起，不能用来刷新。

- **广播收发**：发送侧只按 `setPackage` 做包限定——app 针脚给的是 activity，用 `setClassName` 会变成指向组件的显式广播，动态注册的接收器永远收不到；app 针脚为空则发全局隐式广播；context 优先用无障碍服务，服务不在时退回 `MainApplication`。接收侧 `broadcastReceivers` 以 `taskId + actionId` 为键，重复注册必须先把旧接收器注销，否则改了触发器的广播名后旧过滤器残留、新名字收不到，只能重启服务；extras 用 `bundle.get()` 转字符串，`getString` 会把 int/bool 取成 null。

- **NodePicker**：选中结果没有快照回退——`roots` 由 `NodeInfo.getWindows()` 现场抓取，打开前已被移除的控件找不回（`findNode` 返回 null），只有手动导入 .ttl 才走离线树；打开后才移除的反而选得到，因为整棵树在构造时已物化冻结。已知隐患：`getChildCount()` 实时而 `getChild(i)` 优先返回缓存，会跳过末尾节点或错位返回兄弟节点；`PinNode.nodeInfo` 是 `transient`，选中结果不参与序列化。
